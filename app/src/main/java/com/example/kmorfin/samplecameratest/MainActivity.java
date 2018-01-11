package com.example.kmorfin.samplecameratest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private static final int ACTION_TAKE_PHOTO = 1;
    private static final int ACTION_TAKE_PHOTO_LEGACY = 2;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private BaseAlbumDirFactory mAlbumDirectoryFactory = new BaseAlbumDirFactory();
    private String mCurrentPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button pciBtn = (Button)findViewById(R.id.camera_action_btn);
        final Context parentContext = this;
        pciBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String buildRel = Build.VERSION.RELEASE;
                String[] buildRelMain = buildRel.split("\\.");
                if (Integer.parseInt(buildRelMain[0]) > 4){
                    setupNormalPictureIntent();
                }   else {
                    setupOldPictureIntent();
                }

            }
        });
    }

    private void setupNormalPictureIntent(){
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            if (takePic.resolveActivity(getPackageManager()) != null){
                File f = setUpPhotoFile();
                if (f != null){
                    Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", f);
                    takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePic, ACTION_TAKE_PHOTO);
                } else {
                    Toast.makeText(this, "could not create directory for image", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "could not create directory for image", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupOldPictureIntent(){
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO_LEGACY);
        } catch (SecurityException se){
            Toast.makeText(getApplicationContext(), "permissions issue", Toast.LENGTH_LONG).show();
        }
    }

    private File getAlbumDir(){
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            //storageDir = mAlbumDirectoryFactory.getAlbumStorageDir(getString(R.string.album_name));
            if (storageDir != null){
                if (!storageDir.mkdirs()){
                    if (!storageDir.exists()){
                        Toast.makeText(this, "could not create directory for image", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }
            }

        } else {
            Toast.makeText(this, "external storage not mounted", Toast.LENGTH_SHORT).show();
        }

        return storageDir;

    }

    private File createImageFile() throws IOException{
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + time + "_";
        File album = getAlbumDir();
        File imgFile = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, album);
        return imgFile;
    }

    private File setUpPhotoFile() throws Exception {
        File f = createImageFile();
        mCurrentPath = f.getAbsolutePath();
        return f;
    }

    private void resetPic(ImageView target){
        int targetW = target.getWidth();
        int targetH = target.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 0;
        bmOptions.inPurgeable = true;
        int degrees = 0;

        try {
            ExifInterface exif = new ExifInterface(mCurrentPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("PIC_ORIEN", String.valueOf(orientation));
            switch (orientation){
                case ExifInterface.ORIENTATION_NORMAL:
                    Toast.makeText(this, "PICTURE SHOWN OK", Toast.LENGTH_SHORT).show();
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    //flip 90 degrees
                    degrees = -90;
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    //flip 180 degrees
                    degrees = -180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    //flip -180 degrees
                    degrees = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    //flip -270 degrees
                    degrees = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    //flip -90 degrees
                    degrees = 90;
                    break;
                default:
                    Toast.makeText(this, "PICTURE SHOWN OK", Toast.LENGTH_SHORT).show();
                    break;

            }
        } catch (IOException ioe){
            ioe.printStackTrace();
        }


        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPath, bmOptions);
        Bitmap finalizedBmt = null;
        //rotate image back to normal
        if (degrees != 0){
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            finalizedBmt = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            finalizedBmt = bitmap;
        }
        ImageView picCont = (ImageView)findViewById(R.id.imageCap);
        picCont.setImageBitmap(null);
        picCont.setImageBitmap(finalizedBmt);
        picCont.setScaleType(ImageView.ScaleType.CENTER_CROP);


    }

    private void handlePictureRequest(Intent data){
        //Bundle extras = data.getExtras();
        //Bitmap cpImg = (Bitmap)extras.get("data");
        ImageView picCont = (ImageView)findViewById(R.id.imageCap);
        resetPic(picCont);
    }

    private void handleLegacyPictureRequest(Intent data){
        Bitmap photo = (Bitmap) data.getExtras().get("data");

        ImageView mAddEnrolleeImage = (ImageView)findViewById(R.id.imageCap);
        mAddEnrolleeImage.setImageBitmap(photo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case ACTION_TAKE_PHOTO:
                if (resultCode == RESULT_OK){
                    handlePictureRequest(data);
                }
                else {
                    Toast.makeText(this, "PICTURE NOT TAKEN", Toast.LENGTH_SHORT).show();
                }
                break;
            case ACTION_TAKE_PHOTO_LEGACY:
                if (resultCode == RESULT_OK){
                    handleLegacyPictureRequest(data);
                }
                else {
                    Toast.makeText(this, "PICTURE NOT TAKEN", Toast.LENGTH_SHORT).show();
                }

            default:
                //Toast.makeText(this, "result code not ok", Toast.LENGTH_SHORT).show();
        }
    }
}
