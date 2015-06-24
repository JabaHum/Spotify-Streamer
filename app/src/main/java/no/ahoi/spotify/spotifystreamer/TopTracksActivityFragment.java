package no.ahoi.spotify.spotifystreamer;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {
    private static final String LOG_TAG = TopTracksActivityFragment.class.getSimpleName();
    private ArrayAdapter<TopTracksData> mSpotifyTopTracksAdapter;

    public TopTracksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        TopTracksData topTracks = new TopTracksData("null", "The Scientist", "A Rush Of Blood To The Head", "http://a5.mzstatic.com/us/r1000/050/Music/y2004/m06/d24/h03/s06.tkwjbvaf.600x600-75.jpg");
        ArrayList<TopTracksData> arrayOfTracks = new ArrayList<>();
        mSpotifyTopTracksAdapter = new TopTracksSearchAdapter(getActivity(), arrayOfTracks);

        ListView listTracks = (ListView) rootView.findViewById(R.id.listTracks);
        listTracks.setAdapter(mSpotifyTopTracksAdapter);
        mSpotifyTopTracksAdapter.addAll(topTracks);
        mSpotifyTopTracksAdapter.add(topTracks);

        return rootView;
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
