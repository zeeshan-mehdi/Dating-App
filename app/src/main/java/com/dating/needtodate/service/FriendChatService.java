package com.dating.needtodate.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import com.dating.needtodate.MessageActivity;
import com.dating.needtodate.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.Query;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FriendChatService extends Service {
    private static String TAG = "FriendChatService";
    // Binder given to clients
    public final IBinder mBinder = new LocalBinder();
    public Map<String, Boolean> mapMark;
    public Map<String, Query> mapQuery;
    public Map<String, ChildEventListener> mapChildEventListenerMap;
    public Map<String, Bitmap> mapBitmap;
    public ArrayList<String> listKey;
    public CountDownTimer updateOnline;

    public FriendChatService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();

    }

    public void stopNotify(String id) {
        mapMark.put(id, false);
    }

    public static void createNotify(Context context,String name, String content, int id, Bitmap icon) {
        Intent activityIntent = new Intent(context, MessageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new
                NotificationCompat.Builder(context)
                .setLargeIcon(icon)
                .setContentTitle(name)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[] { 1000, 1000})
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setAutoCancel(true);

//        Bitmap image =null;
//        try {
//            ParcelFileDescriptor parcelFileDescriptor =
//                    getContentResolver().openFileDescriptor(uri, "r");
//            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
//            image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
//
//
//            parcelFileDescriptor.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//            if(image!=null)
            notificationBuilder.setSmallIcon(R.drawable.ic_launcher);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
        notificationManager.notify(id,
                notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "OnStartService");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "OnBindService");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (String id : listKey) {
            mapQuery.get(id).removeEventListener(mapChildEventListenerMap.get(id));
        }
        mapQuery.clear();
        mapChildEventListenerMap.clear();
        mapBitmap.clear();
        updateOnline.cancel();
        Log.d(TAG, "OnDestroyService");
    }

    public class LocalBinder extends Binder {
        public FriendChatService getService() {
            // Return this instance of LocalService so clients can call public methods
            return FriendChatService.this;
        }
    }
}
