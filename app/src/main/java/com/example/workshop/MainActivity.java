package com.example.workshop;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.bson.Document;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class MainActivity extends AppCompatActivity {
    private static final String APP_ID = BuildConfig.APP_ID;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_ROLE = "userRole";


    App app;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> mongoCollection;
    User user;
    EditText username, password;
    Button login;
    ProgressBar progressbarlogin;

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        Realm.init(this);
        app = new App(APP_ID);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        progressbarlogin = findViewById(R.id.progressbarlogin);

        //checkLoggedInStatus();

        login.setOnClickListener(v -> {
            progressbarlogin.setVisibility(View.VISIBLE);
            String id = username.getText().toString();
            String pass = password.getText().toString();

            if (id.trim().isEmpty() || pass.trim().isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter details please", Toast.LENGTH_SHORT).show();
                progressbarlogin.setVisibility(View.GONE);
            } else {
                loginUser(id, pass);
            }
        });
    }

    private void checkLoggedInStatus() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUsername = prefs.getString(KEY_USERNAME, null);
        String savedPassword = prefs.getString(KEY_PASSWORD, null);
        String savedRole = prefs.getString(KEY_USER_ROLE, null);

        if (savedUsername != null && savedPassword != null) {
            loginUser(savedUsername, savedPassword);
        }
    }

    private void loginUser(String id, String pass) {
        Credentials credentials = Credentials.emailPassword(id, pass);
        app.loginAsync(credentials, new App.Callback<User>() {
            @Override
            public void onResult(App.Result<User> result) {
                if (result.isSuccess()) {
                    user = app.currentUser();
                    mongoClient = user.getMongoClient("mongodb-atlas");
                    mongoDatabase = mongoClient.getDatabase("db");
                    mongoCollection = mongoDatabase.getCollection("users");

                    Document filter = new Document("username", id);
                    mongoCollection.findOne(filter).getAsync(res -> {
                        if (res.isSuccess()) {
                            Document document = res.get();
                            if (document != null) {
                                progressbarlogin.setVisibility(View.GONE);
                                String role = document.getString("role");

                               //saveLoginDetails(id, pass, role);
                                Log.d(TAG, "Saved Details ____________________________________________________________" + id + "  " + pass + "  "+ role);

                                if ("admin".equals(role)) {
                                    startActivity(new Intent(MainActivity.this, admin.class));
                                    finish();
                                } else if ("worker".equals(role)) {
                                    startActivity(new Intent(MainActivity.this, worker.class));
                                    finish();
                                } else if ("client".equals(role)) {
                                    startActivity(new Intent(MainActivity.this, clients_order.class));
                                    finish();
                                } else {
                                    Toast.makeText(MainActivity.this, "Undefined Role", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Invalid User", Toast.LENGTH_SHORT).show();
                                progressbarlogin.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                            progressbarlogin.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Login Failed, Try again", Toast.LENGTH_SHORT).show();
                    progressbarlogin.setVisibility(View.GONE);
                }
            }
        });
    }

    private void saveLoginDetails(String id, String pass, String role) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USERNAME, id);
        editor.putString(KEY_PASSWORD, pass);
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }
}
