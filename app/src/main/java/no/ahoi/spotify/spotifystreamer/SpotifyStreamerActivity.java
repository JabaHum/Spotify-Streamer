package no.ahoi.spotify.spotifystreamer;

import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;


public class SpotifyStreamerActivity extends AppCompatActivity implements SearchArtistFragment.OnArtistSelectedListener {
    private static final String TAG = SpotifyStreamerActivity.class.getSimpleName();
    ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_streamer);

        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setLogo(R.mipmap.ic_launcher);
            mActionBar.setDisplayUseLogoEnabled(true);
            mActionBar.setDisplayShowHomeEnabled(true);
        } else {
            Log.v(TAG, " getSupportActionBar() returned null");
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
            getFragmentManager().beginTransaction()
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

    public void onArtistSelected(String[] artistData) {
        Log.v(TAG, " spotify ID: " + artistData[0] + " name: " + artistData[1]);
        
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(findViewById(R.id.searchArtists).getWindowToken(), 0);

        TopTracksFragment topTracksFragment = new TopTracksFragment();
        Bundle args = new Bundle();
        args.putStringArray("artistData", artistData);
        topTracksFragment.setArguments(args);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the spotifySearchFragmentContainer view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        ft.replace(R.id.SearchFragmentPlaceholder, topTracksFragment);
        ft.addToBackStack(null);
        ft.commit();

        setActionBarData(getString(R.string.top_tracks), artistData[1]);
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
}
