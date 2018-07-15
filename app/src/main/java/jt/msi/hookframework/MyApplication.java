package jt.msi.hookframework;

import android.app.Application;

/**
 * Created by MSI on 2018/7/12.
 */

public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        HookUtil hookUtil = new HookUtil();
        hookUtil.hookStartActivity();
    }
}
