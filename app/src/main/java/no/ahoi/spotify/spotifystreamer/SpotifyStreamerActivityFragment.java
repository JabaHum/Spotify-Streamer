package no.ahoi.spotify.spotifystreamer;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * lists artists based on user input
 */
public class SpotifyStreamerActivityFragment extends Fragment {
    private static final String TAG = SpotifyStreamerActivityFragment.class.getSimpleName();
    private ArrayAdapter<String> mSpotifySearchAdapter;

    public SpotifyStreamerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_spotify_streamer, container, false);

        EditText searchArtists = (EditText) rootView.findViewById(R.id.searchArtists);

        searchArtists.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FetchArtistsTask artists = new FetchArtistsTask();
                artists.execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Dummy data for the ListView
        String[] spotifyData = {
                "No artists found"
        };

        // Use ArrayAdapter to translate the spotify data into views for the ListView to display
        List<String> artists = new ArrayList<>(Arrays.asList(spotifyData));
        mSpotifySearchAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_artist, R.id.listItemArtistTextView, artists);

        // Find a reference to the fragments ListView to attach the adapter
        ListView listArtists = (ListView) rootView.findViewById(R.id.listArtists);
        listArtists.setAdapter(mSpotifySearchAdapter);

        return rootView;
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, ArrayList<String>> {
        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        private ArrayList<String> getArtistData(ArtistsPager spotifyArtists) {
            int i = 1;
            ArrayList<String> artistNames = new ArrayList<>();
            for(Artist artist : spotifyArtists.artists.items) {
                artistNames.add(artist.name);
                Log.v(LOG_TAG, "Spotify Artist #" + i + " " + artist.name);
                i++;
            }
            return artistNames;
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(params[0]);
            ArrayList<String> artistList = getArtistData(results);

            return artistList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (result != null) {
                mSpotifySearchAdapter.clear();
                for(String spotifyArtistStr : result) {
                    mSpotifySearchAdapter.add(spotifyArtistStr);
                }
            }
        }
    }
}
