package no.ahoi.spotify.spotifystreamer;

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
public class PlayTrackFragment extends DialogFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final String LOG_TAG = PlayTrackFragment.class.getSimpleName();
    TopTracksData mTopTrack;
    SpotifyStreamerActivity mActivity;
    ImageButton mPreviousTrack;
    ImageButton mPlayTrackToggle;
    ImageButton mNextTrack;

    public PlayTrackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Open dialog in full screen
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_AppCompat);

        // Call to SpotifyStreamerActivity should not be done if fragment is to be
        // called from multiple activities. Since we only have one activity, this is OK.
        // http://stackoverflow.com/questions/12659747/call-an-activity-method-from-a-fragment#answer-12683615
        mActivity = (SpotifyStreamerActivity) getActivity();
        Log.v("mactivity...", "WORKS");
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
                mPreviousTrack = (ImageButton) rootView.findViewById(R.id.dialogBtnPlayPrevious);
                mPlayTrackToggle = (ImageButton) rootView.findViewById(R.id.dialogBtnPlayToggle);
                mNextTrack = (ImageButton) rootView.findViewById(R.id.dialogBtnPlayNext);
                SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.dialogSeekBar);

                // Set click listeners
                mPreviousTrack.setOnClickListener(this);
                mPlayTrackToggle.setOnClickListener(this);
                mNextTrack.setOnClickListener(this);
                seekBar.setOnSeekBarChangeListener(this);

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

    @Override
    public void onClick(View v) {
        Log.v(LOG_TAG, v.getId() + "");
        switch(v.getId()) {
            case R.id.dialogBtnPlayToggle:
                Log.v("onClick: ", "start / pause music");
                Boolean isPlaying = mActivity.trackController("isPlaying");
                if (isPlaying) {
                    Boolean trackPaused = mActivity.trackController("pause");
                    if (trackPaused) {
                        Log.v(LOG_TAG, "paused successfully");
                        mPlayTrackToggle.setImageResource(android.R.drawable.ic_media_play);
                    } else {
                        // TODO Display toast
                    }
                } else {
                    Boolean trackStarted = mActivity.trackController("start");
                    if (trackStarted) {
                        Log.v(LOG_TAG, "started successfully");
                        mPlayTrackToggle.setImageResource(android.R.drawable.ic_media_pause);
                    } else {
                        // TODO Display toast
                    }
                }
                break;
            case R.id.dialogBtnPlayPrevious:
                Log.v("onClick: ", "play previous track");
                break;
            case R.id.dialogBtnPlayNext:
                Log.v("onClick: ", "play next track");
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // Notification that the progress level has changed.
        // TODO: set text in time durition
        // TODO: play song from selected progress
        Log.v("onProgressChanged()", "progress selected: " + progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Notification that the user has started a touch gesture.
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Notification that the user has finished a touching gesture.
    }

}
