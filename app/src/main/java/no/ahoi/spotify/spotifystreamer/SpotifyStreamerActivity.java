package no.ahoi.spotify.spotifystreamer;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class SpotifyStreamerActivity extends AppCompatActivity implements SpotifySearchArtistFragment.OnArtistSelectedListener {
    private static final String TAG = SpotifyStreamerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_streamer);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ic_launcher);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onArtistSelected(String[] artistData) {
        Log.v("works", " spotify ID: " + artistData[0] + " name: " + artistData[1]);

        Intent intent = new Intent(getApplicationContext(), TopTracksActivity.class);
        intent.putExtra("artistData", artistData);
        startActivity(intent);

        /*TopTracksActivityFragment topTracksFrag = (TopTracksActivityFragment) getSupportFragmentManager().findFragmentById(R.id.topTracksFragment);

        if (topTracksFrag == null) {

        }
        FragmentManager fragmentManager = getFragmentManager();

        TopTracksActivityFragment topTracksFragment = new TopTracksActivityFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        int containerId = R.id.topTracksFragment;
        */
    }
}
