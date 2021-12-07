package com.example.wardrobeorganizer;
import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    static final int GET_STRING = 1;
    DatabaseHelper helper;
    SQLiteDatabase db;
    EditText edit_brand;
    Spinner spinner_category, spinner_season, spinner_state, spinner_color, spinner_date;
    Button btnSearch, btnClear;
    List<String> filterValues;;
    ImageView color_img;
    LinearLayout state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        getSupportActionBar().hide();

        spinner_color = findViewById(R.id.spinner_color);
        spinner_category = findViewById(R.id.spinner_category);
        spinner_season = findViewById(R.id.spinner_season);
        spinner_state = findViewById(R.id.spinner_state);
        spinner_date = findViewById(R.id.spinner_date);
        populateSpinnerColor();
        populateSpinnerCategory();
        populateSpinnerSeason();
        populateSpinnerState();
        populateSpinnerDate();
        spinner_category.setOnItemSelectedListener(this);
        spinner_season.setOnItemSelectedListener(this);
        spinner_state.setOnItemSelectedListener(this);
        spinner_color.setOnItemSelectedListener(this);
        edit_brand = findViewById(R.id.edit_brand);
        btnSearch= findViewById(R.id.button_search);
        btnClear = findViewById(R.id.button_clear);
        color_img = findViewById(R.id.color_image);
        state = findViewById(R.id.state);

        btnSearch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                printDb();
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                spinner_color.setSelection(((ArrayAdapter)spinner_color.getAdapter()).getPosition("색상"));
                spinner_category.setSelection(((ArrayAdapter)spinner_category.getAdapter()).getPosition("종류"));
                spinner_season.setSelection(((ArrayAdapter)spinner_season.getAdapter()).getPosition("계절"));
                spinner_state.setSelection(((ArrayAdapter)spinner_state.getAdapter()).getPosition("상태"));
                spinner_date.setSelection(((ArrayAdapter)spinner_date.getAdapter()).getPosition("기간"));
                edit_brand.setText("");
                printDb();
            }
        });
        helper = new DatabaseHelper(this);
        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = helper.getReadableDatabase();
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                printDb();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            }
        }
        else {
            printDb();
        }
    }

    private String checkFilter() {
        filterValues = new ArrayList<>();
        String category = spinner_category.getSelectedItem().toString();
        String season = spinner_season.getSelectedItem().toString();
        String brand = edit_brand.getText().toString();
        String state = spinner_state.getSelectedItem().toString();
        String color = spinner_color.getSelectedItem().toString();
        String date = spinner_date.getSelectedItem().toString();
        StringBuilder filter = new StringBuilder(100);
        if(!category.equals("종류")) {
            filter.append("category = ?");
            filterValues.add(category);
        }
        if(!color.equals("색상")) {
            if(filter.length() > 0) {
                filter.append(" AND ");
            }
            filter.append("color = ?");
            filterValues.add(color);
        }
        if(!season.equals("계절")) {
            if(filter.length() > 0) {
                filter.append(" AND ");
            }
            filter.append("season = ?");
            filterValues.add(season);
        }
        if(!brand.equals("")) {
            if(filter.length() > 0) {
                filter.append(" AND ");
            }
            filter.append("brand like " + "'%" + brand + "%'");
        }
        if(!state.equals("상태")) {
            if(filter.length() > 0) {
                filter.append(" AND ");
            }
            filter.append("state = ?");
            filterValues.add(state);
        }
        if(!date.equals("기간")) {
            if(filter.length() > 0) {
                filter.append(" AND ");
            }
            TimeZone tz = TimeZone.getDefault();
            long time = System.currentTimeMillis();
            switch (date) {
                case "오늘":
                    time = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000);
                    time -= tz.getOffset(time);
                    System.out.println(new Date(time));
                    filter.append("worn BETWEEN " + time + " AND " + System.currentTimeMillis() + " ORDER BY worn ASC");
                    break;
                case "어제":
                    time = System.currentTimeMillis() - (86400000L + System.currentTimeMillis() % 86400000);
                    time -= tz.getOffset(time);
                    System.out.println(new Date(time));
                    filter.append("worn BETWEEN " + time + " AND " + System.currentTimeMillis() + " ORDER BY worn ASC");
                    break;
                case "1주":
                    time = System.currentTimeMillis() - ((86400000L*7L) + System.currentTimeMillis() % 86400000);
                    time -= tz.getOffset(time);
                    System.out.println(new Date(time));
                    filter.append("worn BETWEEN " + time + " AND " + System.currentTimeMillis() + " ORDER BY worn ASC");
                    break;
                case "1개월":
                    time = System.currentTimeMillis() - ((86400000L*30L) + System.currentTimeMillis() % 86400000);
                    time -= tz.getOffset(time);
                    System.out.println(new Date(time));
                    filter.append("worn BETWEEN " + time + " AND " + System.currentTimeMillis() + " ORDER BY worn ASC");
                    break;
                case "3개월":
                    time = System.currentTimeMillis() - ((86400000L*90L) + System.currentTimeMillis() % 86400000);
                    time -= tz.getOffset(time);
                    System.out.println(new Date(time));
                    filter.append("worn BETWEEN " + time + " AND " + System.currentTimeMillis() + " ORDER BY worn ASC");
                    break;
                case "6개월":
                    time = System.currentTimeMillis() - ((86400000L*180L) + System.currentTimeMillis() % 86400000);
                    time -= tz.getOffset(time);
                    System.out.println(new Date(time));
                    filter.append("worn BETWEEN " + time + " AND " + System.currentTimeMillis() + " ORDER BY worn ASC");
                    break;
                case "1년":
                    time = System.currentTimeMillis() - ((86400000L*365L) + System.currentTimeMillis() % 86400000);
                    time -= tz.getOffset(time);
                    System.out.println(new Date(time));
                    filter.append("worn BETWEEN " + time + " AND " + System.currentTimeMillis() + " ORDER BY worn ASC");
                    break;
                case "1년 이상":
                    time = System.currentTimeMillis() - ((86400000L*365L) + System.currentTimeMillis() % 86400000);
                    time -= tz.getOffset(time);
                    long start = 86400000L;
                    filter.append("worn BETWEEN " + start + " AND " + time + " ORDER BY worn ASC");
                    break;
                default:
                    break;
            }
        }
        return filter.toString();
    }

    private void printDb() {
        String filter = checkFilter();

        Cursor cursor;
        java.util.Date date= new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        TimeZone tz = TimeZone.getDefault();
        long time = System.currentTimeMillis() - ((86400000L*365L) + System.currentTimeMillis() % 86400000);
        time -= tz.getOffset(time);

        Intent intent = getIntent();
        String actionRequest = intent.getStringExtra("ACTION_REQUEST");
        String[] sArray = {};
        String str;
        String query = "";

        if(filter.equals("")){
            switch (actionRequest) {
                case "VIEW":
                    state.setVisibility(View.VISIBLE);
                    query = "SELECT * FROM wardrobe";
                    sArray = null;
                    break;
                case "TAKE_OUT":
                    state.setVisibility(View.GONE);
                    if(month >= 9 || (month >= 1 && month <= 2 )){
                        sArray = new String[]{"FW", "정리"};
                        str = "((season = ? AND state = ?) AND (worn BETWEEN " + time + " AND " + System.currentTimeMillis() + "))";
                        query = "SELECT * FROM wardrobe WHERE " + str;
                    }
                    break;
                case "PUT_AWAY":
                    state.setVisibility(View.GONE);
                    long start = 86400000L;
                    if(month >= 4 && month <= 8){
                        sArray = new String[]{"FW", "옷장", "옷장"};
                        str = "((season = ? AND state = ?) OR (worn BETWEEN " + start + " AND " + time + " AND state = ?))";
                        query = "SELECT * FROM wardrobe WHERE " + str;
                    }else{
                        sArray = new String[]{"옷장"};
                        str = "worn BETWEEN " + start + " AND " + time + " AND state = ?";
                        query = "SELECT * FROM wardrobe WHERE " + str;
                    }
                    break;
            }
        }else{
            sArray = new String[filterValues.size()];
            sArray = filterValues.toArray(sArray);
            query = "SELECT * FROM wardrobe WHERE " + filter;
        }
        cursor = db.rawQuery(query, sArray);
        startManagingCursor(cursor);

        String[] from = {"image"};
        int[] to = {R.id.image};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.list_item, cursor, from, to);

        GridView list = (GridView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListActivity.this, SubActivity.class);
                intent.putExtra("ACTION_REQUEST", "UPDATE");
                Cursor cursor = db.rawQuery("SELECT * FROM wardrobe WHERE _id = "
                                + id + ";"
                        , null);

                if (cursor.moveToFirst()) {
                    String rowId = cursor.getString(cursor.getColumnIndex("_id"));
                    String category = cursor.getString(cursor.getColumnIndex("category"));
                    String season = cursor.getString(cursor.getColumnIndex("season"));
                    String brand = cursor.getString(cursor.getColumnIndex("brand"));
                    String state = cursor.getString(cursor.getColumnIndex("state"));
                    String image = cursor.getString(cursor.getColumnIndex("image"));
                    String color = cursor.getString(cursor.getColumnIndex("color"));
                    long worn = cursor.getLong(cursor.getColumnIndex("worn"));
                    intent.putExtra("ID", rowId);
                    intent.putExtra("CATEGORY", category);
                    intent.putExtra("SEASON", season);
                    intent.putExtra("BRAND", brand);
                    intent.putExtra("STATE", state);
                    intent.putExtra("IMAGE", image);
                    intent.putExtra("COLOR", color);
                    intent.putExtra("WORN", worn);
                    startActivityForResult(intent, GET_STRING);
                }
            }
        });
    }
    private void populateSpinnerColor() {
        List<String> colors = new ArrayList<>();
        colors.add(0, "색상");
        colors.addAll(Arrays.asList(getResources().getStringArray(R.array.color_array)));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colors);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_color.setAdapter(arrayAdapter);
    }

    private void populateSpinnerCategory() {
        List<String> categories = new ArrayList<>();
        categories.add(0, "종류");
        categories.addAll(Arrays.asList(getResources().getStringArray(R.array.category_array)));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_category.setAdapter(arrayAdapter);
    }

    private void populateSpinnerSeason() {
        List<String> seasons = new ArrayList<>();
        seasons.add(0, "계절");
        seasons.addAll(Arrays.asList(getResources().getStringArray(R.array.season_array)));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, seasons);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_season.setAdapter(arrayAdapter);
    }

    private void populateSpinnerState() {
        List<String> states = new ArrayList<>();
        states.add(0, "상태");
        states.addAll(Arrays.asList(getResources().getStringArray(R.array.state_array)));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, states);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_state.setAdapter(arrayAdapter);
    }

    private void populateSpinnerDate() {
        List<String> dates = new ArrayList<>();
        dates.add(0, "기간");
        dates.addAll(Arrays.asList(getResources().getStringArray(R.array.date_array)));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dates);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_date.setAdapter(arrayAdapter);
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
                    printDb();
                    break;
                case "UPDATE":
                    db.execSQL("UPDATE wardrobe SET " +
                            "category = '" + data.getStringExtra("INPUT_CATEGORY") + "', " +
                            "season = '" + data.getStringExtra("INPUT_SEASON") + "', " +
                            "brand = '" + data.getStringExtra("INPUT_BRAND") + "', " +
                            "state = '" + data.getStringExtra("INPUT_STATE") + "', " +
                            "image = '" + data.getStringExtra("INPUT_IMAGE") + "', " +
                            "color = '" + data.getStringExtra("INPUT_COLOR") + "', " +
                            "worn = '" + data.getLongExtra("INPUT_WORN", 0) + "' " +
                            "WHERE _id = '" + data.getStringExtra("ID") + "';"
                    );
                    printDb();
                    break;
                case "WEAR":
                    db.execSQL("UPDATE wardrobe SET " +
                            "worn = '" + data.getLongExtra("INPUT_WORN", 0) + "' " +
                            "WHERE _id = '" + data.getStringExtra("ID") + "';"
                    );
                    printDb();
                    break;
                case "MOVE":
                    db.execSQL("UPDATE wardrobe SET " +
                            "state = '" + data.getStringExtra("INPUT_STATE") + "' " +
                            "WHERE _id = '" + data.getStringExtra("ID") + "';"
                    );
                    printDb();
                    break;

                case "DELETE":
                    db.execSQL("DELETE FROM wardrobe WHERE _id = " +
                            "'" + data.getStringExtra("ID") + "';"
                    );
                    printDb();
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
        if(parent.getId() == R.id.spinner_category){
            if(!parent.getItemAtPosition(i).equals("종류")) {
                String selected = parent.getSelectedItem().toString();
            }
        }
        else if(parent.getId() == R.id.spinner_season){
            if(!parent.getItemAtPosition(i).equals("계절")) {
                String selected = parent.getSelectedItem().toString();
            }
        }
        else if(parent.getId() == R.id.spinner_state){
            if(!parent.getItemAtPosition(i).equals("상태")) {
                String selected = parent.getSelectedItem().toString();
            }
        }
        else if(parent.getId() == R.id.spinner_color){
            String selected = parent.getSelectedItem().toString();
            switch(selected){
                case "하양":
                    color_img.setBackgroundResource(R.color.white);
                    break;
                case "빨강":
                    color_img.setBackgroundResource(R.color.red);
                    break;
                case "파랑":
                    color_img.setBackgroundResource(R.color.blue);
                    break;
                case "초록":
                    color_img.setBackgroundResource(R.color.green);
                    break;
                case "노랑":
                    color_img.setBackgroundResource(R.color.yellow);
                    break;
                case "주황":
                    color_img.setBackgroundResource(R.color.orange);
                    break;
                case "갈색":
                    color_img.setBackgroundResource(R.color.brown);
                    break;
                case "분홍":
                    color_img.setBackgroundResource(R.color.pink);
                    break;
                case "보라":
                    color_img.setBackgroundResource(R.color.purple_200);
                    break;
                case "남색":
                    color_img.setBackgroundResource(R.color.navy);
                    break;
                case "회색":
                    color_img.setBackgroundResource(R.color.gray);
                    break;
                case "검정":
                    color_img.setBackgroundResource(R.color.black);
                    break;
                default:
                    color_img.setBackgroundResource(R.color.white);
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}