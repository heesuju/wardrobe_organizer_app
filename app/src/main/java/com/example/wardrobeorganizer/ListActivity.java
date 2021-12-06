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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    static final int GET_STRING = 1;
    DatabaseHelper helper;
    SQLiteDatabase db;
    EditText edit_brand;
    Spinner spinner_category, spinner_material, spinner_state, spinner_color, spinner_date;
    Button btnSearch, btnClear;
    List<String> filterValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        spinner_color = findViewById(R.id.spinner_color);
        spinner_category = findViewById(R.id.spinner_category);
        spinner_material = findViewById(R.id.spinner_material);
        spinner_state = findViewById(R.id.spinner_state);
        spinner_date = findViewById(R.id.spinner_date);
        populateSpinnerColor();
        populateSpinnerCategory();
        populateSpinnerMaterial();
        populateSpinnerState();
        populateSpinnerDate();
        edit_brand = findViewById(R.id.edit_brand);
        btnSearch= findViewById(R.id.button_search);
        btnClear = findViewById(R.id.button_clear);
        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListActivity.this, SubActivity.class);
                intent.putExtra("ACTION_REQUEST", "CREATE");
                startActivityForResult(intent, GET_STRING);
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                printDb();
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                spinner_color.setSelection(((ArrayAdapter)spinner_color.getAdapter()).getPosition("모두"));
                spinner_category.setSelection(((ArrayAdapter)spinner_category.getAdapter()).getPosition("모두"));
                spinner_material.setSelection(((ArrayAdapter)spinner_material.getAdapter()).getPosition("모두"));
                spinner_state.setSelection(((ArrayAdapter)spinner_state.getAdapter()).getPosition("모두"));
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
                Log.v(TAG,"Permission is granted1");
                printDb();
            } else {

                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted1");
            printDb();
        }

    }

    private String checkFilter() {
        filterValues = new ArrayList<>();
        String category = spinner_category.getSelectedItem().toString();
        String material = spinner_material.getSelectedItem().toString();
        String brand = edit_brand.getText().toString();
        String state = spinner_state.getSelectedItem().toString();
        String color = spinner_color.getSelectedItem().toString();
        String date = spinner_date.getSelectedItem().toString();
        StringBuilder filter = new StringBuilder(100);
        if(!category.equals("모두")) {
            filter.append("category = ?");
            filterValues.add(category);
        }
        if(!color.equals("모두")) {
            if(filter.length() > 0) {
                filter.append(" AND ");
            }
            filter.append("color = ?");
            filterValues.add(color);
        }
        if(!material.equals("모두")) {
            if(filter.length() > 0) {
                filter.append(" AND ");
            }
            filter.append("material = ?");
            filterValues.add(material);
        }
        if(!brand.equals("")) {
            if(filter.length() > 0) {
                filter.append(" AND ");
            }
            filter.append("brand = ?");
            filterValues.add(brand);
        }
        if(!state.equals("모두")) {
            if(filter.length() > 0) {
                filter.append(" AND ");
            }
            filter.append("state = ?");
            filterValues.add(state);
        }
        if(!date.equals("전체")) {
            if(filter.length() > 0) {
                filter.append(" AND ");
            }
            TimeZone tz = TimeZone.getDefault();
            long time = System.currentTimeMillis();
            switch (date) {
                case "오늘":
                    time = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000) + 86400000;
                    time -= tz.getOffset(time);
                    System.out.println(new Date(time));
                    filter.append("worn BETWEEN " + time + " AND " + System.currentTimeMillis() + " ORDER BY worn ASC");
                    break;
                case "어제":
                    time = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000);
                    time -= tz.getOffset(time);
                    System.out.println(new Date(time));
                    filter.append("worn BETWEEN " + time + " AND " + System.currentTimeMillis() + " ORDER BY worn ASC");
                    break;
                case "1주":
                    time = System.currentTimeMillis() - ((86400000*6) + System.currentTimeMillis() % 86400000);
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
                    System.out.println(new Date(time));
                    long start = 0L;
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
        String[] sArr = new String[filterValues.size()];
        sArr = filterValues.toArray(sArr);
        Cursor cursor;
        if(filter.equals("")){
            cursor = db.rawQuery("SELECT * FROM wardrobe", null);
        }else{
            String query = "SELECT * FROM wardrobe WHERE " + filter;
            cursor = db.rawQuery(query, sArr);
        }
        startManagingCursor(cursor);

        String[] from = {"category", "material", "brand", "state", "image", "color", "worn"};
        int[] to = {R.id.category, R.id.material, R.id.brand, R.id.state, R.id.image, R.id.color, R.id.worn};

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
                    String material = cursor.getString(cursor.getColumnIndex("material"));
                    String brand = cursor.getString(cursor.getColumnIndex("brand"));
                    String state = cursor.getString(cursor.getColumnIndex("state"));
                    String image = cursor.getString(cursor.getColumnIndex("image"));
                    String color = cursor.getString(cursor.getColumnIndex("color"));
                    long worn = cursor.getLong(cursor.getColumnIndex("worn"));
                    intent.putExtra("ID", rowId);
                    intent.putExtra("CATEGORY", category);
                    intent.putExtra("MATERIAL", material);
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
        colors.add(0, "모두");
        colors.addAll(Arrays.asList(getResources().getStringArray(R.array.color_array)));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colors);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_color.setAdapter(arrayAdapter);
    }

    private void populateSpinnerCategory() {
        List<String> categories = new ArrayList<>();
        categories.add(0, "모두");
        categories.addAll(Arrays.asList(getResources().getStringArray(R.array.category_array)));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_category.setAdapter(arrayAdapter);
    }

    private void populateSpinnerMaterial() {
        List<String> materials = new ArrayList<>();
        materials.add(0, "모두");
        materials.addAll(Arrays.asList(getResources().getStringArray(R.array.material_array)));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, materials);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_material.setAdapter(arrayAdapter);
    }

    private void populateSpinnerState() {
        List<String> states = new ArrayList<>();
        states.add(0, "모두");
        states.addAll(Arrays.asList(getResources().getStringArray(R.array.state_array)));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, states);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_state.setAdapter(arrayAdapter);
    }

    private void populateSpinnerDate() {
        List<String> dates = new ArrayList<>();
        dates.add(0, "전체");
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
                            "'" + data.getStringExtra("INPUT_MATERIAL") + "', " +
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
                            "material = '" + data.getStringExtra("INPUT_MATERIAL") + "', " +
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
            if(parent.getItemAtPosition(i).equals("모두")) {

            }else{
                String selected = parent.getSelectedItem().toString();
            }
        }
        else if(parent.getId() == R.id.spinner_material){
            if(parent.getItemAtPosition(i).equals("모두")) {

            }else{
                String selected = parent.getSelectedItem().toString();
            }
        }
        else if(parent.getId() == R.id.spinner_state){
            if(parent.getItemAtPosition(i).equals("모두")) {

            }else{
                String selected = parent.getSelectedItem().toString();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}