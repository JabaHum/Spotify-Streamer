package no.ahoi.spotify.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

public class TopTracksData implements Parcelable {
    String id, albumTitle, trackTitle, albumImageUrlLarge, albumImageUrlSmall, previewUrl;

    public TopTracksData(String spotifyId, String albumTitle, String trackTitle, String albumImageUrlLarge, String albumImageUrlSmall, String previewUrl) {
        this.id = spotifyId;
        this.albumTitle = albumTitle;
        this.trackTitle = trackTitle;
        this.albumImageUrlLarge = albumImageUrlLarge;
        this.albumImageUrlSmall = albumImageUrlSmall;
        this.previewUrl = previewUrl;
    }

    private TopTracksData(Parcel in) {
        id = in.readString();
        albumTitle = in.readString();
        trackTitle = in.readString();
        albumImageUrlLarge = in.readString();
        albumImageUrlSmall = in.readString();
        previewUrl = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(albumTitle);
        out.writeString(trackTitle);
        out.writeString(albumImageUrlLarge);
        out.writeString(albumImageUrlSmall);
        out.writeString(previewUrl);
    }

    public static final Parcelable.Creator<TopTracksData> CREATOR = new Parcelable.Creator<TopTracksData>() {
        public TopTracksData createFromParcel(Parcel in) {
            return new TopTracksData(in);
        }
        public TopTracksData[] newArray(int size) {
            return new TopTracksData[size];
        }
    };
}
