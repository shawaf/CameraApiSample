package com.shawaf.cameraapisample.takepic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.shawaf.cameraapisample.CameraActivity;
import com.shawaf.cameraapisample.R;
import com.shawaf.cameraapisample.utilites.CommonStrings;
import com.shawaf.cameraapisample.utilites.ImageUtilites;
import com.shawaf.cameraapisample.utilites.VideoUtilites;
import com.shawaf.cameraapisample.views.SquareCameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * Created by Shawaf on 12/17/2016.
 */

public class Camera1Fragment extends Fragment implements SurfaceHolder.Callback, Camera.PictureCallback {

    private View view;
    @BindView(R.id.cam1pic_texture)
    SquareCameraPreview squareCameraPreview;
    @BindView(R.id.capture_image_button)
    ImageButton captureBtn;
    @BindView(R.id.takepic_switchcam_btn)
    ImageButton switchBtn;
    @BindView(R.id.record_video_timer_tv)
    TextView recordTimerTV;
    @BindView(R.id.takepic_flash_btn)
    ImageButton flashBtn;


    public static final String TAG = Camera1Fragment.class.getSimpleName();
    public static final String CAMERA_ID_KEY = "camera_id";
    public static final String CAMERA_FLASH_KEY = "flash_mode";
    public static final String IMAGE_INFO = "image_info";

    private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 640;

    private int mCameraID = -1;
    private String mFlashMode;
    private String curCam;
    private String FRONT_CAM = "front_cam";
    private String BACK_CAM = "back_cam";
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;

    private boolean mIsSafeToTakePhoto = false;

    private ImageParameters mImageParameters;
    private CameraOrientationListener mOrientationListener;
    private CameraActivity cameraViewActivity;
    private File mPicFile, mVideoFile;
    private MediaRecorder mediaRecorder;
    private boolean recording = false;
    private Handler videoTimerHandler;
    private Runnable videoTimerRunnable;
    private boolean longClick = false;
    private int timerDuration;
    private List<Camera.Size> mSupportedPreviewSizes;

    public static Camera1Fragment newInstance() {
        return new Camera1Fragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOrientationListener = new CameraOrientationListener(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraID = getBackCameraID();
        mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
        mImageParameters = new ImageParameters();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_cam_takepic, container, false);
        cameraViewActivity = (CameraActivity) getActivity();

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, getActivity());

        squareCameraPreview.getHolder().addCallback(Camera1Fragment.this);

        mImageParameters.mIsPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        setupFlashMode();
        curCam = BACK_CAM;
        initUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCamera == null) {
            restartPreview();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
//        Log.d(TAG, "onSaveInstanceState");
        outState.putInt(CAMERA_ID_KEY, mCameraID);
        outState.putString(CAMERA_FLASH_KEY, mFlashMode);
        outState.putParcelable(IMAGE_INFO, mImageParameters);
        super.onSaveInstanceState(outState);
    }


    private void setupFlashMode() {
        View view = getView();
        if (view == null) return;

        //  final TextView autoFlashIcon = (TextView) view.findViewById(R.id.auto_flash_icon);
        if (Camera.Parameters.FLASH_MODE_AUTO.equalsIgnoreCase(mFlashMode)) {
            //        autoFlashIcon.setText("Auto");
        } else if (Camera.Parameters.FLASH_MODE_ON.equalsIgnoreCase(mFlashMode)) {
            //        autoFlashIcon.setText("On");
        } else if (Camera.Parameters.FLASH_MODE_OFF.equalsIgnoreCase(mFlashMode)) {
            //        autoFlashIcon.setText("Off");
        }
    }

    private void initUI() {
        captureBtn.setOnTouchListener(new View.OnTouchListener() {
            public float MIN_DISTANCE = 50;
            public float x1;


            public boolean onTouch(View v, MotionEvent event) {

                int action = MotionEventCompat.getActionMasked(event);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        longClick = false;
                        x1 = event.getX();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (event.getEventTime() - event.getDownTime() > 500 && Math.abs(event.getX() - x1) < MIN_DISTANCE) {
                            Log.e(TAG, "Long Clike == true");
                            if (videoTimerHandler == null) startVideoTimer();
                            longClick = true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (longClick == true) {
                            longClick = false;
                            Log.e(TAG, "Lo0000000ng preess");

                        } else {
                            Log.e(TAG, "Shooooort preess");
                            takePicture();
                        }

                }
                return true;
            }
        });


    }

    private void startVideoTimer() {
        timerDuration = 0;
        videoTimerHandler = new Handler();
        videoTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (longClick == true) {
                    Log.e(TAG, "Start Timer");
                    if (recording == false) takeVideo();
                    recordTimerTV.setVisibility(View.VISIBLE);
                    timerDuration++;
                    recordTimerTV.setText(VideoUtilites.milliSecondsToTimer(timerDuration * 500));
                    captureBtn.setImageResource(R.drawable.startrecord_video);
                    videoTimerHandler.postDelayed(this, 500);
                } else if (recording == true) {
                    Log.e(TAG, "Stop Timer");
                    takeVideo();
                    resetVideoTimer();
                }
            }
        };
        videoTimerHandler.post(videoTimerRunnable);
    }

    private void resetVideoTimer() {
        //Update UI
        timerDuration = 0;
        captureBtn.setImageResource(R.drawable.stoprecord_circle);
        // Reset Handler
        if (videoTimerHandler != null) {
            videoTimerHandler.removeCallbacks(videoTimerRunnable);
            videoTimerHandler = null;
        }

    }

    /**
     * Setup the camera pictures parameters
     */
    private void setupPicCamera() {
        mPicFile = ImageUtilites.createPublicFile(ImageUtilites.CAM_PIC_TYPE);
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        Camera.Size bestPreviewSize = PreviewParameters.getInstance().determineOptimalPreviewSize(parameters,squareCameraPreview.getWidth(),squareCameraPreview.getHeight());
        Camera.Size bestPictureSize = PreviewParameters.getInstance().determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);


        // Set continuous picture focus, if it's supported
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        //  final View changeCameraFlashModeBtn = getView().findViewById(R.id.flash);
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(mFlashMode)) {
            parameters.setFlashMode(mFlashMode);
            //changeCameraFlashModeBtn.setVisibility(View.VISIBLE);
        } else {
            //changeCameraFlashModeBtn.setVisibility(View.INVISIBLE);
        }

        // Lock in the changes
        mCamera.setParameters(parameters);
    }

    /**
     * Setup the camera Videos parameters
     */
    private boolean setupVideoCamera() {
        mVideoFile = ImageUtilites.createPublicFile(ImageUtilites.CAM_VIDEO_TYPE);
        if (mCamera == null) {
            getCamera(mCameraID);
        }
        mediaRecorder = new MediaRecorder();

        //Set the orientation of the display camera
        determineDisplayOrientation();

        mediaRecorder.setOrientationHint(90);
        mCamera.setDisplayOrientation(mImageParameters.mDisplayOrientation);

        //SetupPreview Size
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size bestPreviewSize = PreviewParameters.getInstance().determineOptimalPreviewSize(parameters,squareCameraPreview.getWidth(),squareCameraPreview.getHeight());
        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        mCamera.setParameters(parameters);


        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

        mediaRecorder.setOutputFile(mVideoFile.getAbsolutePath());
        mediaRecorder.setMaxDuration(600000); // Set max duration 60 sec.
        mediaRecorder.setMaxFileSize(0); // Set max file size 5M

        mediaRecorder.setPreviewDisplay(squareCameraPreview.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    /**
     * take Camera picture
     */
    private void takePicture() {
        cameraViewActivity.setTitle(CommonStrings.photo_title);
        if (mIsSafeToTakePhoto) {
            setSafeToTakePhoto(false);
            Log.e(TAG, "Taaaaaaaaaaking Phooooooooootooooooooooooooo");


            // Shutter callback occurs after the image is captured. This can
            // be used to trigger a sound to let the user know that image is taken
            Camera.ShutterCallback shutterCallback = null;

            // Raw callback occurs when the raw image data is available
            Camera.PictureCallback raw = null;

            // postView callback occurs when a scaled, fully processed
            // postView image is available.
            Camera.PictureCallback postView = null;

            // jpeg callback occurs when the compressed image is available
            mCamera.takePicture(shutterCallback, raw, postView, this);
        }
    }

    /**
     * take Camera Video
     */
    private void takeVideo() {
        cameraViewActivity.setTitle(CommonStrings.video_title);
        if (recording) {
            // stop recording and release camera
            mediaRecorder.stop();
            releaseMediaRecorder();
            recording = false;
            //Open Display Fragment
            insertAndOpen(CommonStrings.VIDEO_TYPE);
            //  Log.e(TAG, "Stop Camera Recording");
        } else {

            //Release Camera before MediaRecorder start
            releaseCamera();

            if (!setupVideoCamera()) {
                Toast.makeText(getActivity(),
                        "Fail in prepareMediaRecorder()!\n - Ended -",
                        Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
            Log.e(TAG, "Start Camera Recording");
            mediaRecorder.start();
            recording = true;
        }
    }


    private void setSafeToTakePhoto(final boolean isSafeToTakePhoto) {
        mIsSafeToTakePhoto = isSafeToTakePhoto;
    }

    private void setCameraFocusReady(final boolean isFocusReady) {
        if (this.squareCameraPreview != null) {
            squareCameraPreview.setIsFocusReady(isFocusReady);
        }
    }

    private int getFrontCameraID() {
        PackageManager pm = getActivity().getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        return getBackCameraID();
    }

    private int getBackCameraID() {
        return Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    private void getCamera(int cameraID) {
        releaseCameraAndPreview();
        try {
            mCamera = Camera.open(cameraID);
            squareCameraPreview.setCamera(mCamera);
        } catch (Exception e) {
            Log.d(TAG, "Can't open camera with id " + cameraID);
            e.printStackTrace();
        }
    }

    /**
     * Start the camera preview
     */
    private void startCameraPreview() {
        determineDisplayOrientation();
        setupPicCamera();
        setUpPreviewSize();

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();

            setSafeToTakePhoto(true);
            setCameraFocusReady(true);
        } catch (IOException e) {
            Log.d(TAG, "Can't start camera preview due to IOException " + e);
            e.printStackTrace();
        }
    }

    private void setUpPreviewSize() {
        Camera.Parameters parameters = mCamera.getParameters();


        Camera.Size bestPreviewSize = PreviewParameters.getInstance().determineOptimalPreviewSize(parameters,squareCameraPreview.getWidth(),squareCameraPreview.getHeight());;
        Camera.Size bestPictureSize = PreviewParameters.getInstance().determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);

        mCamera.setParameters(parameters);
    }

    /**
     * Stop the camera preview
     */
    private void stopCameraPreview() {
        setSafeToTakePhoto(false);
        setCameraFocusReady(false);

        // Nulls out callbacks, stops face detection
        mCamera.stopPreview();
        squareCameraPreview.setCamera(null);
    }

    /**
     * Restart the camera preview
     */
    private void restartPreview() {
        if (mCamera != null) {
            stopCameraPreview();
            mCamera.release();
            mCamera = null;
        }

        getCamera(mCameraID);
        startCameraPreview();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;

        getCamera(mCameraID);
        startCameraPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // The surface is destroyed with the visibility of the SurfaceView is set to View.Invisible
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case 1:
                Uri imageUri = data.getData();
                Log.d(TAG, "Taken Picture Uri : " + imageUri);
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * A picture has been taken
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        //int rotation = getPhotoRotation();
        Log.e(TAG, "File Path : " + mPicFile.getPath());
        try {
            FileOutputStream fos = new FileOutputStream(mPicFile.getPath());
            Bitmap convBitmap = ImageUtilites.rotatePicture(getActivity(), getPhotoRotation(), data);
            convBitmap.compress(Bitmap.CompressFormat.PNG, 50, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

        insertAndOpen(CommonStrings.IMG_TYPE);
        setSafeToTakePhoto(true);
        restartPreview();


    }

    /**
     * Insert the picture to gallery
     * open the display fragment
     */
    private void insertAndOpen(String type) {
        Uri uri = null;
        String realPath = null;

        switch (type) {
            case CommonStrings.IMG_TYPE:
                // uri = ImageUtilites.insertImgToGal(getActivity(), mPicFile, type);
                realPath = mPicFile.getAbsolutePath();
                break;
            case CommonStrings.VIDEO_TYPE:
                // uri = ImageUtilites.insertImgToGal(getActivity(), mVideoFile, type);
                realPath = mVideoFile.getAbsolutePath();
                break;
        }
        // realPath = ImageUtilites.getRealPathFromURI(getActivity(), uri);
        cameraViewActivity.openDisplayFragment(realPath, type);

    }

    private int getPhotoRotation() {
        int rotation;
        int orientation = mOrientationListener.getRememberedNormalOrientation();
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, info);

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {
            rotation = (info.orientation + orientation) % 360;
        }

        return rotation;
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly
     */
    private void determineDisplayOrientation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);

        // Clockwise rotation needed to align the window display to the natural position
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        int displayOrientation;

        // CameraInfo.Orientation is the angle relative to the natural position of the device
        // in clockwise rotation (angle that is rotated clockwise from the natural position)
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mImageParameters.mDisplayOrientation = displayOrientation;
        mImageParameters.mLayoutOrientation = degrees;

        mCamera.setDisplayOrientation(mImageParameters.mDisplayOrientation);
    }

    /**
     * When orientation changes, onOrientationChanged(int) of the listener will be called
     */
    private static class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }
        }

        /**
         * @param degrees Amount of clockwise rotation from the device's natural position
         * @return Normalized degrees to just 0, 90, 180, 270
         */
        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }

            if (degrees > 45 && degrees <= 135) {
                return 90;
            }

            if (degrees > 135 && degrees <= 225) {
                return 180;
            }

            if (degrees > 225 && degrees <= 315) {
                return 270;
            }

            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        public void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        public int getRememberedNormalOrientation() {
            rememberOrientation();
            return mRememberedNormalOrientation;
        }
    }

    @Nullable
    @Optional
    @OnClick({R.id.takepic_switchcam_btn, R.id.takepic_flash_btn})
    public void onBtnClick(View v) {
        switch (v.getId()) {
            case R.id.takepic_switchcam_btn:
                if (curCam.equals(BACK_CAM)) {
                    mCameraID = getFrontCameraID();
                    curCam = FRONT_CAM;
                } else {
                    mCameraID = getBackCameraID();
                    curCam = BACK_CAM;
                }
                if (mCameraID != -1) {
                    restartPreview();
                }
                break;
            case R.id.takepic_flash_btn:
                if (mFlashMode.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_ON;
                    flashBtn.setImageResource(R.drawable.ic_flash_on_white_24dp);
                }else if (mFlashMode.equals(Camera.Parameters.FLASH_MODE_ON)) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
                    flashBtn.setImageResource(R.drawable.ic_flash_off_white_24dp);
                }else {
                    mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
                    flashBtn.setImageResource(R.drawable.ic_flash_auto_white_24dp);
                }
                if (mCameraID != -1) {
                    restartPreview();
                }
                break;
        }
    }

    /**
     * On Exit the View #####################
     * Exit The Camera Functions
     */
    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            squareCameraPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onStop() {
        // stop the preview
        if (mCamera != null) {
            stopCameraPreview();
            releaseMediaRecorder();
            releaseCamera();
        }
        super.onStop();
    }
}
