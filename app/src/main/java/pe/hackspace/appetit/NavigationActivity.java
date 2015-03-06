package pe.hackspace.appetit;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

public class NavigationActivity extends Activity implements
        SurfaceHolder.Callback {

    Camera mCamera;
    SurfaceView mPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mPreview = (SurfaceView)findViewById(R.id.preview);
        mPreview.getHolder().addCallback(this);

        mCamera = Camera.open();
        Log.i("Navigation", "OnCreate");
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera.stopPreview();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCamera.release();
    }

    //Surface Callback Methods
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,
                               int width, int height) {
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        Camera.Parameters params = mCamera.getParameters();
        //Get the device's supported sizes and pick the first,
        // which is the largest
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();

        Camera.Size selected = sizes.get(sizes.size() - 1);
        params.setPreviewSize(selected.width,selected.height);
        List<int[]>  rates = params.getSupportedPreviewFpsRange();
        int [] maxRate = rates.get(rates.size() - 1);
        params.setPreviewFpsRange(maxRate[0],maxRate[1]);

        int previewSurfaceHeight = mPreview.getHeight();
        int previewSurfaceWidth = mPreview.getWidth();

        

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            mCamera.setDisplayOrientation(90);
            lp.height = previewSurfaceHeight;
            lp.width = (int) (previewSurfaceHeight );
        } else {
            mCamera.setDisplayOrientation(0);
            lp.width = previewSurfaceWidth;
            lp.height = (int) (previewSurfaceWidth);
        }

        if (params.isZoomSupported())
            params.setZoom(0);
        mCamera.setParameters(params);


        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("Camera", "Error starting camera preview: " + e.getMessage());
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("Navigation", "surfaceCreated");
        Camera.Parameters params = mCamera.getParameters();
        /*
        Camera.Size previewSize = params.getSupportedPreviewSizes().get(0);
        int previewWidth = mPreview.getWidth();
        int mPreviewHeight = mPreview.getHeight();
        int scale = Math.max(previewSize.width);
        */
        for(Camera.Size size: mCamera.getParameters().getSupportedPreviewSizes()){
            Log.d("Camera", "Supported Size: " + Integer.toString(size.width) + ":" + Integer.toString(size.height));
        }
        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d("Camera","Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.release();
    }
}