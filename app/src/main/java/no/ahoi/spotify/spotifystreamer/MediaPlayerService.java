package no.ahoi.spotify.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

/*
    Can be called with startService(Intent service); catch (SecurityExcetion)
    Can be stopped with stopSelf() or stopService(Intent service).

 */


public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener {
    private static final String LOG_TAG = PlayTrackFragment.class.getSimpleName();
    private static final String ACTION_INITIATE = "no.ahoi.spotify.spotifystreamer.action.INITIATE";
    private static final String ACTION_PLAY = "no.ahoi.spotify.spotifystreamer.action.START";

    private final IBinder mBinder = new LocalBinder();
    MediaPlayer mMediaPlayer;
    TopTracksData mTopTrack;

    public MediaPlayerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // Do not call directly
        // onStartCommand is called when the service is requested to start by a component (activity)
        if (intent != null && intent.getAction() != null && intent.getExtras() != null &&
                intent.getExtras().containsKey("topTrack")) {
            final String action = intent.getAction();
            mTopTrack = intent.getExtras().getParcelable("topTrack");
            if (ACTION_INITIATE.equals(action)) {
                Log.v("ACTION: ", action);
                Log.v(LOG_TAG, mTopTrack.albumTitle);

                if (mMediaPlayer == null) {
                    // Initialize Media player
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
                    try {
                        mMediaPlayer.setDataSource(mTopTrack.previewUrl);
                        mMediaPlayer.setOnPreparedListener(this);
                        mMediaPlayer.prepareAsync(); // Prepare async to prevent blocking main thread.
                    } catch (IllegalArgumentException | IllegalStateException | IOException e) {
                        Log.e(LOG_TAG, "Cause: " + e.getCause() + " Message: " + e.getMessage());
                    }
                } else {
                    Log.e(LOG_TAG, "mMediaPlayer is not null.");
                }
            } else if (ACTION_PLAY.equals(action)) {
                // TODO Don't reset player if current track is already loaded and/or playing/paused/finished
                mMediaPlayer.reset();
                try {
                    // Load new source
                    mMediaPlayer.setDataSource(mTopTrack.previewUrl);
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
                    mMediaPlayer.setOnPreparedListener(this);
                    mMediaPlayer.prepareAsync();
                } catch (IllegalArgumentException | IllegalStateException | IOException e) {
                    Log.e(LOG_TAG, "Cause: " + e.getCause() + " Message: " + e.getMessage());
                }
            }
        }
        // START_STICKY tells the service to continue running until explicitly stopped.
        // the service can be stopped with stopSelf() or stopService(Intent service).
        // Todo perform cleanup!
        return START_STICKY;
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
}
