package jt.msi.hookframework;

import android.app.ActivityManager;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by MSI on 2018/7/12.
 */

public class HookUtil {
    public  void hookStartActivity() {
        //还原 gDefault 成员变量 反射 调用一次
        try {
            Class<?> activityManagerNativeCls = Class.forName("android.app.ActivityManagerNative");
            Field gDefault = activityManagerNativeCls.getDeclaredField("gDefault");
            gDefault.setAccessible(true);

            //因为是静态变量所以获取到的是默认值
            Object defaultValue = gDefault.get(null);
            //mInstance对象
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstance = singletonClass.getDeclaredField("mInstance");

            //还原IActivityManager对象，系统对象
            mInstance.setAccessible(true);
            Object iActivityManager = mInstance.get(defaultValue);
            Class<?> activityManagerIntercept = Class.forName("android.app.IActivityManager");
            StartActivty startActivtyMethod = new StartActivty(iActivityManager);

            //            第二参数  是即将返回的对象 需要实现那些接口
            Object oldIactivityManager = Proxy.newProxyInstance(startActivtyMethod.getClass().getClassLoader(), new Class[]{activityManagerIntercept}, startActivtyMethod);
            //            将系统的iActivityManager  替换成    自己通过动态代理实现的对象   oldIactivityManager对象  实现了 IActivityManager这个接口的所有方法
            mInstance.set(defaultValue,oldIactivityManager);
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
            }
            return method.invoke(iActivityManagerObject, args);
        }
    }
}
