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

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Document orderDocument = orderList.get(position);
        holder.usernameTextView.setText("Client Name: " + orderDocument.getString("username"));
        holder.orderTextView.setText("Order Due: " + orderDocument.getString("order"));
        holder.dateTextView.setText("Date: " + orderDocument.getString("date"));
        holder.priorityTextView.setText("Priority: " + orderDocument.getString("priority"));

        int statusValue = orderDocument.getInteger("status", 0);
        String statusText;
        int statusColor;

        switch (statusValue) {
            case 1:
                statusText = "Accepted";
                holder.acceptButton.setVisibility(View.INVISIBLE);
                holder.rejectButton.setVisibility(View.VISIBLE);
                holder.accept_text.setVisibility(View.VISIBLE);
                holder.reject_text.setVisibility(View.VISIBLE);
                statusColor = context.getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case 2:
                statusText = "Rejected";
                holder.acceptButton.setVisibility(View.VISIBLE);
                holder.rejectButton.setVisibility(View.INVISIBLE);
                holder.accept_text.setVisibility(View.VISIBLE);
                holder.reject_text.setVisibility(View.VISIBLE);
                statusColor = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            case 4:
                statusText = "DONE";
                holder.acceptButton.setVisibility(View.INVISIBLE);
                holder.rejectButton.setVisibility(View.INVISIBLE);
                holder.accept_text.setVisibility(View.INVISIBLE);
                holder.reject_text.setVisibility(View.INVISIBLE);
                statusColor = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            default:
                statusText = "Not Viewed";
                holder.acceptButton.setVisibility(View.VISIBLE);
                holder.rejectButton.setVisibility(View.VISIBLE);
                holder.accept_text.setVisibility(View.VISIBLE);
                holder.reject_text.setVisibility(View.VISIBLE);
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

        TextView usernameTextView,orderTextView,dateTextView,priorityTextView,statusButton,accept_text,reject_text;
        TextView statusTextView; // Add this line
        Button acceptButton, rejectButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            orderTextView = itemView.findViewById(R.id.order_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            priorityTextView = itemView.findViewById(R.id.priority_text_view);
            statusButton = itemView.findViewById(R.id.status_button);
            statusTextView = itemView.findViewById(R.id.status);
            accept_text = itemView.findViewById(R.id.accept_text);
            reject_text = itemView.findViewById(R.id.reject_text);
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
