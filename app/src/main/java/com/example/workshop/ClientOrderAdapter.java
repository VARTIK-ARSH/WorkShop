package com.example.workshop;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.bson.Document;
import java.util.List;

public class ClientOrderAdapter extends RecyclerView.Adapter<ClientOrderAdapter.ClientOrderViewHolder> {

    private List<Document> orderList;

    public ClientOrderAdapter(List<Document> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ClientOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.client_order_list, parent, false);
        return new ClientOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientOrderViewHolder holder, int position) {
        Document order = orderList.get(position);

        String orderDetails = "Username: " + order.getString("username") + "\n" +
                "Order: " + order.getString("order") + "\n" +
                "Date: " + order.getString("date") + "\n" +
                "Priority: " + order.getString("priority");
        holder.orderDetailsTextView.setText(orderDetails);

        int status = order.getInteger("status", 0);
        String statusText;
        int statusColor;

        switch (status) {
            case 0:
                statusText = "Not Viewed";
                statusColor = Color.GRAY;
                break;
            case 1:
                statusText = "Accepted";
                statusColor = Color.BLUE;
                break;
            case 2:
                statusText = "Rejected";
                statusColor = Color.RED;
                break;
            case 4:
                statusText = "Completed";
                statusColor = Color.GREEN;
                break;
                default:
                statusText = "Unknown Status";
                statusColor = Color.GRAY;
        }

        holder.orderStatusTextView.setText(statusText);
        holder.orderStatusTextView.setTextColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class ClientOrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderDetailsTextView;
        TextView orderStatusTextView;

        public ClientOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDetailsTextView = itemView.findViewById(R.id.order_details_text);
            orderStatusTextView = itemView.findViewById(R.id.order_status_text);
        }
    }
}
