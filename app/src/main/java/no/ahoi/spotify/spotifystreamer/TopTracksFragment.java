package no.ahoi.spotify.spotifystreamer;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksFragment extends Fragment {
    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();
    private ArrayAdapter<TopTracksData> mTopTracksAdapter;

    public TopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        TopTracksData topTracks = new TopTracksData("null", "The Scientist", "A Rush Of Blood To The Head", "http://a5.mzstatic.com/us/r1000/050/Music/y2004/m06/d24/h03/s06.tkwjbvaf.600x600-75.jpg");
        ArrayList<TopTracksData> arrayOfTracks = new ArrayList<>();
        mTopTracksAdapter = new TopTracksSearchAdapter(getActivity(), arrayOfTracks);

        ListView listTracks = (ListView) rootView.findViewById(R.id.listTracks);
        listTracks.setAdapter(mTopTracksAdapter);
        //mTopTracksAdapter.addAll(topTracks);
        //mTopTracksAdapter.add(topTracks);

        if (this.getArguments() != null) {
            Bundle bundle = this.getArguments();
            String[] artistData = bundle.getStringArray("artistData");
            FetchTopTracksTask topTracksTask = new FetchTopTracksTask();
            topTracksTask.execute(artistData[0]);
        } else {
            Log.v(LOG_TAG, " Could not fetch arguments (spotify ID) from activity");
        }

        return rootView;
    }

    public class FetchTopTracksTask extends AsyncTask<String, Void, ArrayList<String[]>> {
        private final String LOG_TAG = FetchTopTracksTask.class.getSimpleName();

        @Override
        protected ArrayList<String[]> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            Map<String, Object> options = new HashMap<>();
            options.put(spotify.COUNTRY, Locale.getDefault().getCountry());
            Tracks results = spotify.getArtistTopTrack(params[0], options);

            return getTopTracksData(results);
        }

        @Override
        protected void onPostExecute(ArrayList<String[]> result) {
            if (result != null) {
                mTopTracksAdapter.clear();
                for(String[] topTrackData : result) {
                    TopTracksData topTrack = new TopTracksData(topTrackData[0], topTrackData[1], topTrackData[2], topTrackData[3]);
                    mTopTracksAdapter.add(topTrack);
                }

                if (mTopTracksAdapter.getCount() == 0) {
                    Toast.makeText(getActivity(), "Could not find artist.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private ArrayList<String[]> getTopTracksData(Tracks topTracksResult) {
            int i = 0;
            ArrayList<String[]> topTracksData = new ArrayList<String[]>();
            for(Track track : topTracksResult.tracks) {
                String[] trackData = new String[4]; // Temporary array

                trackData[0] = track.artists.get(0).id; // Save spotify ID
                trackData[1] = track.album.name; // Save artist name
                trackData[2] = track.name; // Save track name
                try {
                    for (Image image : track.album.images) {
                        if (image.height > 200 || image.width > 200) { // image sizes are always in the order: big to small
                            trackData[3] = image.url; // Save image url
                        }
                        else if (trackData[3] == null) {
                            trackData[3] = image.url; // Fallback to smallest image.
                            break;
                        }
                    }
                }
                catch (IndexOutOfBoundsException e) {
                    Log.v("NoArtistImage", e.getMessage());
                }
                if (trackData[3] == null) { // TODO load static empty image
                    trackData[3] = "http://www-rohan.sdsu.edu/~aty/bibliog/latex/scan/figs/gray127gamcor.png";
                }

                topTracksData.add(trackData); // Add temporary array to ArrayList
                Log.v(LOG_TAG, "Spotify Track #" + i + " " + trackData[2]);
                i++;
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

            if (topTracksData.imageUrl != null) {
                Picasso.with(getContext()).load(topTracksData.imageUrl).into(ivAlbumImage);
            } else {
                //TODO load placeholder image?
            }

            return convertView;
        }
    }

    public class TopTracksData {
        public String id, albumTitle, trackTitle, imageUrl;

        public TopTracksData(String spotifyId, String albumTitle, String trackTitle, String imageUrl) {
            this.id = spotifyId;
            this.albumTitle = albumTitle;
            this.trackTitle = trackTitle;
            this.imageUrl = imageUrl;
        }
    }
}
