package mam.dama.activity;

import android.content.DialogInterface;
import android.content.Intent;
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
        // TODO: This is a placeholder for now. Update this later when we bring in the API.
        final ArrayList<String> playlistItems = new ArrayList<>();
//        for (int i = 0; i < 15; i ++) {
//            playlistItems.add("Playlist #" + i);
//        }
        playlistItems.add("Thing");
        playlistItems.add("andy");
        playlistItems.add("Rock Songs");
        playlistItems.add("90's Hits");
        playlistItems.add("Folk Music");
        final ArrayList<String> playlistItemsCopy = new ArrayList<>(playlistItems);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                playlistItems);

        final ListView playlistView = (ListView) findViewById(R.id.playlistListView);
        playlistView.setAdapter(adapter);

        SearchView playlistSearchBar = (SearchView) findViewById(R.id.playlistSearchView);
        playlistSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Fake it till we make it!
                for (int i = 0; i < playlistItems.size(); i++) {
                    if (!playlistItems.get(i).toLowerCase().contains(query.toLowerCase())) {
                        playlistItems.remove(i);
                    }
                }
                adapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                for (int i = 0; i < playlistItemsCopy.size(); i++) {
                    if (!playlistItems.contains(playlistItemsCopy.get(i))) {
                        playlistItems.add(playlistItemsCopy.get(i));
                    }
                }
                adapter.notifyDataSetChanged();
                return false;
            }

        });

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
                        Intent hubIntent = new Intent(HostPlaylistPickerActivity.this, HubActivity.class);
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

    private int levenshteinDistance (CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for(int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost; cost = newcost; newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }
}
