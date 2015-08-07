package no.ahoi.spotify.spotifystreamer;

import android.os.Bundle;
import android.os.Handler;
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
 * A dialog fragment with possibility to play and stream track from Spotify's API Wrapper
 */
public class PlayTrackFragment extends DialogFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final String LOG_TAG = PlayTrackFragment.class.getSimpleName();
    ArtistData mArtistData;
    TopTracksData mTopTrack;
    SpotifyStreamerActivity mActivity;
    TextView mPlayTimeElapsed;
    TextView mPlayTimeLeft;
    ImageButton mPreviousTrack;
    ImageButton mPlayTrackToggle;
    ImageButton mNextTrack;
    SeekBar mSeekBar;
    Handler mHandler;
    Runnable mTimeChecker;

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
    }

    public static PlayTrackFragment newInstance(TopTracksData topTrack, ArtistData artistData) {
        Log.e(LOG_TAG, artistData.imageUrl);
        PlayTrackFragment playTrackFragment = new PlayTrackFragment();
        // Set top track data before returning
        Bundle args = new Bundle();
        args.putParcelable("topTrack", topTrack);
        args.putParcelable("artistData", artistData);
        playTrackFragment.setArguments(args);
        return playTrackFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_play_track, container, false);

        // Fetch data from savedInstanceState, else fetch from arguments.
        if (savedInstanceState != null && savedInstanceState.containsKey("topTrack") && savedInstanceState.containsKey("artistData")) {
            mTopTrack = savedInstanceState.getParcelable("topTrack");
            mArtistData = savedInstanceState.getParcelable("artistData");
        } else if (this.getArguments() != null) {
            Bundle bundle = this.getArguments();
            mTopTrack = bundle.getParcelable("topTrack");
            mArtistData = bundle.getParcelable("artistData");
        } else {
            Log.e(LOG_TAG, " Could not fetch data.");
        }

        if (mTopTrack != null && mArtistData != null) {
            // Find views
            TextView artistTitle = (TextView) rootView.findViewById(R.id.dialogArtistTitle);
            TextView albumTitle = (TextView) rootView.findViewById(R.id.dialogAlbumTitle);
            TextView trackTitle = (TextView) rootView.findViewById(R.id.dialogTrackTitle);
            mPlayTimeElapsed = (TextView) rootView.findViewById(R.id.dialogPlayTimeElapsed);
            mPlayTimeLeft = (TextView) rootView.findViewById(R.id.dialogPlayTimeLeft);
            ImageView albumCover = (ImageView) rootView.findViewById(R.id.dialogAlbumCover);
            mPreviousTrack = (ImageButton) rootView.findViewById(R.id.dialogBtnPlayPrevious);
            mPlayTrackToggle = (ImageButton) rootView.findViewById(R.id.dialogBtnPlayToggle);
            mNextTrack = (ImageButton) rootView.findViewById(R.id.dialogBtnPlayNext);
            mSeekBar = (SeekBar) rootView.findViewById(R.id.dialogSeekBar);

            // Set click listeners
            mPreviousTrack.setOnClickListener(this);
            mPlayTrackToggle.setOnClickListener(this);
            mNextTrack.setOnClickListener(this);
            mSeekBar.setOnSeekBarChangeListener(this);

            // Set data to each view
            artistTitle.setText(mArtistData.name);
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

            if (mHandler == null) {
                mHandler = new Handler();
            }

            // Update SeekBar times
            // TODO: Pause Runnable when music is paused
            mTimeChecker = new Runnable() {
                @Override
                public void run() {
                    int interval = 200;
                    Integer[] times = mActivity.updateTimes();
                    if (times != null) {
                        // !! will be set every loop when track duration is 100 sek
                        if (mSeekBar.getMax() == 100) {
                            mSeekBar.setMax(times[0] / 1000);
                        }
                        mSeekBar.setProgress(times[1] / 1000);
                    }
                    mHandler.postDelayed(mTimeChecker, interval);
                }
            };
            mTimeChecker.run();

        } else {
            Log.e(LOG_TAG, "Could not fetch necessary data");
        }
        return rootView;
    }

    @Override
     public void onSaveInstanceState(Bundle outState) {
        if (mTopTrack != null && mArtistData != null) {
            outState.putParcelable("topTrack", mTopTrack);
            outState.putParcelable("artistData", mArtistData);
        }
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
                        mPlayTrackToggle.setImageResource(android.R.drawable.ic_media_play);
                    }
                } else {
                    Boolean trackStarted = mActivity.trackController("start");
                    if (trackStarted) {
                        mPlayTrackToggle.setImageResource(android.R.drawable.ic_media_pause);
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
        // TODO: set text in time duration
        // TODO: play song from selected progress
        // http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay/6242292#6242292
        Log.v("onProgressChanged()", "progress selected: " + progress);
        mPlayTimeElapsed.setText(Integer.toString(progress));
        setTimeLeft(progress);

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Notification that the user has started a touch gesture.
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Notification that the user has finished a touching gesture.
    }

    private void setTimeLeft(int progress) {
        mPlayTimeLeft.setText(Integer.toString((progress - mSeekBar.getMax())));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.removeCallbacks(mTimeChecker);
        }
    }
}
