package com.example.workshop;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.bson.Document;

import java.util.Calendar;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class client extends AppCompatActivity {

    private static final String TAG = "ClientActivity";
    private TextView orderDateEditText;

    private EditText orders;
    private Spinner prioritySpinner;
    private Button submitButton;
    private ProgressBar progressBar;

    private ImageView arrowback;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> mongoCollection;
    private User user;

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String APP_ID = BuildConfig.APP_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        orders = findViewById(R.id.workassign);
        orderDateEditText = findViewById(R.id.order_date);
        prioritySpinner = findViewById(R.id.priority);
        submitButton = findViewById(R.id.Submit);
        progressBar = findViewById(R.id.progressbarlogin);


        arrowback=findViewById(R.id.arrowback);

        arrowback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(client.this, clients_order.class));            }
        });

        App app = new App(new AppConfiguration.Builder(APP_ID).build());

        user = app.currentUser();
        if (user == null) {
            Log.e(TAG, "User is not logged in");
        }
        try {
            if (user != null) {
                mongoClient = user.getMongoClient("mongodb-atlas");
                mongoDatabase = mongoClient.getDatabase("db");
                mongoCollection = mongoDatabase.getCollection("order");
                Log.d(TAG, "MongoDB connection established successfully.");
            } else {
                Toast.makeText(this, "User is not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to MongoDB: " + e.getMessage());
            Toast.makeText(this, "Error connecting to MongoDB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUsername = prefs.getString(KEY_USERNAME, null);

        orderDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(client.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                                orderDateEditText.setText(selectedDate);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"High", "Mid", "Low"});
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedDate = orderDateEditText.getText().toString();
                String selectedPriority = prioritySpinner.getSelectedItem().toString();

//
//                int selectedPriority = 0;
//                String priorityString = prioritySpinner.getSelectedItem().toString();
//                switch (priorityString) {
//                    case "High":
//                        selectedPriority = 1;
//                        break;
//                    case "Mid":
//                        selectedPriority = 2;
//                        break;
//                    case "Low":
//                        selectedPriority = 3;
//                        break;
//                }


                String workassign = orders.getText().toString();
                if (savedUsername != null && !selectedDate.isEmpty() && !selectedPriority.isEmpty()) {
                    Document orderDocument = new Document("username", savedUsername)
                            .append("order", workassign)
                            .append("date", selectedDate)
                            .append("priority", selectedPriority)
                            .append("status", 0);

                    try {
                        progressBar.setVisibility(View.VISIBLE);
                        mongoCollection.insertOne(orderDocument).getAsync(result -> {
                            if (result.isSuccess()) {
                                Toast.makeText(client.this, "Order submitted successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                String errorMessage = result.getError().toString();
                                Toast.makeText(client.this, "Order submission failed: " + errorMessage, Toast.LENGTH_LONG).show();
                                Log.e("MongoDB Insert Error", "Failed to insert document: " + errorMessage, result.getError().getException());
                            }
                            progressBar.setVisibility(View.GONE);
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Exception during MongoDB insert: " + e.getMessage());
                        Toast.makeText(client.this, "Order submission failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }

                } else {
                    Toast.makeText(client.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
