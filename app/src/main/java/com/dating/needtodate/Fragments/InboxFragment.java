package com.dating.needtodate.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dating.needtodate.LoginActivity;
import com.dating.needtodate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class InboxFragment extends Fragment {

    TextView txtUserName,profileUser,txtEMail,txtPassword;

    CircleImageView profileImage;

    Button btnLogout;

    private Context mContext;

    // Initialise it from onAttach()
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.inbox_fragment,container,false);


        txtUserName = view.findViewById(R.id.txtProfileUser);

        profileUser = view.findViewById(R.id.txtProfileUserName);


        txtEMail = view.findViewById(R.id.txtProfileEMail);

        txtPassword = view.findViewById(R.id.txtProfilePassword);

        btnLogout = view.findViewById(R.id.btnLogout);

        profileImage = view.findViewById(R.id.profileImage);

        mContext = getActivity();
        loadUserInfo();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    Intent intent = new Intent(getActivity(), LoginActivity.class);

                    startActivity(intent);

                    FirebaseAuth.getInstance().signOut();
                    getActivity().finish();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });



        return view;
    }

    public void loadUserInfo(){

        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            txtUserName.setText(user.getDisplayName());

            txtEMail.setText(user.getEmail());

            Uri uri = user.getPhotoUrl();


            profileUser.setText(user.getDisplayName());

            Log.e("url",user.getPhotoUrl().toString());


            if(mContext==null){
                Toast.makeText(mContext, "context is null", Toast.LENGTH_SHORT).show();
            }

            Glide.with(mContext).load(uri).into(profileImage);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
