package no.ahoi.spotify.spotifystreamer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


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

        // Open dialog in full screen
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_AppCompat);
    }

    public static PlayTrackFragment newInstance(TopTracksData topTrack) {
        PlayTrackFragment playTrackFragment = new PlayTrackFragment();
        // Set top track data before returning
        Bundle args = new Bundle();
        args.putParcelable("topTrack", topTrack);
        playTrackFragment.setArguments(args);
        return playTrackFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_play_track, container, false);

        if (this.getArguments() != null) {
            Bundle bundle = this.getArguments();
            TopTracksData topTrack = bundle.getParcelable("topTrack");
            // find views
            TextView artistTitle = (TextView) rootView.findViewById(R.id.dialogArtistTitle);
            TextView albumTitle = (TextView) rootView.findViewById(R.id.dialogAlbumTitle);
            TextView trackTitle = (TextView) rootView.findViewById(R.id.dialogTrackTitle);
            ImageView albumCover = (ImageView) rootView.findViewById(R.id.dialogAlbumCover);
            // Set data to each view
            artistTitle.setText("artist name");
            albumTitle.setText(topTrack.albumTitle);
            trackTitle.setText(topTrack.trackTitle);
            // Load album cover
            if (topTrack.albumImageUrlLarge != null) {
                Picasso.with(getActivity()).load(topTrack.albumImageUrlLarge).placeholder(R.mipmap.no_image).into(albumCover);
            } else if (topTrack.albumImageUrlSmall != null) {
                Picasso.with(getActivity()).load(topTrack.albumImageUrlSmall).placeholder(R.mipmap.no_image).into(albumCover);
            } else {
                Picasso.with(getActivity()).load(R.mipmap.no_image).into(albumCover);
            }

        } else {
            Log.v(LOG_TAG, " Could not fetch arguments (spotify ID) from activity");
        }


        Log.v(LOG_TAG, "PlayTrackFragment->onCreateView");

        return rootView;
    }
}
