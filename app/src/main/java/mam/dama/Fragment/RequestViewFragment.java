package mam.dama.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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


public class RequestViewFragment extends Fragment {

    private View rootView;
    private ArrayList<String> song_requests = new ArrayList<>();
    private ArrayAdapter adapter;
    private ListView requestsView;

    public RequestViewFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_request_view, container, false);

        Bundle prevBundle = getArguments();

        GetRequestsTask grt = new GetRequestsTask(prevBundle.getString("event_uuid"));
        grt.execute();

        requestsView = (ListView) rootView.findViewById(R.id.song_requests);
        adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, song_requests);

        requestsView.setAdapter(adapter);
        return rootView;
    }

    class GetRequestsTask extends AsyncTask<String, Void, String> {

        private String eventUUID;

        public GetRequestsTask(String eventUUID){
            super();
            this.eventUUID = eventUUID;
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject requestData = new JSONObject();
            try {
                requestData.put("event_uuid", eventUUID);
                requestData.put("key", "DAMA");
            } catch (org.json.JSONException e) {
                Log.v("DAMA", "Couldn't format JSON.");
            }

            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.241.149.243:8080/get_req");
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
                JSONArray requests = data.getJSONArray("songs");
                song_requests.clear();
                for(int i = 0; i < requests.length(); i++) {
                    song_requests.add(requests.getString(i));
                }


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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                    Log.v("DAMA",  song_requests.toString());
                }
            });
        }

    }

}
