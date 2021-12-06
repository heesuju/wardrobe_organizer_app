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
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String NOTIFICATION_CHANNEL_ID = "wardrobe_channel";
    static final int GET_STRING = 1;
    DatabaseHelper helper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button1);
        Button btnView = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
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
        checkSeason();
    }

    private void checkSeason() {
        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        Cursor cursor;
        if(dateFormat.format(date).equals("4") || dateFormat.format(date).equals("5") ||
            dateFormat.format(date).equals("8")){
            List<String> winter_materials = Arrays.asList(getResources().getStringArray(R.array.winter_material_array));
            List<String> list = new ArrayList<>();


            for(String e:winter_materials){
                list.add(e);
                list.add("옷장");
            }

            String[] sArr = new String[list.size()];
            sArr = list.toArray(sArr);

            StringBuilder filter = new StringBuilder(100);
            for (int idx=0; idx<winter_materials.size(); idx++) {
                if(idx>0){
                    filter.append(" OR ");
                }
                filter.append("(material = ? AND state = ?)");
            }
            //String[] txt = {"정리"};

            String query = "SELECT * FROM wardrobe WHERE " + filter.toString();
            //String query = "SELECT * FROM wardrobe WHERE state = ?";
            //String query = "SELECT" + " (SELECT * FROM wardrobe WHERE " + filter.toString() + ")" + "FROM wardrobe WHERE state = ?";
            cursor = db.rawQuery(query, sArr);
            sendNotification(cursor.getCount());
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

    public void sendNotification(int count) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Intent intent = new Intent(this, ListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
        notificationBuilder.setContentTitle("옷장 정리");
        notificationBuilder.setContentText("옷장에서 정리할 항목이 있습니다." + count);
        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_STRING && resultCode == RESULT_OK) {

            switch (data.getStringExtra("ACTION_RESULT")) {
                case "CREATE":
                    db.execSQL("INSERT INTO wardrobe VALUES (null, " +
                            "'" + data.getStringExtra("INPUT_CATEGORY") + "', " +
                            "'" + data.getStringExtra("INPUT_MATERIAL") + "', " +
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