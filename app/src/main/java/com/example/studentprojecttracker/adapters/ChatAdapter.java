package com.example.studentprojecttracker.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.models.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages;
    private Context context;
    private String currentUserId;

    public ChatAdapter(List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        holder.textMessage.setText(msg.getText());

        boolean isSender = msg.getSenderId().equals(currentUserId);

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) holder.messageContainer.getLayoutParams();

        if (isSender) {
            params.gravity = Gravity.END;
            holder.textMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_chat_sender));
        } else {
            params.gravity = Gravity.START;
            holder.textMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_chat_receiver));
        }

        holder.messageContainer.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageContainer;
        TextView textMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            textMessage = itemView.findViewById(R.id.textMessage);
        }
    }
}
