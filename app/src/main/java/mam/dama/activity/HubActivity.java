package mam.dama.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import mam.dama.Fragment.PlaylistFragment;
import mam.dama.R;

public class HubActivity extends AppCompatActivity {

    private TextView curPlayingMeta;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private String eventUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);

        TextView eventName = (TextView) findViewById(R.id.event_name);
        curPlayingMeta = (TextView) findViewById(R.id.currently_plaing_meta);

        Bundle prevBundle = getIntent().getExtras();
        eventUuid = prevBundle.getString("event_uuid");

        Log.v("DAMA-HUB", prevBundle.toString());

        String nameText = prevBundle.getString("event_name");
        if (nameText != null) {
            nameText = "Event: " + nameText;
        } else {
            nameText = "Event: UNKOWN";
        }
        eventName.setText(nameText);

        GetCurrentlyPlaying gcp = new GetCurrentlyPlaying(eventUuid);
        gcp.execute();

        scheduler.schedule(new GCPTask(), 3, TimeUnit.MINUTES);

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

    class GetCurrentlyPlaying extends AsyncTask<String, Void, String> {

        private String event_uuid, cur_play;

        public GetCurrentlyPlaying(String event_uuid){
            super();
            this.event_uuid = event_uuid;
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject hostData = new JSONObject();
            try {
                hostData.put("event_uuid", event_uuid);
                hostData.put("key", "DAMA");
            } catch (org.json.JSONException e) {
                Log.v("DAMA", "Couldn't format JSON.");
            }

            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.241.149.243:8080/gcp");
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
                cur_play = event_data.getString("cur_play");
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    curPlayingMeta.setText(cur_play);
                    curPlayingMeta.invalidate();
                }
            });
        }

    }

    private class GCPTask implements Runnable {

        @Override
        public void run() {
            new GetCurrentlyPlaying(eventUuid).execute();
        }
    }
}
