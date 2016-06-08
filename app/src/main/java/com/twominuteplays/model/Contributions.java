package com.twominuteplays.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.twominuteplays.exceptions.MappingError;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Contributions implements Parcelable {
    /**
     * For beta2. Add name so you can display it with a movie title.
     */
    private String contributorName;
    private String partId;
    private boolean cloned = false;

    /**
     * Map of clips for this contribution to the movie. Key is Part.id and value is GCS bucket
     * path.
     */
    private Map<String,String> clips;


    public Contributions() {
    }

    public Contributions(Map<String, Object> map) {
        this.contributorName    = mapValue(map.get("contributorName"));
        this.partId             = mapValue(map.get("partId"));
        this.cloned             = Boolean.valueOf(mapValue(map.get("cloned")));
        this.clips              = (Map<String, String>) map.get("clips");
    }

    @Exclude
    public static Contributions fromMap(Object value) throws MappingError {
        if (value != null && value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            if (!map.containsKey("cloned"))
                throw new MappingError("Contributions map must have a cloned property.");
            return new Contributions(map);
        }
        throw new MappingError("Value is not an instance of Map.");
    }

    private String mapValue(Object value) {
        if (value == null)
            return null;
        else
            return value.toString();
    }

    public String getContributorName() {
        return contributorName;
    }

    public void setContributorName(String contributorName) {
        this.contributorName = contributorName;
    }

    public String getPartId() { return this.partId; }

    public void setPartId(String partId) { this.partId = partId; }

    public boolean isCloned() {
        return cloned;
    }

    public void setCloned(boolean cloned) {
        this.cloned = cloned;
    }

    public Map<String,String> getClips() {
        if (clips == null)
            clips = Collections.synchronizedMap(new HashMap<String,String>());
        return clips;
    }

    public void setClips(Map<String,String> clips) {
        this.clips = clips;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.contributorName);
        dest.writeString(this.partId);
        dest.writeInt(this.cloned ? 1 : 0);
        dest.writeInt(this.clips.size());
        for (Map.Entry<String, String> entry : this.clips.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }

    protected Contributions(Parcel in) {
        this.contributorName = in.readString();
        this.partId = in.readString();
        this.cloned = (in.readInt()) != 0 ? true : false;
        int clipsSize = in.readInt();
        this.clips = new HashMap<>(clipsSize);
        for (int i = 0; i < clipsSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.clips.put(key, value);
        }
    }

    public static final Parcelable.Creator<Contributions> CREATOR = new Parcelable.Creator<Contributions>() {
        @Override
        public Contributions createFromParcel(Parcel source) {
            return new Contributions(source);
        }

        @Override
        public Contributions[] newArray(int size) {
            return new Contributions[size];
        }
    };
}
