package mam.dama.activity;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

import mam.dama.R;

public class HostPlaylistPickerActivity extends AppCompatActivity {

    private String eventNameText = "";
    private String eventNamePassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_playlist_picker);

        // Add the playlist items
        ArrayList<String> playlistItems = new ArrayList<>();
        for (int i = 0; i < 15; i ++) {
            playlistItems.add("Playlist #" + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                playlistItems);

        final ListView playlistView = (ListView) findViewById(R.id.playlistListView);
        playlistView.setAdapter(adapter);

        SearchView playlistSearchBar = (SearchView) findViewById(R.id.playlistSearchView);
        // TODO: make this actually search.

        // Display the Alert Dialog
        playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedPlaylist = (String) playlistView.getItemAtPosition(position);
                Log.v("DAMA", selectedPlaylist);
                AlertDialog.Builder builder = new AlertDialog.Builder(HostPlaylistPickerActivity.this);
                builder.setTitle("Name your event");

                LinearLayout dialogLayout = new LinearLayout(HostPlaylistPickerActivity.this);
                dialogLayout.setOrientation(LinearLayout.VERTICAL);

                final EditText eventName = new EditText(HostPlaylistPickerActivity.this);
                eventName.setInputType(InputType.TYPE_CLASS_TEXT);
                eventName.setHint("Event Name");

                final EditText eventPassword = new EditText(HostPlaylistPickerActivity.this);
                eventPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eventPassword.setHint("(Optional)");

                dialogLayout.addView(eventName);
                dialogLayout.addView(eventPassword);

                builder.setView(dialogLayout);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eventNameText = eventName.getText().toString();
                        eventNamePassword = eventPassword.getText().toString();
                        Log.v("DAMA [EVENT NAME]:", eventNameText);
                        Log.v("DAMA [EVENT PASSWORD]:", eventNamePassword);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }
}
