package no.ahoi.spotify.spotifystreamer;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;


public class SpotifyStreamerActivity extends AppCompatActivity implements SpotifySearchArtistActivityFragment.OnArtistSelectedListener {
    private static final String TAG = SpotifyStreamerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_streamer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spotify_streamer, menu);
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

    public void onArtistSelected(int position) {
        Log.v("works", " position: " + position);

        Intent intent = new Intent(getApplicationContext(), TopTracksActivity.class);
        intent.putExtra("position", position);
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
