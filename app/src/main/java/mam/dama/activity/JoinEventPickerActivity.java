package mam.dama.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

import mam.dama.R;

public class JoinEventPickerActivity extends AppCompatActivity {

    private String eventNameText = "";
    private String eventNamePassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event_picker);

        // Add the event items
        ArrayList<String> eventItems = new ArrayList<>();
        for (int i = 0; i < 15; i ++) {
            eventItems.add("Event #" + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                eventItems);

        final ListView eventView = (ListView) findViewById(R.id.eventListView);
        eventView.setAdapter(adapter);

        SearchView eventSearchBar = (SearchView) findViewById(R.id.eventSearchView);
        // TODO: make this actually search.

        // Display the Alert Dialog
        eventView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String selectedEvent = (String) eventView.getItemAtPosition(position);
                Log.v("DAMA", selectedEvent);
                AlertDialog.Builder builder = new AlertDialog.Builder(JoinEventPickerActivity.this);
                builder.setTitle(selectedEvent);

                LinearLayout dialogLayout = new LinearLayout(JoinEventPickerActivity.this);
                dialogLayout.setOrientation(LinearLayout.VERTICAL);


                final EditText eventPassword = new EditText(JoinEventPickerActivity.this);
                eventPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eventPassword.setHint("Password: (Optional)");

                //dialogLayout.addView(selectedEvent);
                dialogLayout.addView(eventPassword);

                builder.setView(dialogLayout);

                builder.setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eventNameText = selectedEvent;
                        eventNamePassword = eventPassword.getText().toString();
                        Log.v("DAMA [EVENT NAME]:", eventNameText);
                        Log.v("DAMA [EVENT PASSWORD]:", eventNamePassword);
                        Intent hubIntent = new Intent(JoinEventPickerActivity.this, HubActivity.class);
                        Bundle hubBundle = new Bundle();
                        hubBundle.putString("event_name", eventNameText);
                        //TODO: Fill this in with the songs from the event.
                        hubBundle.putStringArrayList("playlist_songs", new ArrayList<String>());
                        hubIntent.putExtras(hubBundle);
                        startActivity(hubIntent);
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
