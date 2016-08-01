package mam.dama.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import mam.dama.R;

public class RequestAddFragment extends Fragment {

    private View rootView;

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

        Bundle prevBundle = getArguments();
        ArrayList<String> allSongs = prevBundle.getStringArrayList("all_songs");

        ListView addSongs = (ListView) rootView.findViewById(R.id.add_songs);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, allSongs);

        addSongs.setAdapter(adapter);

        return rootView;
    }

}
