package com.dating.needtodate.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dating.needtodate.Adapters.UserAdapter;
import com.dating.needtodate.DataModel.User;
import com.dating.needtodate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Contacts extends Fragment {

    RecyclerView recyclerView;
    ArrayList<User> users;

    UserAdapter userAdapter;
    FirebaseDatabase firebaseDatabase;

    FirebaseAuth auth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        getActivity().setTitle("Chat");

        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        recyclerView = view.findViewById(R.id.userRecyclerView);

        users = new ArrayList<>();

        auth = FirebaseAuth.getInstance();



        firebaseDatabase = FirebaseDatabase.getInstance();

        getUsers();



        return view;
    }

    private void getUsers() {
        DatabaseReference ref= firebaseDatabase.getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();

                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    if(user.id!=null && !user.id.equals(auth.getCurrentUser().getUid())){
                        users.add(user);
                    }

                }

                if(users.size()!=0) {

                    userAdapter = new UserAdapter(getContext(), users);

                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


}
