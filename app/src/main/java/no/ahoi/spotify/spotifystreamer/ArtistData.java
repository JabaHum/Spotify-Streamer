package no.ahoi.spotify.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

public class ArtistData implements Parcelable{
    String spotifyId, name, imageUrl;

    public ArtistData(String spotifyId, String name, String imageUrl) {
        this.spotifyId = spotifyId;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    private ArtistData(Parcel in) {
        spotifyId = in.readString();
        name = in.readString();
        imageUrl = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(spotifyId);
        out.writeString(name);
        out.writeString(imageUrl);
    }

    public static final Parcelable.Creator<ArtistData> CREATOR = new Parcelable.Creator<ArtistData>() {
        public ArtistData createFromParcel(Parcel in) {
            return new ArtistData(in);
        }
        public ArtistData[] newArray(int size) {
            return new ArtistData[size];
        }
    };
}
