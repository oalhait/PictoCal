package io.oalhait.pictocal;

/**
 * Created by oalhait on 12/1/15.
 */
import io.oalhait.pictocal.AndroidMultiPartEntity.ProgressListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ChooseActivity extends Activity implements View.OnClickListener{
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private ImageView imgPreview;
    Button upload, cancel;
    long totalSize = 0;
    private Bitmap bitmapImage;

//    private

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        upload = (Button) findViewById(R.id.btnUpload);
        upload.setOnClickListener(this);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        cancel = (Button)findViewById(R.id.btnCancel);
        cancel.setOnClickListener(this);


        // Receiving the data from previous activity
        Intent i = getIntent();

        Bundle extras = i.getExtras();
        File filePath = ((File) extras.get("pictureFile"));
//        Log.d("File Received",filePath.toString());

//         boolean flag to identify the media type, image or video
        boolean isImage = i.getBooleanExtra("isImage", true);

        if (filePath != null) {
            byte[] bArray = (byte[])extras.get("byteArray");
      //       Displaying the image or video on the screen
            previewMedia(isImage, bArray);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry, file path is missing!", Toast.LENGTH_LONG).show();
        }

    }

    public void onClick(View v){

        switch(v.getId())
        {
            case R.id.btnUpload:
            {

            }
            case R.id.btnCancel:
            {
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                break;
            }

        }
    }

    /**
     * Displaying captured image/video on the screen
     * */
    private void previewMedia(boolean isImage,byte[] bytes) {
        // Checking whether captured media is image or video
        if (isImage) {
//            Log.d("your mum m98",filePath.toString() + filePath.length());
            imgPreview.setVisibility(View.VISIBLE);
            // bitmap factory
//            String s = filePath.toString();



            Bitmap myBitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            //Drawable d = new BitmapDrawable(getResources(), myBitmap);
            imgPreview.setImageBitmap(myBitmap);


        }
    }


    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }






}
