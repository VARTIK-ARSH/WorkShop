package com.example.workshop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class clients_order extends AppCompatActivity {

    private static final String TAG = "ClientActivity";
    private RecyclerView orderRecyclerView;
    private ClientOrderAdapter clientOrderAdapter;

    private ImageView add;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> mongoCollection;
    private User user;

    private static final String APP_ID = BuildConfig.APP_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clients_order);

        add=findViewById(R.id.add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(clients_order.this, client.class));            }
        });

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
        mongoCollection.find().iterator().getAsync(result -> {
            if (result.isSuccess()) {
                MongoCursor<Document> cursor = result.get();
                List<Document> orderList = new ArrayList<>();

                while (cursor.hasNext()) {
                    orderList.add(cursor.next());
                }

                runOnUiThread(() -> {
                    clientOrderAdapter = new ClientOrderAdapter(orderList);
                    orderRecyclerView.setAdapter(clientOrderAdapter);
                });
            } else {
                Log.e(TAG, "Failed to fetch orders: " + result.getError().toString());
                runOnUiThread(() -> {
                    Toast.makeText(clients_order.this, "Failed to fetch orders", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
