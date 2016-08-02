package mam.dama.Fragment;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import mam.dama.activity.HostHubActivity;
import mam.dama.activity.HubActivity;

public class RequestAddFragment extends Fragment {

    private View rootView;
    private Bundle prevBundle;

    public RequestAddFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_request_add, container, false);

        final Bundle prevBundle = getArguments();
        ArrayList<String> allSongs = prevBundle.getStringArrayList("all_songs");

        final ListView addSongs = (ListView) rootView.findViewById(R.id.add_songs);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, allSongs);

        addSongs.setAdapter(adapter);

        addSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String songTitle = (String) addSongs.getItemAtPosition(position);
                // The following sets up the Alert Dialog to create confirm the request.
                AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                builder.setTitle("Are you sure?");

                LinearLayout dialogLayout = new LinearLayout(rootView.getContext());
                dialogLayout.setOrientation(LinearLayout.VERTICAL);

                final TextView song = new TextView(rootView.getContext());
                song.setHint("Requesting song: " + songTitle);
                song.setGravity(Gravity.CENTER);
                song.setPadding(0,50,0,0);

                dialogLayout.addView(song);
                builder.setView(dialogLayout);

                // The following is to define what happens when the user clicks either button on the Alert.
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MakeRequestTask mrt = new MakeRequestTask(prevBundle.getString("event_uuid"), songTitle);
                        mrt.execute();
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

        return rootView;
    }

    class MakeRequestTask extends AsyncTask<String, Void, String> {

        private String eventUUID, songTitle;

        public MakeRequestTask(String eventUUID, String songTitle){
            super();
            this.eventUUID = eventUUID;
            this.songTitle = songTitle;
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject requestData = new JSONObject();
            try {
                requestData.put("event_uuid", eventUUID);
                requestData.put("song_title", songTitle);
                requestData.put("key", "DAMA");
            } catch (org.json.JSONException e) {
                Log.v("DAMA", "Couldn't format JSON.");
            }

            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.241.149.243:8080/make_req");
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
                streamWriter.write(requestData.toString());
                streamWriter.flush();
                streamWriter.close();

                Log.v("DAMA", requestData.toString());

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

                JSONObject data = new JSONObject(sb.toString());

                conn.disconnect();

            }catch (Exception ex) {
                Log.e("DAMA", ex.toString());
                Log.e("DAMA", ex.getLocalizedMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Failed to make request.", Toast.LENGTH_SHORT).show();
                    }
                });
                return "Error";
            }
            return "Done";
        }

        @Override
        protected void onPostExecute(String s) {
            getActivity().onBackPressed();
        }

    }

}
