package com.example.workshop;

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

import java.util.List;

import io.realm.mongodb.mongo.MongoCollection;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Document> orderList;
    private MongoCollection<Document> mongoCollection;
    private Context context;

    public OrderAdapter(List<Document> orderList, MongoCollection<Document> mongoCollection, Context context) {
        this.orderList = orderList;
        this.mongoCollection = mongoCollection;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_item_layout, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Document orderDocument = orderList.get(position);
        String orderDetails = "Username: " + orderDocument.getString("username") + "\n" +
                "Order: " + orderDocument.getString("order") + "\n" +
                "Date: " + orderDocument.getString("date") + "\n" +
                "Priority: " + orderDocument.getString("priority");
        holder.orderDetailsTextView.setText(orderDetails);

        int statusValue = orderDocument.getInteger("status", 0);
        String statusText;
        int statusColor;

        switch (statusValue) {
            case 1:
                statusText = "Accepted";
                statusColor = context.getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case 2:
                statusText = "Rejected";
                statusColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            case 4:
                statusText = "DONE";
                statusColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            default:
                statusText = "Not Viewed";
                statusColor = context.getResources().getColor(android.R.color.darker_gray);
                break;
        }

        holder.statusTextView.setText(statusText);
        holder.statusTextView.setTextColor(statusColor);

        holder.acceptButton.setOnClickListener(v -> updateOrderStatus(orderDocument, 1)); // Status 1 = Accepted

        holder.rejectButton.setOnClickListener(v -> updateOrderStatus(orderDocument, 2)); // Status 2 = Rejected
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView orderDetailsTextView;
        TextView statusTextView; // Add this line
        Button acceptButton, rejectButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDetailsTextView = itemView.findViewById(R.id.order_details_text);
            statusTextView = itemView.findViewById(R.id.status); // Add this line
            acceptButton = itemView.findViewById(R.id.accept_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }
    }

    private void updateOrderStatus(Document orderDocument, int newStatus) {
        Document query = new Document("_id", orderDocument.getObjectId("_id"));
        Document update = new Document("$set", new Document("status", newStatus));

        mongoCollection.updateOne(query, update).getAsync(result -> {
            if (result.isSuccess()) {
                String statusMessage = (newStatus == 1) ? "Order Accepted" : "Order Rejected";
                Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to update order status", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void updateOrders(List<Document> newOrderList) {
        this.orderList.clear();
        this.orderList.addAll(newOrderList);
        notifyDataSetChanged();
    }
}
