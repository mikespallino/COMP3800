package mam.dama.activity;

import android.app.FragmentManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import mam.dama.Fragment.PlaylistFragment;
import mam.dama.R;

public class HostHubActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private boolean shuffleEnabled = false;
    private int currentSong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_hub);

        TextView eventName = (TextView) findViewById(R.id.event_name);
        final TextView currentlyPlaying = (TextView) findViewById(R.id.currently_plaing_meta);

        Bundle prevBundle = getIntent().getExtras();

        String nameText = prevBundle.getString("event_name");
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

        final ArrayList<String> songs = prevBundle.getStringArrayList("playlist_songs");
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
}
