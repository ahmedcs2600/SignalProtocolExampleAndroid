package com.app.signalprotocolimplementation.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.signalprotocolimplementation.R;
import com.app.signalprotocolimplementation.ui.single.Message;

import java.util.ArrayList;
import java.util.List;

public class MessagingAdapter extends RecyclerView.Adapter<MessagingAdapter.ViewHolder> {

    private List<Message> messages = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.onBind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView messageTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message);

        }

        public void onBind(Message message) {
            messageTextView.setText(message.getMessage());
        }
    }

    public void onNewMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

}
