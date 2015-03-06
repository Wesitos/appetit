package pe.hackspace.appetit;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import android.view.TextureView;


import java.io.IOException;
import java.util.List;

public class NavigationActivity extends Activity implements TextureView.SurfaceTextureListener {
    private Camera mCamera;
    private TextureView mTextureView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(this);

        setContentView(mTextureView);
    }

    private static Pair<Integer, Integer> getMaxSize(List<Camera.Size> list, int TextureWidth, int TextureHeight)
    {
        int width = 0;
        int height = 1;

        double TextureAspect = (double)TextureHeight / TextureWidth;
        double LastAspect = 10;

        for (Camera.Size size : list) {
            Log.d("Camera", "PreviewSize: " + Integer.toString(size.width) + ":" + Integer.toString(size.height));
            if (Math.abs(TextureAspect - (double)width/height) < Math.abs(TextureAspect - LastAspect))
            {
                width = size.width;
                height = size.height;
                LastAspect = (double) width / height;
            }
        }

        return new Pair<Integer, Integer>(width, height);
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open();
        Log.d("Texture", "Texture Size: " + Integer.toString(width) + ":" + Integer.toString(height));
        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        Pair<Integer, Integer> size = getMaxSize(params.getSupportedPreviewSizes(), width, height);

        Log.d("Camera", "Selected preview size: " + Integer.toString(size.first) + ":" + Integer.toString(size.second));
        params.setPreviewSize(size.first, size.second);
        mCamera.setParameters(params);
        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Invoked every time there's a new Camera preview frame
    }

}
