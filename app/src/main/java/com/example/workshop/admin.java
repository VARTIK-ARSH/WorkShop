package com.example.workshop;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class admin extends AppCompatActivity {

    private static final String TAG = "ClientActivity";
    private RecyclerView orderRecyclerView;
    private OrderAdapter orderAdapter;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> mongoCollection;
    private User user;

    private static final String APP_ID = BuildConfig.APP_ID;
    private Handler handler = new Handler();
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        orderRecyclerView = findViewById(R.id.order_recycler_view);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        App app = new App(new AppConfiguration.Builder(APP_ID).build());

        user = app.currentUser();

        try {
            if (user != null) {
                mongoClient = user.getMongoClient("mongodb-atlas");
                mongoDatabase = mongoClient.getDatabase("db");
                mongoCollection = mongoDatabase.getCollection("order");

                Log.d(TAG, "MongoDB connection established successfully.");
                fetchOrdersFromMongoDB();
                setupAutoRefresh();
            } else {
                Toast.makeText(this, "User is not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to MongoDB: " + e.getMessage());
            Toast.makeText(this, "Error connecting to MongoDB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchOrdersFromMongoDB() {
        Document sort = new Document("priority_num",-1)
                .append("dueDate", 1);
        mongoCollection.find().sort(sort).iterator().getAsync(result -> {
            if (result.isSuccess()) {
                MongoCursor<Document> cursor = result.get();
                List<Document> orderList = new ArrayList<>();

                while (cursor.hasNext()) {
                    Document orderDocument = cursor.next();
                    orderList.add(orderDocument);
                }

                runOnUiThread(() -> {
                    if (orderAdapter == null) {
                        orderAdapter = new OrderAdapter(orderList, mongoCollection, admin.this);
                        orderRecyclerView.setAdapter(orderAdapter);
                    } else {
                        orderAdapter.updateOrders(orderList);
                    }
                });
            } else {
                Log.e(TAG, "Failed to fetch orders: " + result.getError().toString());
                runOnUiThread(() -> {
                    Toast.makeText(admin.this, "Failed to fetch orders", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                fetchOrdersFromMongoDB();
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(refreshRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(refreshRunnable);
    }
}
