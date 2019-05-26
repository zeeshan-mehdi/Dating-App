package com.dating.needtodate;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.dating.needtodate.Fragments.Contacts;
import com.dating.needtodate.Fragments.InboxFragment;
import com.dating.needtodate.Fragments.PeerToPeerVideoChatFragment;
import com.dating.needtodate.service.ServiceUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);



        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        getSupportFragmentManager().beginTransaction().replace(R.id.container,new Contacts()).commit();


        if(!ServiceUtils.isServiceFriendChatRunning(getApplicationContext())){
            ServiceUtils.startServiceFriendChat(getApplicationContext());
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        Fragment selectedFragment = null;

        switch (menuItem.getItemId()){
            case R.id.chat:
                selectedFragment = new Contacts();
                break;

            case R.id.videoCall:
                selectedFragment = new PeerToPeerVideoChatFragment();
                break;

            case R.id.inbox:
                selectedFragment = new InboxFragment();
                break;

        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container,selectedFragment).commit();



        return true;
    }


    private void status(final String status){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user==null){
            return;
        }


        FirebaseDatabase.getInstance().getReference("Users")
                .child(user.getUid()).child("status").setValue(status).addOnSuccessListener(aVoid -> Log.e("online",status))
                .addOnFailureListener(e -> Log.e("online",status));
    }

    @Override
    protected void onDestroy() {
        status("offline");
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        status("online");
        super.onResume();
    }

}
