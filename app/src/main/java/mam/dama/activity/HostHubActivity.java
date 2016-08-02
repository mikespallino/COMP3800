package mam.dama.activity;

import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
    private ArrayList<Uri> songUris;
    private ArrayList<Integer> curPlayStack;
    private String event_uuid;
    private ImageButton playButton, pauseButton;
    private TextView currentlyPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_hub);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);

        TextView eventName = (TextView) findViewById(R.id.event_name);
        currentlyPlaying = (TextView) findViewById(R.id.currently_plaing_meta);

        Bundle prevBundle = getIntent().getExtras();
        //INFO: To tell the difference between host/join for the fragments
        prevBundle.putString("FROM", "HOST");

        String nameText = prevBundle.getString("event_name");
        event_uuid = prevBundle.getString("event_uuid");
        if (nameText != null) {
            nameText = "Event: " + nameText;
        } else {
            nameText = "Event: UNKOWN";
        }
        eventName.setText(nameText);

        playButton = (ImageButton) findViewById(R.id.play_button);
        pauseButton = (ImageButton) findViewById(R.id.pause_button);
        final ImageButton shuffleButton = (ImageButton) findViewById(R.id.shuffle_button);
        ImageButton skipButton = (ImageButton) findViewById(R.id.skip_button);

        Uri musicPath = MediaStore.Audio.Playlists.Members.getContentUri("external", prevBundle.getLong("playlist_id"));
        ContentResolver resolver = this.getContentResolver();
        Cursor musicCursor = resolver.query(musicPath, new String[] {"*"}, null, null, null);
        songUris = new ArrayList<>();
        curPlayStack = new ArrayList<>();

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

        if (songs.size() != 0) {
            try {
                mediaPlayer.setDataSource(getApplicationContext(), songUris.get(currentSong));
                mediaPlayer.prepare();
            } catch (IOException e) {
                Log.e("MUSIC", "Could not play music");
            }
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(songUris.size() > 1 && songs.size() > 1) {
                    songUris.remove(currentSong);
                    songs.remove(currentSong);
                    if (shuffleEnabled) {
                        currentSong = (int) (Math.random() * songUris.size());
                    }

                    Log.v("SONG LIST:", songs.toString());
                    Log.v("SONG:", songs.get(currentSong));
                    Log.v("SONG ID, LIST SIZE", "" + currentSong + ", " + songUris.size());

                    try {
                        mediaPlayer.setDataSource(getApplicationContext(), songUris.get(currentSong));
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        Log.e("MUSIC", "Could not play music");
                        Toast.makeText(HostHubActivity.this, "Failed to play song.", Toast.LENGTH_SHORT).show();
                    }
                    SetCurrentlyPlaying setCurPlayTask = new SetCurrentlyPlaying();
                    setCurPlayTask.execute();

                    currentlyPlaying.setText(songs.get(currentSong));
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.start();
                currentlyPlaying.setText(songs.get(currentSong));
                playButton.setColorFilter(Color.GREEN);
                pauseButton.setColorFilter(Color.BLACK);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.pause();
                pauseButton.setColorFilter(Color.GREEN);
                playButton.setColorFilter(Color.BLACK);
            }
        });

        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffleEnabled = !shuffleEnabled;
                if(shuffleEnabled) {
                    shuffleButton.setColorFilter(Color.GREEN);
                } else {
                    shuffleButton.setColorFilter(Color.BLACK);
                }
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mediaPlayer.stop();
                mediaPlayer.reset();
                if(songUris.size() > 1 && songs.size() > 1) {
                    songUris.remove(currentSong);
                    songs.remove(currentSong);
                    if (curPlayStack.size() > 0) {
                        currentSong = curPlayStack.get(curPlayStack.size()-1);
                    } else if (shuffleEnabled) {
                        currentSong = (int) (Math.random() * songUris.size());
                    }

                    Log.v("SONG LIST:", songs.toString());
                    Log.v("URI LIST:", songUris.toString());
                    Log.v("SONG:", songs.get(currentSong));
                    Log.v("SONG ID, LIST SIZE", "" + currentSong + ", " + songUris.size());

                    try {
                        mediaPlayer.setDataSource(getApplicationContext(), songUris.get(currentSong));
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        Log.e("MUSIC", "Could not play music");
                        Toast.makeText(HostHubActivity.this, "Failed to play song.", Toast.LENGTH_SHORT).show();
                    }

                    SetCurrentlyPlaying setCurPlayTask = new SetCurrentlyPlaying();
                    setCurPlayTask.execute();
                    currentlyPlaying.setText(songs.get(currentSong));
                }
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
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            HostHubActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
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

            }catch (Exception ex) {
                Log.e("DAMA", ex.toString());
                Log.e("DAMA", ex.getLocalizedMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(HostHubActivity.this, "Failed to set the currently playing song.", Toast.LENGTH_SHORT).show();
                    }
                });
                return "Error";
            }
            return "Done";
        }
    }

    public void playSong(String song) {
        Uri musicPath = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = this.getContentResolver();
        Cursor musicCursor = resolver.query(musicPath, new String[] {"*"}, MediaStore.Audio.Media.TITLE + "= ?", new String[] {song}, null);

        if (musicCursor != null) {
            musicCursor.moveToFirst();
            String file = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            Uri songURI = Uri.fromFile(new File(file));

            mediaPlayer.reset();

            if(songUris.size() > 1 && songs.size() > 1) {
                songUris.remove(currentSong);
                songs.remove(currentSong);
                curPlayStack.add(currentSong);
                songs.add(song);
                songUris.add(songURI);
                currentSong = songs.size() - 1;
            }

            try {
                mediaPlayer.setDataSource(this, songURI);
                mediaPlayer.prepare();
            } catch (IOException e) {
                Log.e("DAMA", e.getMessage());
                Toast.makeText(this, "Failed to play request.", Toast.LENGTH_SHORT);
            }
            mediaPlayer.start();

            SetCurrentlyPlaying setCurPlayTask = new SetCurrentlyPlaying();
            setCurPlayTask.execute();
            currentlyPlaying.setText(song);
            playButton.setColorFilter(Color.GREEN);
            pauseButton.setColorFilter(Color.BLACK);
        } else {
            Toast.makeText(this, "Failed to play request.", Toast.LENGTH_SHORT);
        }
    }

}
