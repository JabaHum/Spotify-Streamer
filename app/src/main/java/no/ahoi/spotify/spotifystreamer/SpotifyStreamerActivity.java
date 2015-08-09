package no.ahoi.spotify.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
    ActionBar mActionBar;
    MediaPlayerService mService;
    Boolean mBound = false;

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
        } else {
            Log.v(LOG_TAG, " getSupportActionBar() returned null");
        }

        // Check that the activity is using the layout version with the container FrameLayout
        if (findViewById(R.id.SearchFragmentPlaceholder) != null) {
            // If we're being restored from a previous state, then we don't need to do anything
            // and should return or else we could end up with overlapping fragments
            if (savedInstanceState != null) {
                return;
            }

            // Create a new fragment to be placed in the activity layout
            SearchArtistFragment searchArtistFragment = new SearchArtistFragment();
            // In the case this activity was started with special instructions from an Intent,
            // pass the Intent's extras tot he fragment as arguments.
            searchArtistFragment.setArguments(getIntent().getExtras());
            // Add the fragment to the 'spotifySearchFragmentContainer' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.SearchFragmentPlaceholder, searchArtistFragment).commit();
        }
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
        topTracksFragment.setArguments(args);

        // Replace whatever is in the spotifySearchFragmentContainer view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.SearchFragmentPlaceholder, topTracksFragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        setActionBarData(getString(R.string.top_tracks), artistData.name);
    }

    public void onTopTrackSelected(ArtistData artistData, ArrayList<TopTracksData> topTracksData, Integer trackPosition) {
        /*
         * To allow the service to play the next song when current is finished, even if the
         * activity or fragment is destroyed, we send the complete track list to the service.

         * To update fragment ui when next song starts playing, we send all track info to fragment
         * as well. Whenever the fragment view resumes, we get the track position from the service
         * and update ui if needed.
         * If service is killed (f.ex after being inactive), we can play song from fragment.
         */

        // show PlayTrackFragment
        FragmentManager fm = getSupportFragmentManager();
        PlayTrackFragment playTrackFragment = PlayTrackFragment.newInstance(artistData, topTracksData, trackPosition);
        playTrackFragment.show(fm, "dialog_play_track");

        startMediaPlayer(topTracksData, trackPosition);
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

        if (mBound && mService != null && mService.mMediaPlayer != null) {
            MediaPlayer mp = mService.mMediaPlayer;
            switch (command) {
                // TODO find better return solution
                case "start":
                    if (!mp.isPlaying()) {
                        mp.start();
                        Log.v(TAG, "starting track.");
                        return true;
                    } else {
                        return false;
                    }
                case "pause":
                    if (mp.isPlaying()) {
                        mp.pause();
                        Log.v(TAG, "Pausing track.");
                        return true;
                    } else {
                        return false;
                    }
                case "isPlaying":
                    return mp.isPlaying();
                case "seekTo":
                    mp.seekTo(position * 1000);
                    return true;
                case "next":
                    mService.playNextTrack();
                    return true;
                case "previous":
                    mService.playPreviousTrack();
                    return true;
            }
        }

        if (mService == null) {
            Log.e("mService", "IS NULL!!");
        } else {
            if (mService.mMediaPlayer == null) {
                Log.e("mMediaPlayer", "IS NULL!!");
            }
        }
        return null;
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
}
