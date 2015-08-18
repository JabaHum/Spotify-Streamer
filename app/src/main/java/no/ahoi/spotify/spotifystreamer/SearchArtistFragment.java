package no.ahoi.spotify.spotifystreamer;

import android.app.Activity;
import android.support.v4.app.Fragment;
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
import retrofit.RetrofitError;


/**
 * lists artists based on user input
 */

public class SearchArtistFragment extends Fragment {
    private static final String LOG_TAG = SearchArtistFragment.class.getSimpleName();
    private ArrayAdapter<ArtistData> mSpotifySearchAdapter;
    private OnArtistSelectedListener mCallback;
    private ArrayList<ArtistData> mArtistData;
    private CharSequence mSearchSequence;

    public SearchArtistFragment() {
    }

    // The container Activity must implement this interface so the fragment can deliver messages
    public interface OnArtistSelectedListener {
        void onArtistSelected(ArtistData artistData);
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

        /* This triggers if we return to the fragment from the back stack.
         * The fragment is not re-created, but is re-used with the same instance.
         * We can't rely on savedInstanceState because onCreateView() is the
         * first method called in the fragment lifecycle.
         * Source: http://stackoverflow.com/questions/11353075/how-can-i-maintain-fragment-state-when-added-to-the-back-stack?answertab=votes#tab-top
         */
        if (mArtistData != null) {
            for (ArtistData artist : mArtistData) {
                mSpotifySearchAdapter.add(artist);
            }
        } else {
            mSpotifySearchAdapter = new ArtistSearchAdapter(getActivity(), new ArrayList<ArtistData>());
            // Populate data from savedInstanceState
            if (savedInstanceState != null && savedInstanceState.containsKey("artistData")) {
                mArtistData = savedInstanceState.getParcelableArrayList("artistData");
                if (mArtistData != null) {
                    for (ArtistData artist : mArtistData) {
                        mSpotifySearchAdapter.add(artist);
                    }
                }
                if (savedInstanceState.containsKey("searchSequence")) {
                    mSearchSequence = savedInstanceState.getCharSequence("searchSequence");
                }
            }
        }


        EditText searchArtists = (EditText) rootView.findViewById(R.id.searchArtists);

        searchArtists.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence searchSeq, int start, int before, int count) {
                if (searchSeq.length() > 0) {
                    /* mSearchSequence is used to prevent adapter execution when the search
                     * haven't changed. For example on orientation change or when
                     * we are returning to this fragment from the back stack */
                    if (mSearchSequence == null || !searchSeq.toString().contentEquals(mSearchSequence)) {
                        // Execute AsyncTask to fetch and populate artists
                        FetchArtistsTask artists = new FetchArtistsTask();
                        artists.execute(searchSeq.toString());
                    }
                    mSearchSequence = searchSeq;
                } else {
                    mSpotifySearchAdapter.clear();
                    mSearchSequence = null;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Find a reference to the fragments ListView to attach the adapter
        ListView listArtists = (ListView) rootView.findViewById(R.id.listArtists);
        listArtists.setAdapter(mSpotifySearchAdapter);

        listArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Send info about selected artist to host activity
                mCallback.onArtistSelected(mSpotifySearchAdapter.getItem(position));
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSearchSequence != null) {
            outState.putCharSequence("searchSequence", mSearchSequence);
        }
        super.onSaveInstanceState(outState);
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, ArrayList<String[]>> {
        private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

        @Override
        protected ArrayList<String[]> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }
            ArtistsPager results;
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            try {
                results = spotify.searchArtists(params[0]);
            }
            catch (RetrofitError e) {
                Log.v(LOG_TAG + "->doInBackground()", e.toString() + " - Error kind: " + e.getKind());
                return null;
            }
            return getArtistData(results);
        }

        @Override
        protected void onPostExecute(ArrayList<String[]> result) {
            if (result != null) {
                mSpotifySearchAdapter.clear();
                mArtistData = new ArrayList<>();
                int i = 0;
                for(String[] artistData : result) {
                    ArtistData artist = new ArtistData(artistData[0], artistData[1], artistData[2]);
                    mSpotifySearchAdapter.add(artist);
                    mArtistData.add(i, artist);
                    i++;
                }

                if (mSpotifySearchAdapter.getCount() == 0) {
                    Toast.makeText(getActivity(), "Could not find artist.", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(getActivity(), "Could not fetch artist - please check your internet connection", Toast.LENGTH_SHORT).show();
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
}
