package no.ahoi.spotify.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/*
    Can be called with startService(Intent service); catch (SecurityExcetion)
    Can be stopped with stopSelf() or stopService(Intent service).

 */


public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener {
    private static final String LOG_TAG = PlayTrackFragment.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();
    MediaPlayer mMediaPlayer;
    ArrayList<TopTracksData> mTopTracksData;
    Integer mCurrentTrackPosition;

    public MediaPlayerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // Do not call directly
        // onStartCommand is called when the service is requested to start by a component (activity)
        if (intent != null && intent.getExtras() != null &&
                intent.getExtras().containsKey("topTracksdata")) {
            mTopTracksData = intent.getExtras().getParcelableArrayList("topTracksdata");
            mCurrentTrackPosition = intent.getExtras().getInt("trackPosition");
            TopTracksData topTrack = mTopTracksData.get(mCurrentTrackPosition);
            Log.v(LOG_TAG, topTrack.albumTitle);
            playTrack(topTrack);
        }
        // START_STICKY tells the service to continue running until explicitly stopped.
        // the service can be stopped with stopSelf() or stopService(Intent service).
        // Todo perform cleanup!
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
                Log.e(LOG_TAG, "MediaPlayer()->OnErrorListener()->OnError() something went wrong");
                // TODO Toast!
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
    }

    @Override
    public void onDestroy() { // Do not call directly
        // The service is no longer used and will be removed.
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // Finished preparing. Start playing.
        mp.start();
    }

    public Integer getTrackPosition() {
        return mCurrentTrackPosition;
    }
}
