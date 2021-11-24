package com.example.wardrobeorganizer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView resultList = (ListView) findViewById(R.id.resultList);
        EditText searchFilter = (EditText) findViewById(R.id.searchFilter);
        Log.d(TAG, "onCreate: Started.");

        ArrayList<String> examples = new ArrayList<>();
        examples.add("Zara");
        examples.add("H&M");
        examples.add("American Eagle");
        examples.add("8 Seconds");

        adapter = new ArrayAdapter(this, R.layout.item_layout, examples);
        resultList.setAdapter(adapter);

        searchFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                (MainActivity.this).adapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}