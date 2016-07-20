package mam.dama.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import mam.dama.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent login = new Intent(SplashScreenActivity.this, JoinHostActivity.class);
                Bundle preData = new Bundle();

                //Add data to bundle

                login.putExtra("PLACEHOLDER",preData);
                startActivity(login);
                finish();
            }
        },1500);
    }
}
