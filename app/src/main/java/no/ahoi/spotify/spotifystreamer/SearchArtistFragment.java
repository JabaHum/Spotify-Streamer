package no.ahoi.spotify.spotifystreamer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;


/**
 * lists artists based on user input
 */
public class SearchArtistFragment extends Fragment {
    private static final String LOG_TAG = SearchArtistFragment.class.getSimpleName();
    private ArrayAdapter<ArtistData> mSpotifySearchAdapter;
    OnArtistSelectedListener mCallback;

    public SearchArtistFragment() {
    }

    // The container Activity must implement this interface so the fragment can deliver messages
    public interface OnArtistSelectedListener {
        void onArtistSelected(String[] artistData);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface and throws an exception otherwise.
        try {
            mCallback = (OnArtistSelectedListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArtistSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_artist, container, false);

        EditText searchArtists = (EditText) rootView.findViewById(R.id.searchArtists);

        searchArtists.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    FetchArtistsTask artists = new FetchArtistsTask();
                    artists.execute(s.toString());
                }
                else {
                    mSpotifySearchAdapter.clear();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSpotifySearchAdapter = new ArtistSearchAdapter(getActivity(), new ArrayList<ArtistData>());

        // Find a reference to the fragments ListView to attach the adapter
        ListView listArtists = (ListView) rootView.findViewById(R.id.listArtists);
        listArtists.setAdapter(mSpotifySearchAdapter);


        listArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Fetch info about clicked artist and send info to host activity
                ArtistData artistData = mSpotifySearchAdapter.getItem(position);
                String[] artist = new String[2];
                artist[0] = artistData.id;
                artist[1] = artistData.name;
                mCallback.onArtistSelected(artist);
            }
        });

        return rootView;
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, ArrayList<String[]>> {
        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        @Override
        protected ArrayList<String[]> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(params[0]);

            return getArtistData(results);
        }

        @Override
        protected void onPostExecute(ArrayList<String[]> result) {
            if (result != null) {
                mSpotifySearchAdapter.clear();
                for(String[] artistData : result) {
                    ArtistData artist = new ArtistData(artistData[0], artistData[1], artistData[2]);
                    mSpotifySearchAdapter.add(artist);
                }

                if (mSpotifySearchAdapter.getCount() == 0) {
                    Toast.makeText(getActivity(), "Could not find artist.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private ArrayList<String[]> getArtistData(ArtistsPager spotifyArtists) {
            int i = 0;
            ArrayList<String[]> artistNames = new ArrayList<>();
            for(Artist artist : spotifyArtists.artists.items) {
                i++;
                String[] artistData = new String[3];

                artistData[0] = artist.id; // Get spotify ID
                artistData[1] = artist.name; // Save artist name
                for (Image image : artist.images) {
                    if (image.height > 200 || image.width > 200) { // image sizes are always in the order: big to small
                        artistData[2] = image.url; // Get image url
                    }
                    else if (artistData[2] == null) { // Fallback to smallest image.
                        artistData[2] = image.url;
                    }
                }

                artistNames.add(artistData);
                Log.v(LOG_TAG, "Spotify Artist #" + i + " " + artist.name);
            }
            return artistNames;
        }
    }

    public class ArtistSearchAdapter extends ArrayAdapter<ArtistData> {
        public ArtistSearchAdapter(Context context, ArrayList<ArtistData> artists) {
            super(context, 0, artists);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ArtistData artistData = getItem(position);
            // Check if existing view is being reused
            if (convertView == null) {
                // Inflate view
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
            }

            // Find view to populate data
            ImageView ivArtistImage = (ImageView) convertView.findViewById(R.id.listItemArtistImageView);
            TextView tvArtistName = (TextView) convertView.findViewById(R.id.listItemArtistTextView);

            tvArtistName.setText(artistData.name);
            if (artistData.imageUrl != null) {
                Picasso.with(getContext()).load(artistData.imageUrl).placeholder(R.mipmap.no_image).into(ivArtistImage);
            } else {
                Picasso.with(getContext()).load(R.mipmap.no_image).into(ivArtistImage);
            }

            Picasso.with(getContext()).load(artistData.imageUrl).placeholder(R.mipmap.no_image).into(ivArtistImage);

            return convertView;
        }
    }

    public class ArtistData {
        public String id, name, imageUrl;

        public ArtistData(String spotifyId, String name, String imageUrl) {
            this.id = spotifyId;
            this.name = name;
            this.imageUrl = imageUrl;
        }
    }
}
