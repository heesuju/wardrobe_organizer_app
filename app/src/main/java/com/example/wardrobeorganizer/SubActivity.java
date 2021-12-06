package com.example.wardrobeorganizer;

import static android.content.ContentValues.TAG;


import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SubActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText edit_brand;
    String id;
    String filePath;
    Spinner spinner_category, spinner_material, spinner_state, spinner_color;
    Button btnCamera, btnAlbum, btnWear;
    ImageView image;
    TextView text_worn;
    long date_time;
    DatabaseHelper helper;
    SQLiteDatabase db;
    public static final int PICK_IMAGE = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        spinner_color = findViewById(R.id.spinner_color);
        spinner_category = findViewById(R.id.spinner_category);
        spinner_material = findViewById(R.id.spinner_material);
        spinner_state = findViewById(R.id.spinner_state);
        edit_brand = findViewById(R.id.edit_brand);
        text_worn = findViewById(R.id.text_worn);
        btnAlbum = findViewById(R.id.button_album);
        btnCamera = findViewById(R.id.button_camera);
        image = findViewById(R.id.image);
        populateSpinnerColor();
        populateSpinnerCategory();
        populateSpinnerMaterial();
        populateSpinnerState();
        spinner_category.setOnItemSelectedListener(this);
        spinner_material.setOnItemSelectedListener(this);
        spinner_state.setOnItemSelectedListener(this);

        helper = new DatabaseHelper(this);
        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = helper.getReadableDatabase();
        }


        Button buttonSave = (Button) findViewById(R.id.button_save);
        Button buttonUpdate = (Button) findViewById(R.id.button_update);
        Button buttonDelete = (Button) findViewById(R.id.button_delete);
        btnWear = findViewById(R.id.button_wear);

        Intent intent = getIntent();
        String actionRequest = intent.getStringExtra("ACTION_REQUEST");
        switch (actionRequest) {
            case "CREATE":
                buttonSave.setVisibility(View.VISIBLE);
                buttonUpdate.setVisibility(View.INVISIBLE);
                buttonDelete.setVisibility(View.INVISIBLE);
                text_worn.setVisibility(View.INVISIBLE);
                break;
            case "UPDATE":
                buttonSave.setVisibility(View.INVISIBLE);
                buttonUpdate.setVisibility(View.VISIBLE);
                buttonDelete.setVisibility(View.VISIBLE);
                text_worn.setVisibility(View.VISIBLE);

                id = intent.getStringExtra("ID");
                spinner_category.setSelection(((ArrayAdapter)spinner_category.getAdapter()).getPosition(intent.getStringExtra("CATEGORY")));
                spinner_material.setSelection(((ArrayAdapter)spinner_material.getAdapter()).getPosition(intent.getStringExtra("MATERIAL")));
                spinner_state.setSelection(((ArrayAdapter)spinner_state.getAdapter()).getPosition(intent.getStringExtra("STATE")));
                spinner_color.setSelection(((ArrayAdapter)spinner_color.getAdapter()).getPosition(intent.getStringExtra("COLOR")));
                edit_brand.setText(intent.getStringExtra("BRAND"));
                date_time = intent.getLongExtra("WORN", 0);
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((intent.getLongExtra("WORN", 0)));
                text_worn.setText(date);
                filePath = intent.getStringExtra("IMAGE");
                Bitmap bitmap = BitmapFactory.decodeFile(intent.getStringExtra("IMAGE"));
                image.setImageBitmap(bitmap);
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
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
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
                int nextInc = getAutoIncrement();
                Intent intent = new Intent();
                intent.putExtra("ACTION_RESULT", "CREATE");
                intent.putExtra("INPUT_CATEGORY", spinner_category.getSelectedItem().toString());
                intent.putExtra("INPUT_MATERIAL", spinner_material.getSelectedItem().toString());
                intent.putExtra("INPUT_BRAND", edit_brand.getText().toString());
                intent.putExtra("INPUT_STATE", spinner_state.getSelectedItem().toString());
                isWriteStoragePermissionGranted();
                isReadStoragePermissionGranted();
                //always save as
                Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
                String dir = saveImage(bitmap, String.valueOf(nextInc));
                intent.putExtra("INPUT_IMAGE", dir);
                intent.putExtra("INPUT_COLOR", spinner_color.getSelectedItem().toString());
                intent.putExtra("INPUT_WORN", 0L);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("ACTION_RESULT", "UPDATE");
                intent.putExtra("ID", id);
                intent.putExtra("INPUT_CATEGORY", spinner_category.getSelectedItem().toString());
                intent.putExtra("INPUT_MATERIAL", spinner_material.getSelectedItem().toString());
                intent.putExtra("INPUT_BRAND", edit_brand.getText().toString());
                intent.putExtra("INPUT_STATE", spinner_state.getSelectedItem().toString());
                isWriteStoragePermissionGranted();
                isReadStoragePermissionGranted();
                //always save as
                Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
                String dir = saveImage(bitmap, id);
                intent.putExtra("INPUT_IMAGE", dir);
                intent.putExtra("INPUT_COLOR", spinner_color.getSelectedItem().toString());
                intent.putExtra("INPUT_WORN", date_time);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert(v);
            }
        });
        btnWear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("ACTION_RESULT", "WEAR");
                intent.putExtra("ID", id);
                intent.putExtra("INPUT_WORN", System.currentTimeMillis());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    } // of onCreate()
    public int getAutoIncrement(){
        String query = "SELECT * FROM SQLITE_SEQUENCE";
        Cursor cursor = db.rawQuery(query, null);
        int nextInc = 0;
        if (cursor.moveToFirst()){
            do{
                nextInc = cursor.getInt(cursor.getColumnIndex("seq"));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return ++nextInc;
    }
    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted1");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted1");
            return true;
        }
    }
    public void showAlert(View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("삭제하시겠습니까?");
        alertDialogBuilder.setPositiveButton("네",
            new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deleteImage(filePath);
                    Intent intent = new Intent();
                    intent.putExtra("ACTION_RESULT", "DELETE");
                    intent.putExtra("ID", id);
                    setResult(RESULT_OK, intent);
                    finish();
                    Toast.makeText(SubActivity.this, "삭제 완료되었습니다.", Toast.LENGTH_LONG).show();
                }
        });
        alertDialogBuilder.setNegativeButton("아니요",
            new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(SubActivity.this, "취소되었습니다.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted2");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted2");
            return true;
        }
    }

    public String saveImage(Bitmap bitmap, String fileName){
        File fullPath = Environment.getExternalStorageDirectory();
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File ex = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File dir = new File(ex, "Wardrobe");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName + ".jpg");
        if (file.exists()) {
            file.delete();
        }
        if (!file.exists()) {
            Log.d("path", file.toString());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
                Log.d("what", file.toString());
            }
        }
        return file.getAbsolutePath();
    }
    public void deleteImage(String filePath){
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
    private void populateSpinnerColor() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.color_array));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_color.setAdapter(arrayAdapter);
    }

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
        }
        else if(parent.getId() == R.id.spinner_material){
            String selected = parent.getSelectedItem().toString();
        }
        else if(parent.getId() == R.id.spinner_state){
            String selected = parent.getSelectedItem().toString();
        }
        else if(parent.getId() == R.id.spinner_color){
            String selected = parent.getSelectedItem().toString();
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
            Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
            image.setImageBitmap(selectedImage);
        }
    }

}