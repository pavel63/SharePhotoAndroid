package com.example.android_razrab.choosesourcephoto;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 10;
    private static final int REQUEST_CAMERA = 11;


    private ImageView mImageView;



    Intent pathLastImage;


    static int PERMISSION_REQUEST_CODE = 123;

    public void onClick(View v) {


        selectImage();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.iv);

        if (hasPermissions()) {
        } else {
            requestPerms();
        }
    }




    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

            Uri uri;



                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment
                            .getExternalStorageDirectory(), "temp.jpg");


                    if(android.os.Build.VERSION.SDK_INT >=24){

                        uri= FileProvider.getUriForFile(MainActivity.this,BuildConfig.APPLICATION_ID+".provider",f);

                    }else {

                        uri = Uri.fromFile(f);
                    }


                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);




                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_PICTURE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();



    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {



            pathLastImage=data;



            if (requestCode == REQUEST_CAMERA) {
                File f = new File(Environment.getExternalStorageDirectory()
                        .toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bm;
                    BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                    btmapOptions.inSampleSize = 2;
                    bm = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            btmapOptions);

                   // bm = Bitmap.createScaledBitmap(bm, 70, 70, true);



                    mImageView.setImageBitmap(bm);

                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "test";
                    f.delete();
                    OutputStream fOut = null;
                    File file = new File(path, String.valueOf(System
                            .currentTimeMillis()) + ".jpg");
                    fOut = new FileOutputStream(file);
                    bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                    fOut.flush();
                    fOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                String tempPath = getPath(selectedImageUri, MainActivity.this);
                Bitmap bm;
                // btmapOptions.inSampleSize = 2;
                BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                bm = BitmapFactory.decodeFile(tempPath, btmapOptions);
                mImageView.setImageBitmap(bm);
            }
        }
    }
    public String getPath(Uri uri, Activity activity) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = activity
                .managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }








    public boolean hasPermissions() {
        int res = 0;

        String[] permissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String perms : permissions) {
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    public void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }

    }






    //этим кодом можем взять наше фото для деления с кем-то
    public void onClickGet(View v){


        if(pathLastImage!=null) {


            Uri uri = pathLastImage.getData();

            File filePath = new File(getPath(uri, this));


            if (android.os.Build.VERSION.SDK_INT >= 24) {

                uri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", filePath);

            } else {

                uri = Uri.fromFile(filePath);
            }

            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("image*/");
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
            intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
            intent.putExtra(Intent.EXTRA_STREAM, uri);

            startActivity(Intent.createChooser(intent, "Sharing something"));

        }else {
            Toast.makeText(this, "Сначала выберите фото!", Toast.LENGTH_SHORT).show();
            selectImage();
        }

    }

}

