package io.oalhait.pictocal;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
    private Camera mCamera = null;
    private CameraView mCameraView = null;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public File fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        try{
            mCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

        if(mCamera != null) {
            mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }
        ImageButton shutter = (ImageButton)findViewById(R.id.fab);
        shutter.setOnClickListener(onClickListener);

    }
    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab:
                    mCamera.takePicture(null, null, mPicture);
                    mCamera.startPreview();
//                    usePic();

                    break;
                default:
                    break;
            }


        }
    };

    public void galleryAddPic(File currentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(currentPhotoPath);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile();

            try {
                // Solve image inverting problem
                Bitmap originalImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                String filePath = pictureFile.getAbsolutePath();
                originalImage = ExifUtil.rotateBitmap(filePath, originalImage);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();


                byte[] byteArray = stream.toByteArray();

                Intent intent = new Intent(MainActivity.this,ChooseActivity.class);
                intent.putExtra("byteArray",byteArray);
                intent.putExtra("pictureFile", pictureFile);
                intent.putExtra("isImage", true);

                FileOutputStream fos = new FileOutputStream(filePath);
                fos.write(byteArray);
                fos.close();


                launchChooseActivity(intent);


//                galleryAddPic(pictureFile);
            } catch (Exception e) {
            }
        }
    };


    private void launchChooseActivity(Intent i){
        startActivity(i);
    }

    private static File getOutputMediaFile() {

        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "PictoCal");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("PictoCal", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US)
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

}
