package io.oalhait.pictocal;
import android.os.AsyncTask;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements View.OnClickListener {
    private Camera mCamera = null;
    private CameraView mCameraView = null;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public File pictureFile;
    private ImageView imgPreview;
    private File trainedData;
    ViewFlipper viewFlipper;
    Bitmap IMAGE_IN_USE;
    public static final String DATA_PATH = Environment.getExternalStorageDirectory() + "/PictoCal";
    public FileOutputStream fos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        imgPreview.setVisibility(View.VISIBLE);


        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            Log.d("ERROR", "Failed to get activity_main: " + e.getMessage());
        }

        if (mCamera != null) {
            //create a SurfaceView to show activity_main data
            mCameraView = new CameraView(this, mCamera);
            FrameLayout camera_view = (FrameLayout) findViewById(R.id.camera_view);
            //add the SurfaceView to the layout
            camera_view.addView(mCameraView);


        }
        ImageButton shutter = (ImageButton) findViewById(R.id.fab);
        shutter.setOnClickListener(this);


    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                mCamera.takePicture(null, null, mPicture);
                break;
            case R.id.btnUpload:
            {
                try {


                    IMAGE_IN_USE.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                    Intent in = new Intent(this, CreateEvent.class);
                    in.putExtra("filePath", pictureFile);
                    startActivity(in);
                } catch (IOException e) {
                    Log.d("Pictocal","bitmap failed to be written");
                }
                break;
            }
            case R.id.btnCancel:
            {
                viewFlipper.showPrevious();
                break;
            }
            default:
                break;
        }
    }

    public void galleryAddPic(File currentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(currentPhotoPath);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            pictureFile = getOutputMediaFile();
            int angleToRotate =
                    getRotationAngle(MainActivity.this, Camera.CameraInfo.CAMERA_FACING_BACK);
            Bitmap originalImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            IMAGE_IN_USE = rotate(originalImage, angleToRotate);
            originalImage.recycle();

            try {
                fos  = new FileOutputStream(pictureFile);
            } catch(IOException e) {}

            imgPreview.setImageDrawable(null);
            imgPreview.setImageBitmap(IMAGE_IN_USE);
//                viewFlipper.refreshDrawableState();

            viewFlipper.showNext();

            mCamera.startPreview();

        }
    };



    private static File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/PictoCal");


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
    //  not mine, all credits go to Sheraz Ahmad Khiliji of Stackoverflow.com
    public static int getRotationAngle(Activity mContext, int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
    //  not mine, all credits go to Sheraz Ahmad Khiliji of Stackoverflow.com
    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    //also not mine, all credits go to
    private void moveFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputPath + inputFile).delete();


        }

        catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }



}

