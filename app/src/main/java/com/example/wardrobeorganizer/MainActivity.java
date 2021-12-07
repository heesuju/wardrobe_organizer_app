package com.example.wardrobeorganizer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    String NOTIFICATION_CHANNEL_ID = "wardrobe_channel";
    static final int GET_STRING = 1;
    DatabaseHelper helper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        Button btnAdd = (Button) findViewById(R.id.button_add);
        Button btnView = (Button) findViewById(R.id.button_view);
        Button btnTakeOut = (Button) findViewById(R.id.button_take_out);
        Button btnPutAway = (Button) findViewById(R.id.button_put_away);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("ACTION_REQUEST", "CREATE");
                startActivityForResult(intent, GET_STRING);
            }
        });
        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                intent.putExtra("ACTION_REQUEST", "VIEW");
                startActivityForResult(intent, GET_STRING);
            }
        });
        btnTakeOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                intent.putExtra("ACTION_REQUEST", "TAKE_OUT");
                startActivityForResult(intent, GET_STRING);
            }
        });
        btnPutAway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                intent.putExtra("ACTION_REQUEST", "PUT_AWAY");
                startActivityForResult(intent, GET_STRING);
            }
        });
        helper = new DatabaseHelper(this);
        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = helper.getReadableDatabase();
        }
        createNotificationChannel();
        checkThingsToPutAway();
        checkThingsToTakeOut();
    }

    private void checkThingsToPutAway() {
        java.util.Date date= new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);

        TimeZone tz = TimeZone.getDefault();
        long time = System.currentTimeMillis() - ((86400000L*365L) + System.currentTimeMillis() % 86400000);
        time -= tz.getOffset(time);
        long start = 86400000L;

        Cursor cursor;

        if(month >= 4 && month <= 8){
            String[] sArr = {"FW", "옷장", "옷장"};
            String filter = "((season = ? AND state = ?) OR (worn BETWEEN " + start + " AND " + time + " AND state = ?))";
            String query = "SELECT * FROM wardrobe WHERE " + filter;
            cursor = db.rawQuery(query, sArr);
        }else{
            String[] sArr = {"옷장"};
            String filter = "worn BETWEEN " + start + " AND " + time + " AND state = ?";
            String query = "SELECT * FROM wardrobe WHERE " + filter.toString();
            cursor = db.rawQuery(query, sArr);
        }
        if(cursor.getCount() > 0){
            sendNotification("정리 필요한 항목 발견", "정리 필요한 항목이 " + cursor.getCount() + "건 있습니다", 1);
        }
    }

    private void checkThingsToTakeOut() {
        java.util.Date date= new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);

        TimeZone tz = TimeZone.getDefault();
        long time = System.currentTimeMillis() - ((86400000L*365L) + System.currentTimeMillis() % 86400000);
        time -= tz.getOffset(time);

        Cursor cursor;

        if(month >= 9 || (month >= 1 && month <= 2 )){
            String[] sArr = {"FW", "정리"};
            String filter = "((season = ? AND state = ?) AND (worn BETWEEN " + time + " AND " + System.currentTimeMillis() + "))";
            String query = "SELECT * FROM wardrobe WHERE " + filter;
            cursor = db.rawQuery(query, sArr);
            if(cursor.getCount() > 0){
                sendNotification("꺼낼 항목 발견", "정리함에서 꺼낼 항목이 " + cursor.getCount() + "건 있습니다", 2);
            }
        }
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Channel description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void sendNotification(String title, String message, int id) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Intent intent = new Intent(this, ListActivity.class);
        if(id<=1){
            intent.putExtra("ACTION_REQUEST", "PUT_AWAY");
        }else{
            intent.putExtra("ACTION_REQUEST", "TAKE_OUT");
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, id);

        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(message);
        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notificationBuilder.build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_STRING && resultCode == RESULT_OK) {

            switch (data.getStringExtra("ACTION_RESULT")) {
                case "CREATE":
                    db.execSQL("INSERT INTO wardrobe VALUES (null, " +
                            "'" + data.getStringExtra("INPUT_CATEGORY") + "', " +
                            "'" + data.getStringExtra("INPUT_SEASON") + "', " +
                            "'" + data.getStringExtra("INPUT_BRAND") + "', " +
                            "'" + data.getStringExtra("INPUT_STATE") + "', " +
                            "'" + data.getStringExtra("INPUT_IMAGE") + "', " +
                            "'" + data.getStringExtra("INPUT_COLOR") + "', " +
                            "'" + data.getLongExtra("INPUT_WORN", 0) + "');"
                    );
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}