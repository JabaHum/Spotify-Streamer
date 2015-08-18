package no.ahoi.spotify.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

/*
    Can be called with startService(Intent service); catch (SecurityExcetion)
    Can be stopped with stopSelf() or stopService(Intent service).

 */


public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener {
    private static final String LOG_TAG = MediaPlayerService.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();
    protected MediaPlayer mMediaPlayer;
    private ArrayList<TopTracksData> mTopTracksData;
    private Integer mCurrentTrackPosition;
    private LocalBroadcastManager mBroadcaster;

    public MediaPlayerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // Do not call directly
        // onStartCommand is called when the service is requested to start by a component (activity)

        if (intent != null && intent.getExtras() != null &&
                intent.getExtras().containsKey("topTracksdata") &&
                intent.getExtras().containsKey("trackPosition")) {
            ArrayList<TopTracksData> topTracksData = intent.getExtras().getParcelableArrayList("topTracksdata");
            Integer trackPosition = intent.getExtras().getInt("trackPosition");
            if (topTracksData != null && !topTracksData.isEmpty()) {
                if (mTopTracksData != null && mCurrentTrackPosition != null) {
                    if (!mCurrentTrackPosition.equals(trackPosition) ||
                            !mTopTracksData.get(mCurrentTrackPosition).id.equals(topTracksData.get(mCurrentTrackPosition).id)) {
                        // Another track has been selected. Play new track.
                        Log.v(LOG_TAG, "Another track has been selected. Play new track.");
                        mTopTracksData = topTracksData;
                        mCurrentTrackPosition = trackPosition;
                        playTrack(mTopTracksData.get(mCurrentTrackPosition));
                    } else {
                        // Track is already loaded. Don't start over.
                        Log.v(LOG_TAG, "Track is already loaded. Don't start over.");
                    }
                } else {
                // No previous data found. Play track.
                Log.v(LOG_TAG, "No previous data found. Play track.");
                mTopTracksData = topTracksData;
                mCurrentTrackPosition = trackPosition;
                playTrack(mTopTracksData.get(mCurrentTrackPosition));

                }
            } else {
                Log.e(LOG_TAG, "topTracksData is missing.");
            }
        }
        // START_STICKY tells the service to continue running until explicitly stopped.
        // the service can be stopped with stopSelf() or stopService(Intent service).
        return START_STICKY;
    }

    private void playTrack(TopTracksData topTrack) {
        if (mMediaPlayer == null) {
            // Initialize Media player
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        } else {
            mMediaPlayer.reset();
        }
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(LOG_TAG, "OnErrorListener() - what: " + what);
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MediaPlayerService.this, "A wild error appeared.", Toast.LENGTH_SHORT).show();
                    }
                });

                // The MediaPlayer has moved to the Error state. Reset.
                mp.reset();
                return false;
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.v("OnCompletionListener: ", "Track completed. starting next...");
                playNextTrack();
                updateUI();
            }
        });
        try {
            mMediaPlayer.setDataSource(topTrack.previewUrl);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync(); // Prepare async to prevent blocking main thread.
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.e(LOG_TAG, "Cause: " + e.getCause() + " Message: " + e.getMessage());
        }
    }

    private TopTracksData getNextTrack() {
        mCurrentTrackPosition++;
        if (mCurrentTrackPosition == mTopTracksData.size()) { // End of track list
            mCurrentTrackPosition = 0;
        }
        return mTopTracksData.get(mCurrentTrackPosition);
    }

    private TopTracksData getPreviousTrack() {
        mCurrentTrackPosition--;
        if (mCurrentTrackPosition == -1) { // End of track list
            mCurrentTrackPosition = mTopTracksData.size() - 1;
        }
        return mTopTracksData.get(mCurrentTrackPosition);
    }

    protected void playNextTrack() {
        playTrack(getNextTrack());
    }

    protected void playPreviousTrack() {
        playTrack(getPreviousTrack());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        return mBinder;
    }

    public class LocalBinder extends Binder {
        MediaPlayerService getService() {
            // Return this instance of MediaPlayerService so that clients can call the public methods.
            return MediaPlayerService.this;
        }
    }

    @Override
    public void onCreate() { // Do not call directly
        // Called by the system when the system is first created.
        // onCreate() will not be called if the service is already running.
        mBroadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onDestroy() { // Do not call directly
        // The service is no longer used and will be removed.
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // Finished preparing. Start playing.
        mp.start();
    }

    public Integer getTrackPosition() {
        return mCurrentTrackPosition;
    }

    private void updateUI() {
        Intent intent = new Intent("sendTrackPosition");
        intent.putExtra("trackPosition", mCurrentTrackPosition);
        mBroadcaster.sendBroadcast(intent);
    }
}
