package no.ahoi.spotify.spotifystreamer;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class TopTracksActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        // Place logo on action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ic_launcher);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        String[] artistData = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            artistData = extras.getStringArray("artistData");
            Log.v("TopTracksActivity", " spotify ID: " + artistData[0] + " name: " + artistData[1]);
            actionBar.setTitle(artistData[1]); // Change action bar title to artist name

        }
        // TODO Delete this activity and send data directly to the other fragment.
        /*if (savedInstanceState == null) {
            TopTracksActivityFragment topTracksFragment = new TopTracksActivityFragment();
            if (artistData != null) {
                Bundle args = new Bundle();
                args.putString("spotifyId", artistData[0]);
                topTracksFragment.setArguments(args);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.topTracksFragment, topTracksFragment)
                    .commit();
        }*/


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
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
}
