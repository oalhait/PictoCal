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
import android.widget.DatePicker;
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


//        setContentView(R.layout.ocr);
//        _field = (EditText) findViewById(R.id.field);



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

        // _image.setImageBitmap( bitmap );

        Log.v(TAG, "Before baseApi");

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(bitmap);

        String recognizedText = baseApi.getUTF8Text();

        baseApi.end();

        // You now have the text in recognizedText int, you can do anything with it.
        // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
        // so that garbage doesn't make it to the display.

        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        if ( lang.equalsIgnoreCase("eng") ) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }

        try {
            recognizedText = recognizedText.trim();
            Date date = findDate(recognizedText);
            if ( recognizedText.length() != 0 ) {
                _field.setText(_field.getText().toString().length() == 0 ? recognizedText : _field.getText() + " " + recognizedText);
                _field.setSelection(_field.getText().toString().length());
                Log.d(TAG, recognizedText);
                Log.d("DATE FOUND", date.toString());
                Intent calendar = new Intent(this,CreateEvent.class);
                calendar.putExtra("Date", date);

                calendar.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(calendar);
                finish();
            }
        } catch(NullPointerException e) {
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

            for (int s = 1; s < actualDates.size(); s++) {
                dates.add(actualDates.get(s).getDates().get(0));
            }


            Log.d("OUTPUT GIVEN",output);

            Log.d("DATES",dates.toString());
            List<Date> dates2 = new Parser().parse(output).get(1).getDates();
            for (int dat = 0; dat < dates2.size();dat++) {
                Log.v("tEseseract", dates2.get(dat).toString());
            }

            return actualDates.get(0).getDates().get(0);

        } catch (Exception e) {
            Log.d(TAG,"No date Found!");
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
