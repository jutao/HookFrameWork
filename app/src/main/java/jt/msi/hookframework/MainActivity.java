package jt.msi.hookframework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
/**
 * Created by MSI on 2018/7/12.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSharedPreferences("name",MODE_PRIVATE).edit().putBoolean("login",false).apply();
        setContentView(R.layout.activity_main);
    }

    public void jump1(View view) {
        Intent intent = new Intent(this, OneActivity.class);
        startActivity(intent);
    }
    public void jump2(View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }
    public void jump3(View view) {
        Intent intent = new Intent(this, ThreeActivity.class);
        startActivity(intent);
    }
    public void jump4(View view) {
        Intent intent = new Intent(this,ThirdActivity.class);
        startActivity(intent);
    }

    public void logout(View view) {
        SharedPreferences share = this.getSharedPreferences("jutao", MODE_PRIVATE);//实例化
        SharedPreferences.Editor editor = share.edit();
        editor.putBoolean("login",false);
        Toast.makeText(this, "退出登录成功",Toast.LENGTH_SHORT).show();
        editor.commit();
    }

}
