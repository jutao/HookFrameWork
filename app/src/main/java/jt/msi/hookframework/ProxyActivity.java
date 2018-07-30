package jt.msi.hookframework;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ProxyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proxy);
        final Intent oldIntent = getIntent().getParcelableExtra("oldIntent");
        getSharedPreferences("name",MODE_PRIVATE).edit().putBoolean("login",true).apply();
        findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(oldIntent);
            }
        });
    }
}
