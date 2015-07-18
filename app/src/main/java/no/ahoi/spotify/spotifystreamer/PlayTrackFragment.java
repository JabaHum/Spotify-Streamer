package no.ahoi.spotify.spotifystreamer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A dialog fragment with posibility to play and stream track from Spotify's API Wrapper
 */
public class PlayTrackFragment extends DialogFragment {
    private static final String LOG_TAG = PlayTrackFragment.class.getSimpleName();
    MediaPlayer mMediaPlayer;
    TopTracksData mTopTrack;

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
            mTopTrack = bundle.getParcelable("topTrack");
            if (mTopTrack != null) {
                // Find views
                TextView artistTitle = (TextView) rootView.findViewById(R.id.dialogArtistTitle);
                TextView albumTitle = (TextView) rootView.findViewById(R.id.dialogAlbumTitle);
                TextView trackTitle = (TextView) rootView.findViewById(R.id.dialogTrackTitle);
                TextView playTimeElapsed = (TextView) rootView.findViewById(R.id.dialogPlayTimeElapsed);
                TextView playTimeLeft = (TextView) rootView.findViewById(R.id.dialogPlayTimeLeft);
                ImageView albumCover = (ImageView) rootView.findViewById(R.id.dialogAlbumCover);
                ImageButton previousTrack = (ImageButton) rootView.findViewById(R.id.dialogBtnPlayPrevious);
                final ImageButton playTrackToggle = (ImageButton) rootView.findViewById(R.id.dialogBtnPlayToggle);
                ImageButton nextTrack = (ImageButton) rootView.findViewById(R.id.dialogBtnPlayNext);
                SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.dialogSeekBar);

                // Set data to each view
                artistTitle.setText("artist name"); // TODO get artist data
                albumTitle.setText(mTopTrack.albumTitle);
                trackTitle.setText(mTopTrack.trackTitle);
                // Load album cover
                if (mTopTrack.albumImageUrlLarge != null) {
                    Picasso.with(getActivity()).load(mTopTrack.albumImageUrlLarge).placeholder(R.mipmap.no_image).into(albumCover);
                } else if (mTopTrack.albumImageUrlSmall != null) {
                    Picasso.with(getActivity()).load(mTopTrack.albumImageUrlSmall).placeholder(R.mipmap.no_image).into(albumCover);
                } else {
                    Picasso.with(getActivity()).load(R.mipmap.no_image).into(albumCover);
                }

                playTrackToggle.setImageResource(android.R.drawable.ic_media_pause);

                playTrackToggle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*playTrackToggle.setImageResource(android.R.drawable.ic_media_pause);
                        mMediaPlayer.start();*/

                        Boolean trackPaused = ((SpotifyStreamerActivity) getActivity()).pauseTrack();
                        if (trackPaused) {
                            Log.v(LOG_TAG, "pausing");
                            playTrackToggle.setImageResource(android.R.drawable.ic_media_play);
                        } else {
                            Log.v(LOG_TAG, "cannot pause");
                            // TODO implement method to play track in activity
                        }
                    }
                });

            }

        } else {
            Log.v(LOG_TAG, " Could not fetch arguments.");
        }

        return rootView;
    }

    @Override
     public void onSaveInstanceState(Bundle outState) {
        // TODO Save data for later
        super.onSaveInstanceState(outState);
    }
}