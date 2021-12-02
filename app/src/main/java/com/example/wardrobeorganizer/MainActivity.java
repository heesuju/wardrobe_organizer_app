package com.example.wardrobeorganizer;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends AppCompatActivity {

    static final int GET_STRING = 1;
    DatabaseHelper helper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("ACTION_REQUEST", "CREATE");
                startActivityForResult(intent, GET_STRING);
            }
        });

        helper = new DatabaseHelper(this);
        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = helper.getReadableDatabase();
        }
        printDb();
    }

    private void printDb() {
        Cursor cursor = db.rawQuery("SELECT * FROM wardrobe", null);
        startManagingCursor(cursor);

        String[] from = {"category", "material", "brand", "state"};
        int[] to = {R.id.category, R.id.material, R.id.brand, R.id.state};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.list_item, cursor, from, to);

        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
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
                    intent.putExtra("ID", rowId);
                    intent.putExtra("CATEGORY", category);
                    intent.putExtra("MATERIAL", material);
                    intent.putExtra("BRAND", brand);
                    intent.putExtra("STATE", state);
                    startActivityForResult(intent, GET_STRING);
                }
            }
        });
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
                            "'" + data.getStringExtra("INPUT_STATE") + "');"
                    );
                    printDb();
                    break;
                case "UPDATE":
                    db.execSQL("UPDATE wardrobe SET " +
                            "'" + data.getStringExtra("INPUT_CATEGORY") + "', " +
                            "'" + data.getStringExtra("INPUT_MATERIAL") + "', " +
                            "'" + data.getStringExtra("INPUT_BRAND") + "', " +
                            "'" + data.getStringExtra("INPUT_STATE") + "' " +
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
}