package com.example.workshop;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
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

public class worker extends AppCompatActivity {

    private static final String TAG = "ClientActivity";
    private RecyclerView orderRecyclerView;
    private ProgressBar progressBar;

    private MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    private MongoCollection<Document> mongoCollection;
    private User user;

    private static final String APP_ID = BuildConfig.APP_ID;

    private Handler handler;
    private Runnable fetchOrdersRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker);

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
                setupAutoRefresh();
            } else {
                Toast.makeText(this, "User is not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to MongoDB: " + e.getMessage());
            Toast.makeText(this, "Check network", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAutoRefresh() {
        handler = new Handler(Looper.getMainLooper());

        fetchOrdersRunnable = new Runnable() {
            @Override
            public void run() {
                fetchOrdersFromMongoDB();
                handler.postDelayed(this, 5000);
            }
        };

        handler.post(fetchOrdersRunnable);
    }

    private void fetchOrdersFromMongoDB() {
        Document filter = new Document("status", new Document("$ne", 4));

        // Sorting priority: High -> Mid -> Low, and if same, sort by dueDate (ascending)
        Document sort = new Document("priority_num",-1)   // Sort by priority: high > mid > low (-1 for descending order)
                .append("dueDate", 1);  // Sort by due date in ascending order (1 for ascending)

        // Fetch documents matching the query and sorting criteria
        mongoCollection.find(filter).sort(sort).iterator().getAsync(result -> {
            if (result.isSuccess()) {
                MongoCursor<Document> cursor = result.get();
                List<Document> orderList = new ArrayList<>();

                while (cursor.hasNext()) {
                    Document orderDocument = cursor.next();
                    orderList.add(orderDocument);
                }

                // Update RecyclerView with the fetched and sorted orders
                runOnUiThread(() -> {
                    WorkerOrder adapter = new WorkerOrder(orderList, worker.this);
                    orderRecyclerView.setAdapter(adapter);
                });
            } else {
                // Handle the failure
                Log.e(TAG, "Failed to fetch orders: " + result.getError().toString());
                runOnUiThread(() -> {
                    Toast.makeText(worker.this, "Failed to fetch orders", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && fetchOrdersRunnable != null) {
            handler.removeCallbacks(fetchOrdersRunnable);
        }
    }
}
