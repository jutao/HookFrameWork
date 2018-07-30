package jt.msi.hookframework;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by MSI on 2018/7/12.
 */

public class HookUtil {
    private Context context;

    public void hookHookMh(Context context) {
        this.context=context;
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Field sCurrentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            Object sCurrentActivityThread = sCurrentActivityThreadField.get(null);
            Field mHFild = activityThreadClass.getDeclaredField("mH");
            mHFild.setAccessible(true);
            Handler mH = (Handler) mHFild.get(sCurrentActivityThread);
            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            mCallbackField.set(mH, new ActivityMH(mH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hookStartActivity(Context context) {
        this.context = context;
        //还原 gDefault 成员变量 反射 调用一次
        Object defaultValue;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Class<?> activityManagerCls = Class.forName("android.app.ActivityManager");
                Field iActivityManagerSingleton = activityManagerCls.getDeclaredField("IActivityManagerSingleton");
                iActivityManagerSingleton.setAccessible(true);
                //因为是静态变量所以获取到的是默认值
                defaultValue = iActivityManagerSingleton.get(null);
            } else {
                Class<?> activityManagerNativeCls = Class.forName("android.app.ActivityManagerNative");
                Field gDefault = activityManagerNativeCls.getDeclaredField("gDefault");
                gDefault.setAccessible(true);

                //因为是静态变量所以获取到的是默认值
                defaultValue = gDefault.get(null);
            }
            //mInstance对象
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstance = singletonClass.getDeclaredField("mInstance");

            //还原IActivityManager对象，系统对象
            mInstance.setAccessible(true);
            Object iActivityManager = mInstance.get(defaultValue);
            Class<?> activityManagerIntercept = Class.forName("android.app.IActivityManager");
            StartActivty startActivtyMethod = new StartActivty(iActivityManager);

            //第二参数  是即将返回的对象 需要实现那些接口
            Object oldIactivityManager = Proxy.newProxyInstance(startActivtyMethod.getClass().getClassLoader(), new Class[]{activityManagerIntercept}, startActivtyMethod);
            //将系统的iActivityManager  替换成    自己通过动态代理实现的对象   oldIactivityManager对象  实现了 IActivityManager这个接口的所有方法
            mInstance.set(defaultValue, oldIactivityManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class StartActivty implements InvocationHandler {
        private Object iActivityManagerObject;

        public StartActivty(Object iActivityManagerObject) {
            this.iActivityManagerObject = iActivityManagerObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("startActivity".equals(method.getName())) {
                Log.i("INFO", "-----------------startActivity--------------------------");
                Intent intent = null;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        intent = (Intent) args[i];
                        index = i;
                        break;
                    }
                }
                Intent newIntent = new Intent();
                ComponentName componentName = new ComponentName(context, ProxyActivity.class);
                newIntent.setComponent(componentName);
                newIntent.putExtra("oldIntent", intent);
                args[index] = newIntent;
            }
            return method.invoke(iActivityManagerObject, args);
        }
    }

    class ActivityMH implements Handler.Callback {
        private Handler mH;

        public ActivityMH(Handler mH) {
            this.mH = mH;
        }

        @Override
        public boolean handleMessage(Message msg) {
            //LAUNCH_ACTIVITY ==100 即将要加载一个activity了
            if (msg.what == 100) {
                handleLuachActivity(msg);
            }
            mH.handleMessage(msg);
            return true;
        }

        private void handleLuachActivity(Message msg) {
            //还原
            Object obj = msg.obj;
            try {
                Field intentField = obj.getClass().getDeclaredField("intent");
                intentField.setAccessible(true);
                Intent realyIntent = (Intent) intentField.get(obj);
                Intent oldIntent = realyIntent.getParcelableExtra("oldIntent");
                if(oldIntent!=null){
                    if(context.getSharedPreferences("name",MODE_PRIVATE).getBoolean("login",false)){
                        realyIntent.setComponent(oldIntent.getComponent());
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
