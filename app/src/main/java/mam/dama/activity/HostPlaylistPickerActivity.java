package mam.dama.activity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.INotificationSideChannel;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.*;

import mam.dama.R;

public class HostPlaylistPickerActivity extends AppCompatActivity {
    private String eventNameText = "";
    private String eventNamePassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_playlist_picker);

        // The following is to query the device for playlists
        final String[] getAll = {"*"};
        final Uri tempPlaylistURI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        final ContentResolver resolver = this.getContentResolver();
        final String id = MediaStore.Audio.Playlists._ID;
        final String name = MediaStore.Audio.Playlists.NAME;
        final Cursor playlistCursor = resolver.query(tempPlaylistURI, getAll, null, null, null);

        // This constructs a map of playlist name -> playlist id
        // we show the name to the user, but we use the id internally
        // to get the playlist tracks
        final HashMap<String, Long> playlistIDMap = new HashMap<>();

        if(playlistCursor.getCount() == 0) {
            playlistIDMap.put("No playlists found!", -1l);
        } else {
            for (int i = 0; i < playlistCursor.getCount(); i++) {
                playlistCursor.moveToPosition(i);
                String nameText = playlistCursor.getString(playlistCursor.getColumnIndex(name));
                long idLong = playlistCursor.getLong(playlistCursor.getColumnIndex(id));
                playlistIDMap.put(nameText, idLong);
            }
        }
        playlistCursor.close();

        // Here we create the lists to populate the list of playlists displayed to the user
        final ArrayList<String> playlistItems = new ArrayList<>(playlistIDMap.keySet());
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
                // Here we are *attempting* to to rudimentary searches through the playlist for text
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
                // If the search text has changed, we need to change our data back to the original list
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
                final String selectedPlaylist = (String) playlistView.getItemAtPosition(position);
                final long selectedPlaylistId = playlistIDMap.get(selectedPlaylist);
                final ArrayList<String> playlistSongs = new ArrayList<>();
                final ArrayList<String> allSongs = new ArrayList<String>();

                // The following sets up the Alert Dialog to create a new event.
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

                // The following is to define what happens when the user clicks either button on the Alert.
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get all of the tracks for the selected playlist
                        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", selectedPlaylistId);
                        final Uri all_uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        Cursor all_media = resolver.query(all_uri, null, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, null);
                        Cursor tracks = resolver.query(uri, new String[] {"*"}, null, null, null);

                        if(tracks != null) {
                            for (int i = 0; i < tracks.getCount(); i++) {
                                tracks.moveToPosition(i);
                                int dataIndex = tracks.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                                String trackName = tracks.getString(dataIndex);
                                playlistSongs.add(trackName);
                            }
                            tracks.close();
                        }

                        if(all_media != null) {
                            for (int i = 0; i < all_media.getCount(); i++) {
                                all_media.moveToPosition(i);
                                allSongs.add(all_media.getString(all_media.getColumnIndex(MediaStore.Audio.Media.DATA)));
                            }
                        }

                        // Log important stuff and switch to the hub activity
                        eventNameText = eventName.getText().toString();
                        eventNamePassword = eventPassword.getText().toString();
                        Log.v("DAMA [EVENT NAME]:", eventNameText);
                        Log.v("DAMA [EVENT PASSWORD]:", eventNamePassword);
                        Intent hubIntent = new Intent(HostPlaylistPickerActivity.this, HubActivity.class);
                        Bundle hubBundle = new Bundle();
                        hubBundle.putString("event_name", eventNameText);
                        hubBundle.putString("event_password", eventNamePassword);
                        hubBundle.putString("playlist_name", selectedPlaylist);
                        hubBundle.putStringArrayList("playlist_songs", playlistSongs);
                        hubIntent.putExtras(hubBundle);

                        CreateEvent createEvent = new CreateEvent(eventNameText, eventNamePassword, selectedPlaylist, playlistSongs, allSongs);
                        createEvent.execute();

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

    class CreateEvent extends AsyncTask<String, Void, String> {

        private String eventNameText, eventNamePassword, selectedPlaylist;
        private ArrayList<String> playlistSongs, allSongs;

        public CreateEvent(String eventName, String eventPassword, String selectedPlaylist, ArrayList<String> playlistSongs, ArrayList<String> allSongs){
            super();
            this.eventNameText = eventName;
            this.eventNamePassword = eventPassword;
            this.selectedPlaylist = selectedPlaylist;
            this.playlistSongs = playlistSongs;
            this.allSongs = allSongs;
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject hostData = new JSONObject();
            try {
                hostData.put("event_name", eventNameText);
                if (eventNamePassword == "") {
                    hostData.put("event_password", JSONObject.NULL);
                } else {
                    hostData.put("event_password", eventNamePassword);
                }
                hostData.put("key", "DAMA");
                hostData.put("location", "Boston");
                hostData.put("playlist_name", selectedPlaylist);
                hostData.put("playlist_songs", new JSONArray(playlistSongs));
                hostData.put("all_songs", new JSONArray(allSongs));
            } catch (org.json.JSONException e) {
                Log.v("DAMA", "SHIT BROKE YO.");
            }

            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.241.149.243:8080/host");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(true);
                conn.connect();

                OutputStream sendData = conn.getOutputStream();
                OutputStreamWriter streamWriter = new OutputStreamWriter(sendData, "UTF-8");
                streamWriter.write(hostData.toString());
                streamWriter.flush();
                streamWriter.close();

                Log.v("DAMA", hostData.toString());

                InputStream in = new BufferedInputStream(conn.getInputStream());

                StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String read;

                while((read= br.readLine())!=null)
                {
                    sb.append(read);

                }
                br.close();
                Log.v("DAMA-POST", sb.toString());

                //try {Thread.sleep(5000); } catch (InterruptedException e) {}

                conn.disconnect();

            }catch (Exception ex) {
                Log.e("DAMA", ex.toString());
                Log.e("DAMA", ex.getLocalizedMessage());
                return "Error";
            }
            return "Done";
        }
    }

}
