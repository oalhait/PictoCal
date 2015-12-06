package io.oalhait.pictocal;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

import android.view.WindowManager;


public class CameraView extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context mContext;

    public CameraView(Context context, Camera camera){
        super(context);
        this.mContext = context;
        Camera.Parameters params = camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(params);
        mCamera = camera;
        mCamera.setDisplayOrientation(90);
        //get the holder and set this class as the callback, so we can get activity_main data here
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }




    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try{
            //when the surface is created, we can set the activity_main to draw images in this surfaceholder
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();
            tryAutoFocus();
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceCreated " + e.getMessage());

        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        //before changing the application orientation, you need to stop the preview, rotate and then start it again
        if(mHolder.getSurface() == null)//check if the surface is ready to receive activity_main data
            return;

        try{
            mCamera.stopPreview();
        } catch (Exception e){
            //this will happen when you are trying the activity_main if it's not running
        }

        //now, recreate the activity_main preview
        try{
            Camera.Parameters params = mCamera.getParameters();
            Display display =
                    ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            if(display.getRotation() == Surface.ROTATION_0)
            {
                params.setPreviewSize(height, width);
                mCamera.setDisplayOrientation(90);
            }

            if(display.getRotation() == Surface.ROTATION_90)
            {
                params.setPreviewSize(width, height);
                mCamera.setDisplayOrientation(0);

            }
//            don't really care about upside down
            if(display.getRotation() == Surface.ROTATION_180)
            {
                params.setPreviewSize(height, width);
            }

            if(display.getRotation() == Surface.ROTATION_270)
            {
                params.setPreviewSize(width, height);
                mCamera.setDisplayOrientation(180);
            }
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            tryAutoFocus();


        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceChanged " + e.getMessage());
        }
    }

    private void tryAutoFocus(){
        if(mCamera.getParameters().getFocusMode().compareTo("FOCUS_MODE_AUTO") == 0 ||
                mCamera.getParameters().getFocusMode().compareTo("FOCUS_MODE_MACRO") == 0){
            mCamera.autoFocus(null);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //our app has only one screen, so we'll destroy the activity_main in the surface
        //if you are unsing with more screens, please move this code your activity
        mCamera.stopPreview();
        mCamera.release();
    }



}
