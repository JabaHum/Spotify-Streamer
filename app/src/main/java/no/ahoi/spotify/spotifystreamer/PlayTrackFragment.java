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

import java.util.ArrayList;

/**
 * A dialog fragment with possibility to play and stream track from Spotify's API Wrapper
 */
public class PlayTrackFragment extends DialogFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final String LOG_TAG = PlayTrackFragment.class.getSimpleName();
    private SpotifyStreamerActivity mActivity;
    private View mRootView;
    private ArtistData mArtistData;
    private ArrayList<TopTracksData> mTopTracksData;
    private Integer mTrackPosition;
    private TextView mPlayTimeElapsed;
    private TextView mPlayTimeLeft;
    private ImageButton mPlayTrackToggle;
    private SeekBar mSeekBar;
    private Handler mHandler;
    private Runnable mTimeChecker;
    private Boolean mTwoPane;

    public PlayTrackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey("twoPane")) {
            mTwoPane = savedInstanceState.getBoolean("twoPane");
        } else if (this.getArguments() != null) {
            mTwoPane = this.getArguments().getBoolean("twoPane");
        }
        if (!mTwoPane) {
            // Open dialog in full screen
            setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_AppCompat);
        }
        // Call to SpotifyStreamerActivity should not be done if fragment is to be
        // called from multiple activities. Since we only have one activity, this is OK.
        // http://stackoverflow.com/questions/12659747/call-an-activity-method-from-a-fragment#answer-12683615
        mActivity = (SpotifyStreamerActivity) getActivity();
    }

    public static PlayTrackFragment newInstance(ArtistData artistData, ArrayList<TopTracksData> topTracksData, Integer trackPosition, Boolean twoPane) {
        PlayTrackFragment playTrackFragment = new PlayTrackFragment();
        // Set top track data before returning
        Bundle args = new Bundle();
        args.putParcelable("artistData", artistData);
        args.putParcelableArrayList("topTracksData", topTracksData);
        args.putInt("trackPosition", trackPosition);
        args.putBoolean("twoPane", twoPane);
        playTrackFragment.setArguments(args);
        return playTrackFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.dialog_play_track, container, false);

        // To be certain we are receiving the correct track info, we always get info from the
        // activity before trying to get info from savedInstanceState.
        if (this.getArguments() != null) {
            Bundle bundle = this.getArguments();
            mArtistData = bundle.getParcelable("artistData");
            mTopTracksData = bundle.getParcelableArrayList("topTracksData");
            mTrackPosition = bundle.getInt("trackPosition");
        } else if (savedInstanceState != null && savedInstanceState.containsKey("topTracksData") &&
                savedInstanceState.containsKey("artistData") &&
                savedInstanceState.containsKey("trackPosition")) {
            mArtistData = savedInstanceState.getParcelable("artistData");
            mTopTracksData = savedInstanceState.getParcelableArrayList("topTracksData");
            mTrackPosition = savedInstanceState.getInt("trackPosition");
        }

        if (mArtistData != null && mTopTracksData != null && mTrackPosition != null) {
            // Find views
            mPlayTimeElapsed = (TextView) mRootView.findViewById(R.id.dialogPlayTimeElapsed);
            mPlayTimeLeft = (TextView) mRootView.findViewById(R.id.dialogPlayTimeLeft);
            ImageButton previousTrack = (ImageButton) mRootView.findViewById(R.id.dialogBtnPlayPrevious);
            mPlayTrackToggle = (ImageButton) mRootView.findViewById(R.id.dialogBtnPlayToggle);
            ImageButton nextTrack = (ImageButton) mRootView.findViewById(R.id.dialogBtnPlayNext);
            mSeekBar = (SeekBar) mRootView.findViewById(R.id.dialogSeekBar);

            // Set click listeners
            previousTrack.setOnClickListener(this);
            mPlayTrackToggle.setOnClickListener(this);
            nextTrack.setOnClickListener(this);
            mSeekBar.setOnSeekBarChangeListener(this);

            // If track was paused, start playing from where we left of.
            if (mActivity != null && mActivity.trackController("isPlaying", null) != null &&
                    !mActivity.trackController("isPlaying", null)) {
                mActivity.trackController("start", null);
            }
            updateUI(mTrackPosition, true);
            setUIPlaying();
            updateSeekBarTimes();
        } else {
            Log.e(LOG_TAG, "Could not fetch necessary data");
        }
        return mRootView;
    }

    @Override
     public void onSaveInstanceState(Bundle outState) {
        if (mArtistData != null && mTopTracksData != null && mTrackPosition != null && mTwoPane != null) {
            outState.putParcelable("artistData", mArtistData);
            outState.putParcelableArrayList("topTracksData", mTopTracksData);
            outState.putInt("trackPosition", mTrackPosition);
            outState.putBoolean("twoPane", mTwoPane);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        Log.v(LOG_TAG, v.getId() + "");
        Integer trackPosition;
        switch(v.getId()) {
            case R.id.dialogBtnPlayToggle:
                Log.v("onClick: ", "start / pause music");
                Boolean isPlaying = mActivity.trackController("isPlaying", null);
                if (isPlaying) {
                    if (mActivity.trackController("pause", null)) { // if track paused
                        setUIPaused();
                        removeSeekBarHandler();
                    }
                } else {
                    if (mActivity.trackController("start", null)) { // if track started
                        setUIPlaying();
                        updateSeekBarTimes();
                    }
                }
                break;
            case R.id.dialogBtnPlayNext:
                mActivity.trackController("next", null);
                trackPosition = mActivity.getTrackPosition();
                updateUI(trackPosition, false);
                setUIPlaying();
                updateSeekBarTimes();
                break;
            case R.id.dialogBtnPlayPrevious:
                mActivity.trackController("previous", null);
                trackPosition = mActivity.getTrackPosition();
                updateUI(trackPosition, false);
                setUIPlaying();
                updateSeekBarTimes();
                break;
        }
    }

    // Notification that the SeekBar progress level has changed.
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // Set SeekBar times
        mPlayTimeElapsed.setText(Integer.toString(progress));
        mPlayTimeLeft.setText(Integer.toString((progress - mSeekBar.getMax())));
    }

    // Notification that the user has started a SeekBar touch gesture.
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    // Notification that the user has finished a SeekBar touching gesture.
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Play song from selected SeekBar progress
        mActivity.trackController("seekTo", seekBar.getProgress());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeSeekBarHandler();
    }

    private void updateUI(Integer trackPosition, Boolean forceUpdate) {
        if (forceUpdate || !mTrackPosition.equals(trackPosition)) {
            mTrackPosition = trackPosition;
            TopTracksData topTrack = mTopTracksData.get(mTrackPosition);

            // Find views
            TextView artistTitle = (TextView) mRootView.findViewById(R.id.dialogArtistTitle);
            TextView albumTitle = (TextView) mRootView.findViewById(R.id.dialogAlbumTitle);
            TextView trackTitle = (TextView) mRootView.findViewById(R.id.dialogTrackTitle);
            ImageView albumCover = (ImageView) mRootView.findViewById(R.id.dialogAlbumCover);

            // Set data to each view
            artistTitle.setText(mArtistData.name);
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
        }
    }

    private void setUIPlaying() {
        mPlayTrackToggle.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void setUIPaused() {
        mPlayTrackToggle.setImageResource(android.R.drawable.ic_media_play);
    }

    // Update SeekBar times
    // http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay/6242292#6242292
    private void updateSeekBarTimes() {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        mTimeChecker = new Runnable() {
            @Override
            public void run() {
                int interval = 200;
                Integer[] times = mActivity.updateTimes();
                Integer trackPosition = mActivity.getTrackPosition();
                // Update UI if song has changed
                // TODO: use onCompletionListener
                if (trackPosition != null && !mTrackPosition.equals(trackPosition)) {
                    updateUI(trackPosition, false);
                }
                // get SeekBar times and set progress
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
    }

    private void removeSeekBarHandler() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mTimeChecker);
            mTimeChecker = null;
        }
    }
}
