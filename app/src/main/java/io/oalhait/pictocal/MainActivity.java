package io.oalhait.pictocal;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.provider.CalendarContract;
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
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ViewFlipper;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements View.OnClickListener{
    private Camera mCamera = null;
    private CameraView mCameraView = null;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public File pictureFile;
    private ImageView imgPreview;
    private File trainedData;
    ViewFlipper viewFlipper;
    Bitmap IMAGE_IN_USE;
    Intent schedule;
    DatePicker chooseDate;
    EditText eventTitle;
    NumberPicker eventTime;
    Date date;
    CreateEvent customizeDialog;
    Intent in = getIntent();
    Intent analyze;
    int year, month, day, hour;
    String title;
    static boolean paused = false;

    public static final String DATA_PATH = Environment.getExternalStorageDirectory() + "/PictoCal";
    FileOutputStream fos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //restart app, if you're here you're either coming back from a tag or from a multitask

        setContentView(R.layout.activity_main);
        viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        imgPreview.setVisibility(View.VISIBLE);
        schedule = new Intent(this, GetCalendarAPIActivity.class);
        analyze = new Intent(this,OCR.class);






        try {
            String s = (String) this.getIntent().getExtras().get("caller");
            Log.d("LLKJSLJKCSJ","LKJWLEKJFLWEKJFLEKWJLKJ");

            if (s.equals("OCR")) {

                Date dateReceived = (Date)getIntent().getExtras().get("Date");
                date = dateReceived;
                Log.d("DATERECE",date.toString());

                launchEventDialog();
                Log.d("LLKJSLJKCSJ", "LKJWBIOPLEKJF");

            }
            else {
                date = new Date(System.currentTimeMillis());

                launchEventDialog();
            }

        } catch(Exception e) {
            Log.d("ERROR",e.getMessage());
            Log.d("PictoCal", "date from OCR not found");
            //if it fails use the current date/time as default
        }
        try {
            if((boolean)this.getIntent().getExtras().get("approved")) {




            }

        } catch(Exception en) {
            Log.v("ERROR",en.getMessage());

        }


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


    }
    public void setYear(int year) {
        this.year = year;
    }
    public void setMonth(int month) {
        this.month = month;
    }
    public void setDay(int day) {
        this.day = day;
    }
    public void setHour(int hour) {
        this.hour = hour;
    }
    public void setEventTitle(String title) {
        this.title = title;
    }


    @Override
    public void onPause() {
        paused = true;
        super.onPause();
    }


    @Override
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
                    analyze.putExtra("filePath", pictureFile);
                    startActivity(analyze);

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
            case R.id.schedule_event: {

            }
            case R.id.cancel_event:
                customizeDialog.dismiss();

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
            } catch(IOException e) {
                Log.d("PictoCal","picturefile is failing");
            }

            imgPreview.setImageDrawable(null);
            imgPreview.setImageBitmap(IMAGE_IN_USE);

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


    public void launchEventDialog() {
        //assume "date" has already been put in as extras, otherwise this wouldn't be open
//        in.putExtra("Date",date);
        customizeDialog = new CreateEvent(this, date);
        customizeDialog.show();
    }

    class CreateEvent extends Dialog {


        public CreateEvent(Context context, Date date) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_create_event);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            Button cancelEvent = (Button) findViewById(R.id.cancel_event);
            Button schedule1 = (Button) findViewById(R.id.schedule_event);
            chooseDate = (DatePicker) findViewById(R.id.date_picker);
            eventTitle = (EditText) findViewById(R.id.event_name);
            eventTime = (NumberPicker) findViewById(R.id.time);


            cancelEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customizeDialog.dismiss();
                }
            });

            schedule1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int year = chooseDate.getYear();
                    int month = chooseDate.getMonth();
                    int day = chooseDate.getDayOfMonth();
                    String title = eventTitle.getText().toString();
                    int hour = eventTime.getValue();
                    Calendar beginTime = Calendar.getInstance();
                    Log.v("Pictocal",year+" " +month+" "+day+" "+hour);
                    beginTime.set(year, month, day, hour, 0);
                    Calendar endTime = Calendar.getInstance();
                    //fix hour+1 rollover
                    endTime.set(year, month, day, hour + 1, 0);
                    Log.v("Pictocal", endTime.toString());
                    Calendar cal = Calendar.getInstance();

                    Log.d("picooo",year + " " + month + " " + day);
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.setData(CalendarContract.Events.CONTENT_URI)
                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                            .putExtra(CalendarContract.Events.TITLE, title)
                            .putExtra(CalendarContract.Events.DESCRIPTION, "Made with PictoCal")
                            .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                    startActivity(intent);
                    setEventTitle(eventTitle.getText().toString());
//                    customizeDialog.dismiss();
//                    startActivity(schedule);
                }
            });
            //12 am
            eventTime.setMinValue(0);
            //11 pm
            eventTime.setMaxValue(23);
            ArrayList<String> times = new ArrayList<String>(0);
            times.add("12 am");
            for (int i = 1; i < 24; i++) {
                if (i < 12) {
                    times.add(i + " am");
                } else {
                    times.add(i - 11 + " pm");
                }
            }

            String[] objects = new String[times.size()];
            times.toArray(objects);
            eventTime.setDisplayedValues(objects);


            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int year = cal.get(Calendar.YEAR);
                Log.d("Year Found", String.valueOf(year));
                int month = cal.get(Calendar.MONTH);
                Log.d("Month Found",String.valueOf(month));
                int day = cal.get(Calendar.DAY_OF_MONTH);
                Log.d("Day Found", String.valueOf(day));
                chooseDate.updateDate(year, month, day);
            } catch (NullPointerException f) {
                Log.d("ERRRR",f.getMessage());
                Log.d("ERROR", "date is null");
            }

            builder.show();


        }



    }




}

