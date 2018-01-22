package com.android.prize.simple.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.android.prize.simple.R;
import com.android.prize.simple.model.SimpleDeviceProfile;
import com.android.prize.simple.ui.SimpleFrame;

import java.util.List;

public class EasyActivity extends Activity {

    private static final String TAG = "EasyActivity";
    private int viewWidth;
    public static int viewHeight;
    private float scale;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);


        scale = getResources().getDisplayMetrics().density;
        Point smallestSize = new Point();
        Point largestSize = new Point();
        Point realSize = new Point();
        Display display = getWindowManager().getDefaultDisplay();

        display = getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        viewWidth = display.getWidth();
        viewHeight = display.getHeight();
        display.getCurrentSizeRange(smallestSize, largestSize);
        SimpleDeviceProfile simpeProfile = new SimpleDeviceProfile(this, Math.min(smallestSize.x, smallestSize.y),
                Math.min(largestSize.x, largestSize.y), realSize.x, realSize.y,
                dm.widthPixels, dm.heightPixels);
        setContentView(R.layout.simple_main);
        SimpleFrame simple_main = (SimpleFrame) findViewById(R.id.simple_frame);
        simple_main.setActivity(this);
        Log.i(TAG, "easyActivity,启动");

//        try {
//            getAllPackageName();
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    private void getAllPackageName() throws PackageManager.NameNotFoundException {
        PackageManager packageManager = this.getApplication().getPackageManager();

        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
        for (PackageInfo info :
                installedPackages) {

            ApplicationInfo applicationInfo = info.applicationInfo;
            String packageName = applicationInfo.packageName;
            CharSequence charSequence = applicationInfo.loadLabel(packageManager);
            String s = applicationInfo.toString();
            Log.e(TAG, "####packageName:" + packageName + ", label:" + charSequence);
            getActivities(this, packageName);
        }
    }

    private void getActivities(Activity activity, String packageName) {
        Intent localIntent = new Intent("android.intent.action.MAIN", null);
        localIntent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> appList = activity.getPackageManager().queryIntentActivities(localIntent, 0);
        for (int i = 0; i < appList.size(); i++) {
            ResolveInfo resolveInfo = appList.get(i);
            String packageStr = resolveInfo.activityInfo.packageName;
            if (packageStr.equals(packageName)) {
                //这个就是你想要的那个Activity
                Log.e("EasyActivity", "*********" + resolveInfo.activityInfo.name);
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

}
