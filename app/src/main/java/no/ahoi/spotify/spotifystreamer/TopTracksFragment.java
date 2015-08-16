package no.ahoi.spotify.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * lists top tracks for an artist by fetching data from the Spotify Web API
 */
public class TopTracksFragment extends Fragment {
    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();
    private ArrayAdapter<TopTracksData> mTopTracksAdapter;
    private ArrayList<TopTracksData> mTopTracksData;
    private ArtistData mArtistData;
    private OnTopTrackSelectedListener mCallback;

    public TopTracksFragment() {
    }

    // The container Activity must implement this interface so the fragment can deliver messages
    public interface OnTopTrackSelectedListener {
        void onTopTrackSelected(ArtistData artistdata, ArrayList<TopTracksData> topTracksData, Integer trackPosition);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface and throws an exception otherwise.
        try {
            mCallback = (OnTopTrackSelectedListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTopTrackSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        mTopTracksAdapter = new TopTracksSearchAdapter(getActivity(), new ArrayList<TopTracksData>());

        // Save artistData
        if (savedInstanceState != null && savedInstanceState.containsKey("artistData")) {
            mArtistData = savedInstanceState.getParcelable("artistData");
        }

        // Populate list if we already have topTracksData, otherwise fetch it from the spotify API.
        if (savedInstanceState != null && savedInstanceState.containsKey("topTracksData")) {
            mTopTracksData = savedInstanceState.getParcelableArrayList("topTracksData");
            if (mTopTracksData != null) {
                for (TopTracksData topTrack : mTopTracksData) {
                    mTopTracksAdapter.add(topTrack);
                }
            } else {
                Log.e(LOG_TAG, "mTopTracksData is null.");
            }

        } else if (this.getArguments() != null) {
            // Expects artist Spotify ID
            Bundle bundle = this.getArguments();
            mArtistData = bundle.getParcelable("artistData");
            FetchTopTracksTask topTracksTask = new FetchTopTracksTask();
            topTracksTask.execute(mArtistData.spotifyId);
        } else {
            Log.v(LOG_TAG, " Could not fetch artistData activity");
        }

        // Find a reference to the fragments ListView to attach the adapter
        ListView listTracks = (ListView) rootView.findViewById(R.id.listTracks);
        listTracks.setAdapter(mTopTracksAdapter);

        listTracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Send artist info to host activity
                mCallback.onTopTrackSelected(mArtistData, mTopTracksData, position);
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mTopTracksData != null && !mTopTracksData.isEmpty() && mArtistData != null) {
            outState.putParcelableArrayList("topTracksData", mTopTracksData);
            outState.putParcelable("artistData", mArtistData);
        }
        super.onSaveInstanceState(outState);
    }

    public class FetchTopTracksTask extends AsyncTask<String, Void, ArrayList<String[]>> {
        private final String LOG_TAG = FetchTopTracksTask.class.getSimpleName();

        @Override
        protected ArrayList<String[]> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }
            Tracks results;
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            // Pass in country as option (required by getArtistTopTracks)
            Map<String, Object> options = new HashMap<>();
            options.put("country", Locale.getDefault().getCountry());
            try {
                results = spotify.getArtistTopTrack(params[0], options);
            }
            catch (RetrofitError e) {
                Log.v(LOG_TAG + "->doInBackground()", e.toString() + " - Error kind: " + e.getKind());
                return null;
            }
            return getTopTracksData(results);
        }

        @Override
        protected void onPostExecute(ArrayList<String[]> result) {
            if (result != null) {
                mTopTracksAdapter.clear();
                mTopTracksData = new ArrayList<>();
                int i = 0;
                for(String[] topTrackData : result) {
                    TopTracksData topTrack = new TopTracksData(topTrackData[0], topTrackData[1], topTrackData[2], topTrackData[3], topTrackData[4], topTrackData[5]);
                    mTopTracksAdapter.add(topTrack);
                    mTopTracksData.add(i, topTrack);
                    i++;
                }

                if (mTopTracksAdapter.getCount() == 0) {
                    Toast.makeText(getActivity(), "Could not find artist's top tracks.", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(getActivity(), "Could not fetch artist - please check your internet connection", Toast.LENGTH_SHORT).show();
            }
        }

        private ArrayList<String[]> getTopTracksData(Tracks topTracksResult) {
            int i = 0;
            ArrayList<String[]> topTracksData = new ArrayList<>();
            for(Track track : topTracksResult.tracks) {
                i++;
                String[] trackData = new String[6]; // Temporary array

                trackData[0] = track.artists.get(0).id; // Save spotify ID
                trackData[1] = track.album.name; // Save artist name
                trackData[2] = track.name; // Save track name

                // Save largest album image possible and largest album image over 200 x 200 px.
                // Image sizes are always in order: large to small
                for (Image image : track.album.images) {
                    if (image.height > 200 || image.width > 200) {
                        trackData[3] = image.url; // Save and overwrite small album image url
                        if (trackData[4] == null) {
                            trackData[4] = image.url; // Save largest album image url
                        }
                    }
                    else if (trackData[3] == null) {
                        trackData[3] = image.url; // Fallback to a small image.
                        if (trackData[4] == null) {
                            trackData[4] = image.url;
                        }
                        break;
                    }
                }
                trackData[5] = track.preview_url; // Save preview url

                topTracksData.add(trackData); // Add temporary array to ArrayList
                Log.v(LOG_TAG, "Spotify Track #" + i + " " + trackData[2]);
            }
            return topTracksData;
        }
    }

    public class TopTracksSearchAdapter extends ArrayAdapter<TopTracksData> {
        public TopTracksSearchAdapter(Context context, ArrayList<TopTracksData> topTracks) {
            super(context, 0, topTracks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TopTracksData topTracksData = getItem(position);
            // Check if existing view is being reused
            if (convertView == null) {
                // Inflate view
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);
            }

            // Find view to populate data
            ImageView ivAlbumImage = (ImageView) convertView.findViewById(R.id.listItemAlbumImageView);
            TextView tvTrackTitle = (TextView) convertView.findViewById(R.id.listItemTrackTitleTextView);
            TextView tvAlbumTitle = (TextView) convertView.findViewById(R.id.listItemTrackAlbumTextView);

            tvAlbumTitle.setText(topTracksData.albumTitle);
            tvTrackTitle.setText(topTracksData.trackTitle);

            if (topTracksData.albumImageUrlSmall != null) {
                Picasso.with(getContext()).load(topTracksData.albumImageUrlSmall).placeholder(R.mipmap.no_image).into(ivAlbumImage);
            } else {
                Picasso.with(getContext()).load(R.mipmap.no_image).into(ivAlbumImage);
            }

            return convertView;
        }
    }


}
