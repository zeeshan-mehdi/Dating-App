package com.dating.needtodate;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dating.needtodate.Adapters.ChatAdapter;
import com.dating.needtodate.DataModel.Message;
import com.dating.needtodate.DataModel.User;
import com.dating.needtodate.service.FriendChatService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profilePic;

    TextView txtUserName,txtMessage,txtUserOnline;

    ImageButton sendButton;

    Toolbar toolbar;
    String id;

    FirebaseDatabase firebaseDatabase;

    DatabaseReference ref;

    FirebaseAuth mAuth;

    ArrayList<Message> messageArrayList;

    RecyclerView recyclerView;


    public static boolean call_established = false;
    boolean isError = false;

    Map<String, String> headers;


    // APIService apiService;

    boolean notify = false;



    //sinch

    private static final String APP_KEY = "72240561-46fb-4522-951a-57df57248aed";
    private static final String APP_SECRET = "gaafusACp0yAse+4g9NyJw==";
    private static final String ENVIRONMENT = "clientapi.sinch.com";


    String receiver_name;

    ImageButton makeCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        id = getIntent().getStringExtra("userId");

        receiver_name = getIntent().getStringExtra("receiver");

        makeCall = findViewById(R.id.makeCall);
        profilePic = findViewById(R.id.userProfileIcon);

        txtUserName = findViewById(R.id.txtUserName);

        toolbar = findViewById(R.id.toolbar);

        txtMessage = findViewById(R.id.messageText);


        txtUserOnline = findViewById(R.id.userisOnline);


        mAuth = FirebaseAuth.getInstance();
        sendButton = findViewById(R.id.btnSend);

        messageArrayList = new ArrayList<>();

        recyclerView = findViewById(R.id.messagesRecyclerView);


       // apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = txtMessage.getText().toString();

                if(!message.equals("")){

                    Message chat = new Message(mAuth.getCurrentUser().getUid(),id,message,false);

                    sendMessage(chat);

                }else{
                    Toast.makeText(MessageActivity.this, "Can't Send Empty Message", Toast.LENGTH_SHORT).show();
                }


                txtMessage.setText("");

            }
        });


        try {


            setSupportActionBar(toolbar);


            setTitle("");


            getSupportActionBar().setDisplayHomeAsUpEnabled(true);



            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }finally {
           getUserInfo();

        }

        makeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // sinchClient.getCallClient().callUser(id,headers);
            }
        });





    }

    private void sendMessage(final Message chat) {

        DatabaseReference  ref = firebaseDatabase.getReference();

        ref.child("chats").push().setValue(chat).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MessageActivity.this, "We failed to Send Message...", Toast.LENGTH_SHORT).show();
            }
        });

        final  String msg = chat.message;

        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

//                if(notify)
//                    FriendChatService.createNotify(getApplicationContext(),user.displayName,chat.reciever,msg,0, BitmapFactory.decodeResource(getResources(),R.drawable.ic_account_circle_black_24dp));
//                notify= false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }



    private void getUserInfo() {
        firebaseDatabase = FirebaseDatabase.getInstance();

        ref = firebaseDatabase.getReference("Users").child(id);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {


                    User user = dataSnapshot.getValue(User.class);


                    assert user != null;
                    if (user.profileImage != null)
                        Glide.with(getApplicationContext()).load(user.profileImage).into(profilePic);
                    else
                        profilePic.setImageResource(R.drawable.placeholder);
                    txtUserName.setText(user.displayName);

                    txtUserOnline.setText(user.status);


                    getMessages(mAuth.getCurrentUser().getUid());

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    public  void getMessages(final String userId){
        DatabaseReference reference = firebaseDatabase.getReference("chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageArrayList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){


                    Message chat = ds.getValue(Message.class);


                    if(chat!=null && (chat.sender.equals(userId)&& chat.reciever.equals(id)||chat.reciever.equals(userId)&&chat.sender.equals(id))){
                        messageArrayList.add(chat);
                    }


                }

                if(messageArrayList.size()!=0) {

                    ChatAdapter chatAdapter = new ChatAdapter(getApplicationContext(), messageArrayList);

                    recyclerView.setAdapter(chatAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }







}
