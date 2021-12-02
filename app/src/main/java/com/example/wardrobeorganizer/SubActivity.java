package com.example.wardrobeorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SubActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText edit_brand;
    String id;
    Spinner spinner_category, spinner_material, spinner_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        spinner_category = findViewById(R.id.spinner_category);
        spinner_material = findViewById(R.id.spinner_material);
        spinner_state = findViewById(R.id.spinner_state);
        edit_brand = findViewById(R.id.edit_brand);
        populateSpinnerCategory();
        populateSpinnerMaterial();
        populateSpinnerState();
        spinner_category.setOnItemSelectedListener(this);
        spinner_material.setOnItemSelectedListener(this);
        spinner_state.setOnItemSelectedListener(this);

        Button buttonSave = (Button) findViewById(R.id.button_save);
        Button buttonDelete = (Button) findViewById(R.id.button_delete);

        Intent intent = getIntent();
        String actionRequest = intent.getStringExtra("ACTION_REQUEST");
        switch (actionRequest) {
            case "CREATE":
                buttonSave.setVisibility(View.VISIBLE);
                buttonDelete.setVisibility(View.INVISIBLE);
                break;
            case "UPDATE":
                break;
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("ACTION_RESULT", "CREATE");
                intent.putExtra("INPUT_CATEGORY", spinner_category.getSelectedItem().toString());
                intent.putExtra("INPUT_MATERIAL", spinner_material.getSelectedItem().toString());
                intent.putExtra("INPUT_BRAND", edit_brand.getText().toString());
                intent.putExtra("INPUT_STATE", spinner_state.getSelectedItem().toString());

                setResult(RESULT_OK, intent);
                finish();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("ACTION_RESULT", "DELETE");
                intent.putExtra("ID", id);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    } // of onCreate()

    private void populateSpinnerCategory() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.category_array));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_category.setAdapter(arrayAdapter);
    }

    private void populateSpinnerMaterial() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.material_array));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_material.setAdapter(arrayAdapter);
    }

    private void populateSpinnerState() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.state_array));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_state.setAdapter(arrayAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
        if(parent.getId() == R.id.spinner_category){
            String selected = parent.getSelectedItem().toString();
            Toast.makeText(this, "selected" + selected, Toast.LENGTH_SHORT).show();
        }
        else if(parent.getId() == R.id.spinner_material){
            String selected = parent.getSelectedItem().toString();
            Toast.makeText(this, "selected" + selected, Toast.LENGTH_SHORT).show();
        }
        else if(parent.getId() == R.id.spinner_state){
            String selected = parent.getSelectedItem().toString();
            Toast.makeText(this, "selected" + selected, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}