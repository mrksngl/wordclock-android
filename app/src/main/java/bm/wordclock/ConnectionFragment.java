package bm.wordclock;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import bm.wordclock.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionFragment extends Fragment {

    private TextView mStatusText;

    public ConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connection, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mStatusText = (TextView) view.findViewById(R.id.connectProgressText);
    }

    public void setText(@NonNull CharSequence text) {
        mStatusText.setText(text);
        mStatusText.setVisibility(View.VISIBLE);
    }

    public void hideText() {
        mStatusText.setVisibility(View.INVISIBLE);
    }

}
