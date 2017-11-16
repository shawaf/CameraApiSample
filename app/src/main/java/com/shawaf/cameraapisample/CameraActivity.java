package com.shawaf.cameraapisample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.shawaf.cameraapisample.takepic.Camera1Fragment;
import com.shawaf.cameraapisample.utilites.CommonStrings;
import com.shawaf.cameraapisample.utilites.RuntimePermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * CameraActivity CLass
 * Init ViewPager
 * Init View Shared Resources like Fonts
 * Requset the Rquired Permissions
 */
public class CameraActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private String mainFrag = "main", displayFrag = "display";
    public static String TYPE_KEY = "type";
    public static String PATH_KEY = "path";


    private static final String[] CAM_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int CAM_ASK_PERMISSION_CODE = 200;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newcam);

        ButterKnife.bind(this);
        askForCameraPermisions();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void initFragment() {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        Camera1Fragment mainFragment = new Camera1Fragment();
        fragmentTransaction.replace(R.id.cam_frag_holder, mainFragment, mainFrag);
        fragmentTransaction.commitAllowingStateLoss();

    }


    private void askForCameraPermisions() {
        if (RuntimePermissions.shouldAskPermission() == true) {
            RuntimePermissions.askForCameraPermisionsInActivity(this, CAM_PERMISSIONS, CAM_ASK_PERMISSION_CODE);
        } else {
            initFragment();
        }
    }


    public void openDisplayFragment(String filePath, String fileType) {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        //  fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);

//        ShareFragment displayFragment = new ShareFragment();
//        Bundle b = new Bundle();
//        b.putString(PATH_KEY, filePath);
//        b.putString(TYPE_KEY, fileType);
//        displayFragment.setArguments(b);
//
//        fragmentTransaction.replace(R.id.cam_frag_holder, displayFragment, displayFrag);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();

    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

        switch (permsRequestCode) {
            case 200:
                if (grantResults.length > 0 || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initFragment();
                } else {
                    Toast.makeText(this, "Permission Is Required to open the Camera", Toast.LENGTH_LONG).show();
                    this.finish();
                }
                break;

        }
    }

    public void closeActivity(String filePath, String fileType) {
        Intent intent = new Intent();
        intent.putExtra(PATH_KEY, filePath);
        intent.putExtra(TYPE_KEY, fileType);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void closeActivity() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            closeActivity();
        }
    }


}
