package com.example.wardrobeorganizer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class SubActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText edit_brand;
    String id;
    Spinner spinner_category, spinner_material, spinner_state;
    Button btnCamera, btnAlbum;
    ImageView image;
    public static final int PICK_IMAGE = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        spinner_category = findViewById(R.id.spinner_category);
        spinner_material = findViewById(R.id.spinner_material);
        spinner_state = findViewById(R.id.spinner_state);
        edit_brand = findViewById(R.id.edit_brand);

        btnAlbum = findViewById(R.id.button_album);
        btnCamera = findViewById(R.id.button_camera);
        image = findViewById(R.id.image);
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
        btnAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(SubActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(SubActivity.this, new String[]{"Manifest.permission.CAMERA"}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("ACTION_RESULT", "CREATE");
                intent.putExtra("INPUT_CATEGORY", spinner_category.getSelectedItem().toString());
                intent.putExtra("INPUT_MATERIAL", spinner_material.getSelectedItem().toString());
                intent.putExtra("INPUT_BRAND", edit_brand.getText().toString());
                intent.putExtra("INPUT_STATE", spinner_state.getSelectedItem().toString());
                //Bitmap b = BitmapFactory.decodeResource(getResources(), R.id.image);
                /*create the object of ByteArrayoutputStream class. Now break the image into the byte parts by calling toByteArray() of ByteOutputStream class and store it in a array */
                //ByteArrayOutputStream bos = new ByteArrayOutputStream();
                //b.compress(Bitmap.CompressFormat.PNG, 100, bos);
                //byte[] img = bos.toByteArray();
                /*to write in a database call the getWritableDatabase method */
                //intent.putExtra("INPUT_IMAGE", img);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                image.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            image.setImageBitmap(photo);
        }
    }

}