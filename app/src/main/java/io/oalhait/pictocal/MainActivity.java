package io.oalhait.pictocal;

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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ViewFlipper;
import com.googlecode.tesseract.android.TessBaseAPI;

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
    public File pictureFileUri;
    private ImageView imgPreview;
    Button upload, cancel;
    private File trainedData;
    ViewFlipper viewFlipper;
    Bitmap IMAGE_IN_USE;
    public static final String DATA_PATH =
            Environment.getExternalStorageDirectory() + "/PictoCal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        trainedData = new File(Environment.getExternalStorageDirectory() + "/Pictocal/tessdata/eng.traineddata");
        if (!trainedData.exists()) {
//            DownloadFile x = new DownloadFile();
        }

        setContentView(R.layout.activity_main);
        viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        imgPreview.setVisibility(View.VISIBLE);



        try{
            mCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get activity_main: " + e.getMessage());
        }

        if(mCamera != null) {
            mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show activity_main data
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout


        }
        ImageButton shutter = (ImageButton)findViewById(R.id.fab);
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
                    FileOutputStream fos = new FileOutputStream(pictureFileUri);
                    IMAGE_IN_USE.compress(Bitmap.CompressFormat.PNG, 50, fos);
                    fos.flush();
                    fos.close();
                    TessBaseAPI baseApi = new TessBaseAPI();
                    galleryAddPic(pictureFileUri);
//                    following 4 lines are to implement tesseract OCR library
//                     DATA_PATH = Path to the storage
//                     lang = for which the language data exists, usually "eng"
                    baseApi.init(DATA_PATH, "eng");
//                     Eg. baseApi.init("/mnt/sdcard/tesseract/tessdata/eng.traineddata", "eng");
                    baseApi.setImage(IMAGE_IN_USE);
                    String recognizedText = baseApi.getUTF8Text();
                    baseApi.end();
                    Log.d("PICTOCAL",recognizedText);

                } catch (IOException e) {
                    Log.d("PictoCal","Image not Found");
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

            pictureFileUri = getOutputMediaFile();
            int angleToRotate =
                    getRotationAngle(MainActivity.this, Camera.CameraInfo.CAMERA_FACING_BACK);
            Bitmap originalImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            IMAGE_IN_USE = rotate(originalImage, angleToRotate);
            originalImage.recycle();


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

