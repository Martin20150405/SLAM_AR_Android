package com.martin.ads.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.martin.ads.slamar.R;
import com.martin.ads.utils.ZipHelper;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by Ads on 2016/11/28.
 */

public class ModelActivity extends Activity {
    private static final int REQUEST_PERMISSION = 233;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pending_layout);
        //"android.resource://" + getPackageName() + "/" +R.raw.clm_model

        if(checkPermission(Manifest.permission.CAMERA,REQUEST_PERMISSION))
            init();
    }

    private void init(){
        new ExtractModelTask(this).execute();
    }
    private boolean checkPermission(String permission,int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    showHint("Camera and SDCard access is required, please grant the permission in settings.");
                    finish();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    permission,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },
                            requestCode);
                }
                return false;
            }else return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                }else {
                    showHint("Camera and SDCard access is required, please grant the permission in settings.");
                    finish();
                }
                break;
            default:
                finish();
                break;
        }
    }

    private void showHint(String hint){
        Toast.makeText(this,hint , Toast.LENGTH_LONG).show();
    }

    class ExtractModelTask extends AsyncTask<Void,Void,Boolean>{
        Context context;

        public ExtractModelTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean createNew=false;
            String resDir = context.getExternalFilesDir("SLAM").getAbsolutePath();
            ZipHelper.saveFile(context,resDir,"CameraSettings.yaml","CameraSettings.yaml",createNew);
            ZipHelper.saveFile(context,resDir,"config.txt","config.txt",createNew);
            ZipHelper.saveFile(context,resDir,"ORBvoc.txt.arm.bin","ORBvoc.txt.arm.bin",createNew);
            //ZipHelper.upZipFile(new File(Environment.getExternalStorageDirectory().getPath()+"/CLM/clm_model.zip"),
//                        Environment.getExternalStorageDirectory().getPath()+"/CLM");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Intent intent=new Intent(ModelActivity.this,ArCamUIActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
