package mam.dama.Fragment;

import android.app.FragmentManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

        addRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle request_add = new Bundle();
                FragmentManager fm = getFragmentManager();
                Fragment fragment = new RequestAddFragment();
                fragment.setArguments(request_add);
                fm.beginTransaction().replace(R.id.content_hub,fragment).addToBackStack(null).commit();
            }
        });

        viewRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle request_view = new Bundle();
                FragmentManager fm = getFragmentManager();
                Fragment fragment = new RequestViewFragment();
                fragment.setArguments(request_view);
                fm.beginTransaction().replace(R.id.content_hub,fragment).addToBackStack(null).commit();
            }
        });

        return rootView;
    }
}
