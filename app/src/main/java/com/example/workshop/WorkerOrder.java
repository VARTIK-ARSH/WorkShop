package com.example.workshop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.List;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class WorkerOrder extends RecyclerView.Adapter<WorkerOrder.OrderViewHolder> {
    private final List<Document> orderList;
    private final Context context;

    public WorkerOrder(List<Document> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Document orderDocument = orderList.get(position);
        holder.usernameTextView.setText("Client Name: " + orderDocument.getString("username"));
        holder.orderTextView.setText("Order Due: " + orderDocument.getString("order"));
        holder.dateTextView.setText("Date: " + orderDocument.getString("date"));
        holder.priorityTextView.setText("Priority: " + orderDocument.getString("priority"));

        holder.statusButton.setOnClickListener(v -> {
            changeOrderStatus(orderDocument);
        });
    }

    private void changeOrderStatus(Document orderDocument) {
        // Get the ObjectId from the order document
        ObjectId orderId = orderDocument.getObjectId("_id"); // Use getObjectId instead of getInteger

        // Update the status field in the document
        orderDocument.put("status", 4); // Change the status to 4

        // Update the document in MongoDB
        MongoDatabase mongoDatabase = ((worker) context).mongoDatabase;
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("order");

        // Use the ObjectId to find the document
        mongoCollection.updateOne(new Document("_id", orderId), new Document("$set", new Document("status", 4)))
                .getAsync(updateResult -> {
                    if (updateResult.isSuccess()) {
                        // Successfully updated the status
                        Toast.makeText(context, "Order status updated", Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle the failure
                        Toast.makeText(context, "Failed to update order status", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, orderTextView, dateTextView, priorityTextView;
        Button statusButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            orderTextView = itemView.findViewById(R.id.order_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            priorityTextView = itemView.findViewById(R.id.priority_text_view);
            statusButton = itemView.findViewById(R.id.status_button);
        }
    }
}
