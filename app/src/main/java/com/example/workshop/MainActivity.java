package com.example.workshop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.bson.Document;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class MainActivity extends AppCompatActivity {
    String Appid = "application-0-jxtgbji";
    App app;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> mongoCollection;
    User user;
    EditText username,password;
    Button login;
    @SuppressLint({"MissingInflatedId","SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //testing
        setContentView(R.layout.activity_main);
        Realm.init(this);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        app = new App(Appid);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = username.getText().toString();
                String pass = password.getText().toString();
                if (id.trim().isEmpty() || pass.trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter details please", Toast.LENGTH_SHORT).show();
                } else {
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
                                mongoCollection.findOne(filter).getAsync(res ->{
                                    if (res.isSuccess()) {
                                        Document document = res.get();
                                        if (document != null) {
                                            String role = document.getString("role");
                                            if("admin".equals(role)){
                                                Intent intent = new Intent(MainActivity.this,admin.class);
                                                startActivity(intent);
                                            }
                                            else if("worker".equals(role)){
                                                Intent intent = new Intent(MainActivity.this,worker.class);
                                                startActivity(intent);
                                            }
                                            else if("client".equals(role)){
                                                Intent intent = new Intent(MainActivity.this,client.class);
                                                startActivity(intent);
                                            }
                                            else{
                                                Toast.makeText(MainActivity.this, "Undefined Role", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else {
                                            Toast.makeText(MainActivity.this, "Invalid User", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else {
                                        Toast.makeText(MainActivity.this, "User Not Found", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(MainActivity.this, "Try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}