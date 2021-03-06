package mam.dama.Fragment;

import android.app.FragmentManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import mam.dama.R;

public class PlaylistFragment extends Fragment {

    private Button addRequest;
    private Button viewRequest;
    private View rootView;

    public PlaylistFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_playlist, container, false);
        addRequest = (Button)(rootView.findViewById(R.id.button_addRequest));
        viewRequest = (Button)(rootView.findViewById(R.id.button_viewRequest));


        final Bundle prevBundle = getArguments();

        ArrayList<String> playlistSongs = prevBundle.getStringArrayList("playlist_songs");
        ListView playlistView = (ListView) rootView.findViewById(R.id.listView_currentPlaylist);
        final ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, playlistSongs);

        playlistView.setAdapter(adapter);
        addRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                Fragment fragment = new RequestAddFragment();
                fragment.setArguments(prevBundle);
                fm.beginTransaction().replace(R.id.content_hub,fragment).addToBackStack(null).commit();
            }
        });

        viewRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                Fragment fragment = new RequestViewFragment();
                fragment.setArguments(prevBundle);
                fm.beginTransaction().replace(R.id.content_hub,fragment).addToBackStack(null).commit();
            }
        });

        return rootView;
    }
}
