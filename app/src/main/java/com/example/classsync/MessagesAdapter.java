package com.example.classsync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context context ;
    ArrayList<Messages> messagesArrayList;

    public MessagesAdapter(Context context, ArrayList<Messages> messagesArrayList) {
        this.context = context;
        this.messagesArrayList = messagesArrayList;
    }

    int ITEM_SEND = 1;
    int ITEM_RECEIVED = 2;

    @NonNull

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == ITEM_SEND){
            View view = LayoutInflater.from(context).inflate(R.layout.sender_chat_layout,parent,false);
            return new SenderViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.reciver_chat_layout,parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages messages = messagesArrayList.get(position);
        if (holder.getClass() == SenderViewHolder.class ){
            SenderViewHolder viewHolder = (SenderViewHolder)holder;
            viewHolder.textViewMessage.setText(messages.getMessage());
            viewHolder.textViewTime.setText(messages.getCurrentTime());
        }else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.textViewMessage.setText(messages.getMessage());
            viewHolder.textViewTime.setText(messages.getCurrentTime());
        }

    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Messages messages = messagesArrayList.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.senderId)){
            return ITEM_SEND;
        }else return ITEM_RECEIVED;
    }

    class SenderViewHolder extends RecyclerView.ViewHolder{
        TextView textViewMessage;
        TextView textViewTime;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.senderMessage);
            textViewTime = itemView.findViewById(R.id.timeOfMessage);

        }
    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder{
        TextView textViewMessage;
        TextView textViewTime;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.senderMessage);
            textViewTime = itemView.findViewById(R.id.timeOfMessage);

        }
    }

}
