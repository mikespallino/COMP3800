package mam.dama.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import mam.dama.R;

public class JoinHostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_host);

        // Move to the Host Playlist Picker activity when the host button is clicked
        Button hostButton = (Button) findViewById(R.id.button_host);
        hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JoinHostActivity.this, HostPlaylistPickerActivity.class);
                startActivity(intent);
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
    }
}
