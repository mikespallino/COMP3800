package mam.dama.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import mam.dama.R;

public class JoinHostActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_STORAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_host);

        if (ContextCompat.checkSelfPermission(JoinHostActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(JoinHostActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(JoinHostActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_READ_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Move to the Host Playlist Picker activity when the host button is clicked
                    Button hostButton = (Button) findViewById(R.id.button_host);
                    hostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(JoinHostActivity.this, HostPlaylistPickerActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    // Move to the Join Event Picker Activity when the join button is clicked
                    Button joinButton = (Button) findViewById(R.id.button_join);
                    joinButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(JoinHostActivity.this, JoinEventPickerActivity.class);
                            startActivity(intent);
                        }
                    });

                } else {
                    finish();
                }
                return;
            }
        }
    }
}
