package io.oalhait.pictocal;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import com.joestelmach.natty.*;


public class OCR extends Activity {
    private static final String TAG = "TESSERACT";




    public String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/PictoCal/";


    public static final String lang = "eng";


    protected Button _button;
    protected EditText _field;
    protected String _path;
    protected boolean _taken;
    protected static final String PHOTO_TAKEN = "photo_taken";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {
                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }

        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        Bundle extra = i.getExtras();
        File pictureFile = (File)extra.get("filePath");



        _path = pictureFile.toString();
        onPhotoTaken();


    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "resultCode: " + resultCode);

        if (resultCode == -1) {
            onPhotoTaken();
        } else {
            Log.v(TAG, "User cancelled");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(OCR.PHOTO_TAKEN, _taken);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onRestoreInstanceState()");
        if (savedInstanceState.getBoolean(OCR.PHOTO_TAKEN)) {
            onPhotoTaken();
        }
    }

    protected void onPhotoTaken() {
        _taken = true;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile(_path, options);

        Log.v(TAG, "Before baseApi");

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(bitmap);

        String recognizedText = baseApi.getUTF8Text();

        baseApi.end();

        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        if ( lang.equalsIgnoreCase("eng") ) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }

        try {
            recognizedText = recognizedText.trim();
            Date date = findDate(recognizedText);
            if ( recognizedText.length() != 0 ) {

                Log.d("DATE FOUND", date.toString());
                Intent calendar = new Intent(this,MainActivity.class);
                calendar.putExtra("Date", date);
                calendar.putExtra("caller","OCR");
                calendar.putExtra("executed",true);
                calendar.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(calendar);
                finish();
            }
        } catch(NullPointerException e) {
            Log.d("ERROR",e.getMessage());
            Log.d(TAG,"Sorry, nothing was recognized!");
        }



        // Cycle done.

    }

    public Date findDate(String output) {
        try {
            List<DateGroup> actualDates = new Parser().parse(output);
            //remove first date in actualDates, for some reason it's always the time photo was taken
            actualDates.remove(0);
            // make new list with one date found in the document in it
            List<Date> dates = new Parser().parse(output).get(0).getDates();


            Log.d("OUTPUT GIVEN",output);


            Log.d("DATES",dates.toString());
            for (int dat = 0; dat < dates.size();dat++) {
                Log.v("tEseseract", dates.get(dat).toString());
            }
            Log.v(TAG,dates.get(0).toString());
            return dates.get(0);


//            return actualDates.get(0).getDates().get(0);

        } catch (Exception e) {
            Log.d(TAG,"No date Found!");
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
        return;
    }
}
