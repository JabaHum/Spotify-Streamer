package no.ahoi.spotify.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A dialog fragment with posibility to play and stream track from Spotify's API Wrapper
 */
public class PlayTrackFragment extends DialogFragment {
    private static final String LOG_TAG = PlayTrackFragment.class.getSimpleName();

    public PlayTrackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_AppCompat);
    }

    public static PlayTrackFragment newInstance() {

        return new PlayTrackFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_play_track, container, false);

        Log.v(LOG_TAG, "PlayTrackFragment->onCreateView");

        return rootView;
    }
}
