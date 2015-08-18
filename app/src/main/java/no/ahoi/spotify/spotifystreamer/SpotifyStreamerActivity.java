package no.ahoi.spotify.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

public class SpotifyStreamerActivity extends AppCompatActivity implements SearchArtistFragment.OnArtistSelectedListener, TopTracksFragment.OnTopTrackSelectedListener {
    private static final String LOG_TAG = SpotifyStreamerActivity.class.getSimpleName();
    private ActionBar mActionBar;
    protected MediaPlayerService mService;
    private Boolean mBound = false;
    private Boolean mTwoPane;
    private Boolean mRegisterReceiver = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_streamer);

        Log.v("4", "onCreate");

        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setLogo(R.mipmap.ic_launcher);
            mActionBar.setDisplayUseLogoEnabled(true);
            mActionBar.setDisplayShowHomeEnabled(true);
        }

        // Rebind service
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("bound")) {
                Intent intent = new Intent(this, MediaPlayerService.class);
                bindService(intent, mConnection, 0);
            }
            mRegisterReceiver = true;
        }

        // Check if activity is in two-pane mode
        if (findViewById(R.id.topTracksContainer) != null) {
            mTwoPane = true;
            // SearchArtistFragment is added statically.
        } else {
            mTwoPane = false;
            // If we're being restored from a previous state, then we don't need to do
            // anything and should return or else we could end up with overlapping fragments
            if (savedInstanceState != null) {
                return;
            }
            // Add SearchArtistFragment Dynamically:
            // Create a new fragment to be placed in the activity layout
            SearchArtistFragment searchArtistFragment = new SearchArtistFragment();
            // Pass two-pane info to fragment
            Bundle args = new Bundle();
            args.putBoolean("twoPane", mTwoPane);
            searchArtistFragment.setArguments(args);
            // Add the fragment to the 'searchArtistsFragmentPlaceholder' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.searchArtistsFragmentPlaceholder, searchArtistFragment).commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mTwoPane != null) {
            outState.putBoolean("twoPane", mTwoPane);
        }
        outState.putBoolean("bound", mBound);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Let user select country from options menu
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spotify_streamer, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onArtistSelected(ArtistData artistData) {
        Log.v(LOG_TAG, " spotify ID: " + artistData.spotifyId + " name: " + artistData.name);
        
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.searchArtists).getWindowToken(), 0);

        TopTracksFragment topTracksFragment = new TopTracksFragment();
        Bundle args = new Bundle();
        args.putParcelable("artistData", artistData);
        args.putBoolean("twoPane", mTwoPane);
        topTracksFragment.setArguments(args);

        // Replace whatever is in the spotifySearchFragmentContainer view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.topTracksContainer, topTracksFragment)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.searchArtistsFragmentPlaceholder, topTracksFragment)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }

        setActionBarData(getString(R.string.top_tracks), artistData.name);
    }

    public void onTopTrackSelected(ArtistData artistData, ArrayList<TopTracksData> topTracksData, Integer trackPosition) {
        /*
         * To allow the service to play the next song when current is finished, even if the
         * activity or fragment is destroyed, we send the complete track list to the service.

         * To update fragment ui when next song starts playing, we send all track info to fragment
         * as well. Whenever the fragment view resumes, we get the track position from the service
         * and update ui if needed.
         */

        // show PlayTrackFragment
        FragmentManager fm = getSupportFragmentManager();
        PlayTrackFragment playTrackFragment = PlayTrackFragment.newInstance(artistData, topTracksData, trackPosition, mTwoPane);
        playTrackFragment.show(fm, "dialog_play_track");

        startMediaPlayer(topTracksData, trackPosition);

        if (mRegisterReceiver) {
            LocalBroadcastManager.getInstance(this).registerReceiver((mReceiver),
                    new IntentFilter("sendTrackPosition")
            );
            mRegisterReceiver = false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        resetActionBarData();
    }

    protected void setActionBarData(String title, String subtitle) {
        mActionBar.setTitle(title);
        mActionBar.setSubtitle(subtitle);
    }
    protected void resetActionBarData() {
        mActionBar.setTitle(getString(R.string.app_name));
        mActionBar.setSubtitle(null);
    }
    @Override
    public void onPause() {
        super.onPause();
        // Prepare for app destruction
        Log.v("1", "onPause");
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.v("2", "onStop");
        if (!mRegisterReceiver) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            // Must reset flag in case home button is pressed (because onCreate() doesn't run)
            mRegisterReceiver = true;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("3", "onDestroy");
        // unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.v("5", "onStart");
        if (mRegisterReceiver) {
            LocalBroadcastManager.getInstance(this).registerReceiver((mReceiver),
                    new IntentFilter("sendTrackPosition")
            );
            mRegisterReceiver = false;
        } else {
            Log.e(LOG_TAG, "Could not start broadcastReceiver.");
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.v("6", "onResume");
    }

    // Defines callbacks for service binding, passed to the bindService() method
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Successful bind to the MediaPlayerService, cast the IBinder and get the service's instance.
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    public Boolean trackController(String command, Integer position) {
        String TAG = "trackController()";
        Boolean returnFlag = false;
        if (mBound && mService != null && mService.mMediaPlayer != null) {
            MediaPlayer mp = mService.mMediaPlayer;
            switch (command) {
                case "start":
                    if (!mp.isPlaying()) {
                        Log.v(TAG, "starting track.");
                        mp.start();
                        returnFlag = true;
                    }
                    break;
                case "pause":
                    if (mp.isPlaying()) {
                        Log.v(TAG, "Pausing track.");
                        mp.pause();
                        returnFlag = true;
                    }
                    break;
                case "isPlaying":
                    return mp.isPlaying();
                case "seekTo":
                    mp.seekTo(position * 1000);
                    returnFlag = true;
                    break;
                case "next":
                    mService.playNextTrack();
                    returnFlag = true;
                    break;
                case "previous":
                    mService.playPreviousTrack();
                    returnFlag = true;
                    break;
            }
        }

        if (mService == null) {
            Log.e(LOG_TAG, "mService is null.");
        } else if (mService.mMediaPlayer == null) {
            Log.e(LOG_TAG, "mMediaPlayer is null.");
        }
        return returnFlag;
    }

    public Integer[] updateTimes() {
        // fetch times to update UI
        if (mBound && mService != null && mService.mMediaPlayer != null && trackController("isPlaying", null)) {
            Integer[] times = new Integer[2];
            // nitpick: no need to call getDuration() more than once
            times[0] = mService.mMediaPlayer.getDuration();
            times[1] = mService.mMediaPlayer.getCurrentPosition();
            return times;
        } else {
            return null;
        }
    }

    private void startMediaPlayer(ArrayList<TopTracksData> topTracksData, Integer trackPosition) {
        // Prepare track for MediaPlayerService
        Intent intent = new Intent(this, MediaPlayerService.class);
        Bundle args = new Bundle();
        args.putParcelableArrayList("topTracksdata", topTracksData);
        args.putInt("trackPosition", trackPosition);
        intent.putExtras(args);
        // Run MediaPlayer Service and bind to it
        this.startService(intent);
        bindService(intent, mConnection, 0);
    }

    protected Integer getTrackPosition() {
        if (mBound && mService != null) {
            return mService.getTrackPosition();
        } else {
            Log.e(LOG_TAG + "->getTrackPosition()", "Could not get current track position");
            return null;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Updates UI in PlayTrackFragment when service's onCompleteListener runs.
            Integer trackPosition = intent.getIntExtra("trackPosition", 0);
            PlayTrackFragment playTrackFragment = (PlayTrackFragment) getSupportFragmentManager().findFragmentByTag("dialog_play_track");
            // Fails if playTrackFragment isn't visible.
            if (playTrackFragment != null) {
                playTrackFragment.updateUI(trackPosition, false);
            } else {
                Log.v(LOG_TAG, "playTrackFragment is null.");
            }
        }
    };
}
