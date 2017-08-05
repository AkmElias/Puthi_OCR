package com.example.user.travelapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.user.travelapp.config.AppController;
import com.example.user.travelapp.models.VolleyMultipartRequest;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    List<byte[]> photoListByte;

    private static final int IMAGE_FROM_STORAGE = 100;
    private static final int PERMISSION_REQUEST = 101;

    private Button loadImageFromStorage;
    private ImageView image;

    private Button uploadImage;

    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageBitmap = null;

        photoListByte = new ArrayList<>();

        loadImageFromStorage = (Button) findViewById(R.id.loadImage);
        image = (ImageView) findViewById(R.id.image);
        uploadImage = (Button) findViewById(R.id.uploadNow);

        loadImageFromStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForPermission();
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImageNow();
            }
        });


    }


    private void uploadImageNow(){

        imageBitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);

        photoListByte.clear();

        photoListByte.add(bos.toByteArray());

        if(imageBitmap == null){
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }else{
            upload();
        }
    }

    private void upload(){


        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading....");
        dialog.show();

        String API_URL = "http://113.11.120.208/upload";

        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, API_URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {

                dialog.dismiss();
                String data = new String(response.data);

                do_OCR(data);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String, DataPart> params = new HashMap<>();

                if(photoListByte.size() > 0){
                    params.put("sampleFile" , new DataPart("Image_FIle", photoListByte.get(0),"image/jpeg/png"));
                }

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request,"Uploading_Files");


    }


    private void do_OCR(String str){

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Doing OCR....");
        dialog.show();

        String API_URL = "http://113.11.120.208/do_ocr?src=" + str;

        StringRequest request = new StringRequest(Request.Method.GET, API_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.dismiss();
                Log.d("OCR RESPONSE", "onResponse: " + response);
                startResultActivity(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                dialog.dismiss();
            }
        });

        AppController.getInstance().addToRequestQueue(request);
    }

    private void startResultActivity(String response){
        Intent intent = new Intent(MainActivity.this, OCRResultActivity.class);
        intent.putExtra("response", response);
        startActivity(intent);
    }

    private void checkForPermission(){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST);
        }else{
            pickImageFromGallery();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_REQUEST){
            if(grantResults.length > 0){
                pickImageFromGallery();
            }else{
                checkForPermission();
            }
        }
    }

    private void pickImageFromGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, IMAGE_FROM_STORAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_FROM_STORAGE){
            if(data == null){
                Log.d("Data Result", "onActivityResult: Null Data Found");
            }else{
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,filePath, null, null, null);

                assert cursor != null;

                cursor.moveToFirst();

                int colIndex = cursor.getColumnIndex(filePath[0]);
                String imageFilePath = cursor.getString(colIndex);

                Log.d("Image URL", "onActivityResult: " + imageFilePath);

                cursor.close();

                Glide.with(this).load(imageFilePath).asBitmap().into(image);
            }
        }
    }
}
