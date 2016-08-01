package mam.dama.activity;

import android.app.FragmentManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import mam.dama.Fragment.PlaylistFragment;
import mam.dama.R;

public class HostHubActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private boolean shuffleEnabled = false;
    private int currentSong = 0;
    private ArrayList<String> songs;
    private String event_uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_hub);

        TextView eventName = (TextView) findViewById(R.id.event_name);
        final TextView currentlyPlaying = (TextView) findViewById(R.id.currently_plaing_meta);

        Bundle prevBundle = getIntent().getExtras();

        String nameText = prevBundle.getString("event_name");
        event_uuid = prevBundle.getString("event_uuid");
        final SetCurrentlyPlaying setCurPlayTask = new SetCurrentlyPlaying();
        if (nameText != null) {
            nameText = "Event: " + nameText;
        } else {
            nameText = "Event: UNKOWN";
        }
        eventName.setText(nameText);

        ImageButton playButton = (ImageButton) findViewById(R.id.play_button);
        ImageButton pauseButton = (ImageButton) findViewById(R.id.pause_button);
        final ImageButton shuffleButton = (ImageButton) findViewById(R.id.shuffle_button);
        ImageButton skipButton = (ImageButton) findViewById(R.id.skip_button);

        Uri musicPath = MediaStore.Audio.Playlists.Members.getContentUri("external", prevBundle.getLong("playlist_id"));
        ContentResolver resolver = this.getContentResolver();
        Cursor musicCursor = resolver.query(musicPath, new String[] {"*"}, null, null, null);
        final ArrayList<Uri> songUris = new ArrayList<>();

        songs = prevBundle.getStringArrayList("playlist_songs");
        Log.v("SONGS", songs.toString());
        for (int i = 0; i < musicCursor.getCount(); i++) {
            musicCursor.moveToPosition(i);
            String trackTitle = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            Log.v("TRACKS", trackTitle);
            if (songs.contains(trackTitle)) {
                String file = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                songUris.add(Uri.fromFile(new File(file)));
                Log.v("CURSOR", songUris.get(i).toString());
            }
        }

        musicCursor.close();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), songUris.get(currentSong));
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("MUSIC", "Could not play music");
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(shuffleEnabled) {
                    songUris.remove(currentSong);
                    currentSong = (int) (Math.random() * songUris.size());
                } else {
                    currentSong++;
                }

                Log.v("SONG ID, LIST SIZE", "" + currentSong + ", " + songUris.size());

                try {
                    mediaPlayer.setDataSource(getApplicationContext(), songUris.get(currentSong));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    Log.e("MUSIC", "Could not play music");
                }

                setCurPlayTask.execute();

                currentlyPlaying.setText(songs.get(currentSong));
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.start();
                currentlyPlaying.setText(songs.get(currentSong));
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.pause();
            }
        });

        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffleEnabled = !shuffleEnabled;
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                if(shuffleEnabled) {
                    songUris.remove(currentSong);
                    currentSong = (int) (Math.random() * songUris.size());
                } else {
                    currentSong++;
                }

                Log.v("SONG ID, LIST SIZE", "" + currentSong + ", " + songUris.size());

                try {
                    mediaPlayer.setDataSource(getApplicationContext(), songUris.get(currentSong));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    Log.e("MUSIC", "Could not play music");
                }

                setCurPlayTask.execute();
                currentlyPlaying.setText(songs.get(currentSong));
            }
        });

        if(savedInstanceState==null){
            FragmentManager fm = getFragmentManager();
            android.app.Fragment fragment = new PlaylistFragment();
            fragment.setArguments(prevBundle);
            fm.beginTransaction().replace(R.id.content_hub, fragment).commit();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        switch(id){
            case R.id.action_settings:
                return true;
            case R.id.action_about:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }

    class SetCurrentlyPlaying extends AsyncTask<String, Void, String> {

        public SetCurrentlyPlaying(){
            super();
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject hostData = new JSONObject();
            try {
                Log.v("DAMA-SCP", event_uuid);
                hostData.put("event_uuid", event_uuid);
                hostData.put("cur_play", songs.get(currentSong));
                hostData.put("key", "DAMA");
            } catch (org.json.JSONException e) {
                Log.v("DAMA", "Couldn't format JSON.");
            }

            Log.v("DAMA-SCP", hostData.toString());

            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.241.149.243:8080/scp");
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
                conn.disconnect();

                onPostExecute(1L);

            }catch (Exception ex) {
                Log.e("DAMA", ex.toString());
                Log.e("DAMA", ex.getLocalizedMessage());
                return "Error";
            }
            return "Done";
        }

        protected void onPostExecute(Long result) {
        }
    }
}
