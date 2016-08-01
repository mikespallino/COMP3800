package mam.dama.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import mam.dama.R;

public class JoinEventPickerActivity extends AppCompatActivity {

    private String eventNameText = "";
    private String eventNamePassword = "";
    private ArrayAdapter<String> adapter;
    private ListView eventView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event_picker);

        final ArrayList<String> eventItems = new ArrayList<>();
        final ArrayList<String> eventItemsCopy = new ArrayList<>(eventItems);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                eventItems);

        // TODO:Boston should be replaced later.
        DiscoverEventsTask events = new DiscoverEventsTask("Boston", eventItems);
        events.execute();

        eventView = (ListView) findViewById(R.id.eventListView);
        eventView.setAdapter(adapter);

        SearchView eventSearchBar = (SearchView) findViewById(R.id.eventSearchView);
        eventSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Fake it till we make it!
                // Here we are *attempting* to to rudimentary searches through the playlist for text
                for (int i = 0; i < eventItems.size(); i++) {
                    if (!eventItems.get(i).toLowerCase().contains(query.toLowerCase())) {
                        eventItems.remove(i);
                    }
                }
                adapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // If the search text has changed, we need to change our data back to the original list
                for (int i = 0; i < eventItemsCopy.size(); i++) {
                    if (!eventItems.contains(eventItemsCopy.get(i))) {
                        eventItems.add(eventItemsCopy.get(i));
                    }
                }
                adapter.notifyDataSetChanged();
                return false;
            }

        });

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
                        Log.v("DAMA [EVENT NAME]", eventNameText);
                        Log.v("DAMA [EVENT PASSWORD]", eventNamePassword);

                        JoinEventTask joinEvent = new JoinEventTask(eventNameText, eventNamePassword);
                        joinEvent.execute();
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

        ImageButton refresh = (ImageButton) findViewById(R.id.refresh_button);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiscoverEventsTask events = new DiscoverEventsTask("Boston", eventItems);
                events.execute();
            }
        });
    }

    class DiscoverEventsTask extends AsyncTask<String, Void, String> {

        private String location;
        private JSONObject event_data;
        private ArrayList<String> eventItems;

        public DiscoverEventsTask(String location, ArrayList<String> eventItems){
            super();
            this.location = location;
            this.eventItems = eventItems;
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject discoverData = new JSONObject();
            try {
                discoverData.put("location", location);
                discoverData.put("key", "DAMA");
            } catch (org.json.JSONException e) {
                Log.v("DAMA", "Couldn't format JSON.");
            }

            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.241.149.243:8080/discover");
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
                streamWriter.write(discoverData.toString());
                streamWriter.flush();
                streamWriter.close();

                Log.v("DAMA", discoverData.toString());

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

                event_data = new JSONObject(sb.toString());
                eventItems.clear();
                JSONArray event_list = event_data.getJSONArray("event_list");
                for(int i = 0; i < event_list.length(); i++) {
                    eventItems.add(event_list.getString(i));
                }

                //try {Thread.sleep(5000); } catch (InterruptedException e) {}

                conn.disconnect();

            }catch (Exception ex) {
                Log.e("DAMA", ex.toString());
                Log.e("DAMA", ex.getLocalizedMessage());
                return "Error";
            }
            return "Done";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }

        public JSONObject getEvents() {
            return event_data;
        }
    }

    class JoinEventTask extends AsyncTask<String, Void, String> {

        private String eventNameText, eventNamePassword, playlistName, event_uuid;
        private ArrayList<String> playlistSongs, allSongs;

        public JoinEventTask(String eventName, String eventPassword){
            super();
            this.eventNameText = eventName;
            this.eventNamePassword = eventPassword;
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
            } catch (org.json.JSONException e) {
                Log.v("DAMA", "Couldn't format JSON.");
            }

            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.241.149.243:8080/join");
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

                JSONObject event_data = new JSONObject(sb.toString());
                JSONArray playlist_songs = event_data.getJSONArray("playlist_songs");
                playlistSongs = new ArrayList<>();
                for(int i = 0; i < playlist_songs.length(); i++) {
                    playlistSongs.add(playlist_songs.getString(i));
                }
                JSONArray all_songs = event_data.getJSONArray("all_songs");
                allSongs = new ArrayList<>();
                for(int i = 0; i < all_songs.length(); i++) {
                    allSongs.add(all_songs.getString(i));
                }
                playlistName = event_data.getString("playlist_name");
                event_uuid = event_data.getString("event_uuid");

                conn.disconnect();

            }catch (Exception ex) {
                Log.e("DAMA", ex.toString());
                Log.e("DAMA", ex.getLocalizedMessage());
                return "Error";
            }
            return "Done";
        }

        @Override
        protected void onPostExecute(String s) {
            if(playlistSongs == null && allSongs == null) {
                Log.e("DAMA", "Did not join an event!");
            } else {
                Intent hubIntent = new Intent(JoinEventPickerActivity.this, HubActivity.class);
                Bundle hubBundle = new Bundle();
                hubBundle.putString("event_name", eventNameText);
                //TODO: Fill this in with the songs from the event.
                hubBundle.putStringArrayList("playlist_songs", playlistSongs);
                hubBundle.putString("event_uuid", event_uuid);
                hubBundle.putStringArrayList("all_songs", allSongs);
                hubIntent.putExtras(hubBundle);
                startActivity(hubIntent);
                finish();
            }
        }
    }
}
