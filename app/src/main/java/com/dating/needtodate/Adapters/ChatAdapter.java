package com.dating.needtodate.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dating.needtodate.DataModel.Message;
import com.dating.needtodate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.Holder> {
    static ArrayList<Message> messages;
    Context context;


    public ChatAdapter(Context ctx, ArrayList<Message> messages){
        context = ctx;
        ChatAdapter.messages = messages;
    }

    @NonNull
    @Override
    public ChatAdapter.Holder onCreateViewHolder(@NonNull ViewGroup viewGroup,int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);

        View view;


        if(viewType==1){
            view = inflater.inflate(R.layout.chat_row_right,viewGroup,false);
        }else {
            view = inflater.inflate(R.layout.chat_row_left,viewGroup,false);
        }

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.Holder holder, int i) {
        Message chat = messages.get(i);

        final ImageView view = holder.userProfilePic;


            holder.userMessage.setText(chat.message);


            if(getItemViewType(i)==0) {

                FirebaseDatabase.getInstance().getReference("Users").child(chat.sender).child("profilePicUrl").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(view!=null)
                            Glide.with(context).load(dataSnapshot.getValue()).into(view);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

    }

    @Override
    public int getItemViewType(int position) {

        Log.e("called","getItemviewtype()");

        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = auth.getCurrentUser();

        String id = firebaseUser.getUid();

        Log.e("ids",id+"  sender "+messages.get(position).sender);


        String senderId = messages.get(position).sender;



        if(id.equals(senderId)){
            return 1;
        }else{
            return 0;
        }
    }



    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    static class Holder extends  RecyclerView.ViewHolder {

        TextView userMessage;
        CircleImageView userProfilePic;

         Holder(@NonNull View itemView) {
            super(itemView);

            userMessage = itemView.findViewById(R.id.userMessage);


            userProfilePic = itemView.findViewById(R.id.userImage);

        }
    }



}
