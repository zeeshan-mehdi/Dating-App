package com.dating.needtodate.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dating.needtodate.DataModel.ConnectedUsers;
import com.dating.needtodate.MainActivity;
import com.dating.needtodate.OpenTokConfig;
import com.dating.needtodate.R;
import com.dating.needtodate.WebServiceCoordinator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class PeerToPeerVideoChatFragment extends Fragment implements EasyPermissions.PermissionCallbacks,
        WebServiceCoordinator.Listener,
        Session.SessionListener,
        PublisherKit.PublisherListener,
        SubscriberKit.SubscriberListener {

    private boolean isSender = false;
    Dialog dialog;

    private FirebaseDatabase firebaseDatabase;

    private LinearLayout linearLayout;

    private Context context;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    // Suppressing this warning. mWebServiceCoordinator will get GarbageCollected if it is local.
    @SuppressWarnings("FieldCanBeLocal")
    private WebServiceCoordinator mWebServiceCoordinator;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;
    PowerManager.WakeLock wl;

    TextView name;
    private CircleImageView profileImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.peer_to_peer_video_chat_fragment, container, false);

        linearLayout= view.findViewById(R.id.top);

        profileImage = view.findViewById(R.id.profile_image);

        context = getActivity();

        // initialize view objects from your layout
        mPublisherViewContainer = (FrameLayout) view.findViewById(R.id.publisher_container);
        mSubscriberViewContainer = (FrameLayout) view.findViewById(R.id.subscriber_container);

        mPublisherViewContainer.setVisibility(View.GONE);
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "wake:");
        firebaseDatabase = FirebaseDatabase.getInstance();

        name = view.findViewById(R.id.txtName);

        showProgressDialog();

        requestPermissions();

        return view;

    }

    @Override
    public void onPause() {

        Log.d(LOG_TAG, "onPause");

        super.onPause();

        if (mSession != null) {
            mSession.onPause();
        }

    }

    public void writeToDB(String connectionId,FirebaseUser fUser){
        try {


            String imageUrl = fUser.getPhotoUrl().toString();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sessions");

            ConnectedUsers connectedUser = new ConnectedUsers(imageUrl,fUser.getDisplayName());

            reference.child(connectionId).child(fUser.getUid()).setValue(connectedUser).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.e("user", "is succesffuly on call");
                } else {
                    Log.e("user", "failed to mark on Call"+task.getException());
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {

        Log.d(LOG_TAG, "onResume");

        super.onResume();

        if (mSession != null) {
            mSession.onResume();
           // writeToDB(mSession,FirebaseAuth.getInstance().getCurrentUser());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        Log.d(LOG_TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        Log.d(LOG_TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setRationale(getString(R.string.rationale_ask_again))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel))
                    .setRequestCode(RC_SETTINGS_SCREEN_PERM)
                    .build()
                    .show();
        }
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {

        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            // if there is no server URL set
            if (OpenTokConfig.CHAT_SERVER_URL == null) {
                // use hard coded session values
                if (OpenTokConfig.areHardCodedConfigsValid()) {
                    initializeSession(OpenTokConfig.API_KEY, OpenTokConfig.SESSION_ID, OpenTokConfig.TOKEN);
                } else {
                    showConfigError("Configuration Error", OpenTokConfig.hardCodedConfigErrorMessage);
                }
            } else {
                // otherwise initialize WebServiceCoordinator and kick off request for session data
                // session initialization occurs once data is returned, in onSessionConnectionDataReady
                if (OpenTokConfig.isWebServerConfigUrlValid()) {
                    mWebServiceCoordinator = new WebServiceCoordinator(getActivity(), this);
                    mWebServiceCoordinator.fetchSessionConnectionData(OpenTokConfig.SESSION_INFO_ENDPOINT);
                } else {
                    showConfigError("Configuration Error", OpenTokConfig.webServerConfigErrorMessage);
                }
            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }

    private void initializeSession(String apiKey, String sessionId, String token) {

        mSession = new Session.Builder(getActivity(), apiKey, sessionId).build();
        mSession.setSessionListener(this);
        mSession.connect(token);
    }

    /* Web Service Coordinator delegate methods */

    @Override
    public void onSessionConnectionDataReady(String apiKey, String sessionId, String token) {

        Log.d(LOG_TAG, "ApiKey: " + apiKey + " SessionId: " + sessionId + " Token: " + token);
        initializeSession(apiKey, sessionId, token);
    }

    @Override
    public void onWebServiceCoordinatorError(Exception error) {

        Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
        Toast.makeText(getActivity(), "Web Service error: " + error.getMessage(), Toast.LENGTH_LONG).show();

        getActivity().finish();

    }

    /* Session Listener methods */

    @Override
    public void onConnected(Session session) {
        hideDialog();

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        wl.acquire();
        Log.d(LOG_TAG, "onConnected: Connected to session: " + session.getSessionId());

        // initialize Publisher and set this object to listen to Publisher events
        mPublisher = new Publisher.Builder(getActivity()).build();
        mPublisher.setPublisherListener(this);

        // set publisher video style to fill view
        mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);


        mSubscriberViewContainer.addView(mPublisher.getView());
        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);


        //writeToDB(session.getConnection().getConnectionId(),fUser);
    }

    @Override
    public void onDisconnected(Session session) {

        Log.d(LOG_TAG, "onDisconnected: Disconnected from session: " + session.getSessionId());
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        Log.d(LOG_TAG, "onStreamReceived: New Stream Received " + stream.getStreamId() + " in session: " + session.getSessionId());

        if (mSubscriber == null) {

            writeToDB(session.getSessionId(),fUser);
            mSubscriberViewContainer.removeAllViews();
            mPublisherViewContainer.setVisibility(View.VISIBLE);
            mPublisherViewContainer.addView(mPublisher.getView());
            mSubscriber = new Subscriber.Builder(getActivity(), stream).build();
            mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
            mSubscriber.setSubscriberListener(this);
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());

            setProfileImage(session.getSessionId(),fUser);
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

        Log.d(LOG_TAG, "onStreamDropped: Stream Dropped: " + stream.getStreamId() + " in session: " + session.getSessionId());

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
            mPublisherViewContainer.removeAllViews();


            mPublisherViewContainer.setVisibility(View.GONE);

            mSubscriberViewContainer.addView(mPublisher.getView());

            deleteSession(session.getSessionId());

        }
    }

    private void deleteSession(String sessionId) {
        try {
            firebaseDatabase.getReference("Sessions").child(sessionId).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.e("session", "removed successfully");
                            } else {
                                Log.e("session", "could not be removed");
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(LOG_TAG, "onError: " + opentokError.getErrorDomain() + " : " +
                opentokError.getErrorCode() + " - " + opentokError.getMessage() + " in session: " + session.getSessionId());

        showOpenTokError(opentokError);
    }

    /* Publisher Listener methods */

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

       // writeToDB( publisherKit.getStream().getStreamId(),FirebaseAuth.getInstance().getCurrentUser());

        isSender = true;


        Log.d(LOG_TAG, "onStreamCreated: Publisher Stream Created. Own stream " + stream.getStreamId());

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

        Log.d(LOG_TAG, "onStreamDestroyed: Publisher Stream Destroyed. Own stream " + stream.getStreamId());
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

        Log.e(LOG_TAG, "onError: " + opentokError.getErrorDomain() + " : " +
                opentokError.getErrorCode() + " - " + opentokError.getMessage());

        showOpenTokError(opentokError);
    }

    @Override
    public void onConnected(SubscriberKit subscriberKit) {

       // writeToDB(subscriberKit.getStream().getStreamId(),FirebaseAuth.getInstance().getCurrentUser());


        Log.d(LOG_TAG, "onConnected: Subscriber connected. Stream: " + subscriberKit.getStream().getStreamId());
    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {

        Log.d(LOG_TAG, "onDisconnected: Subscriber disconnected. Stream: " + subscriberKit.getStream().getStreamId());
    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {

        Log.e(LOG_TAG, "onError: " + opentokError.getErrorDomain() + " : " +
                opentokError.getErrorCode() + " - " + opentokError.getMessage());

        showOpenTokError(opentokError);
    }

    private void showOpenTokError(OpentokError opentokError) {
        getActivity().finish();
    }

    private void showConfigError(String alertTitle, final String errorMessage) {
        Log.e(LOG_TAG, "Error " + alertTitle + ": " + errorMessage);
        new AlertDialog.Builder(getActivity())
                .setTitle(alertTitle)
                .setMessage(errorMessage)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onStop() {
        try {
            wl.release();
            super.onStop();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setProfileImage(String sessionId,FirebaseUser fUser){
        try {


            DatabaseReference reference = firebaseDatabase.getReference("Sessions");

            reference.child(sessionId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String key = ds.getKey();

                        ConnectedUsers user = ds.getValue(ConnectedUsers.class);

                        if (key!=null&&!key.equals(fUser.getUid())) {
                            Glide.with(context).load(user.url).into(profileImage);

                            String userName = user.name;
                            if(name!=null&&name.length()>10){
                                userName = userName.substring(0,10)+"..";
                            }

                            name.setText(userName);
                            linearLayout.bringToFront();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void showProgressDialog(){
        dialog = new  Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }



}
