package com.twominuteplays.model;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.database.Exclude;
import com.twominuteplays.db.FirebaseStuff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Movie implements Parcelable {
    public static final String MOVIE_SHARE = "com.twominuteplays.model.MOVIE_SHARE";
    public static final String MOVIE_EXTRA = "movie";

    private final String id;
    private final String templateId;
    private final Long shareId;
    private final String contributor;
    public final MovieState state;
    private final String title;
    private final String synopsis;
    private final String author;
    private final String scriptMarkup;
    private final String imageUrl;
    private final String movieUrl;
    private final List<Part> parts;

    public static final Parcelable.Creator<Movie> CREATOR =
            new Parcelable.Creator<Movie>() {

                @Override
                public Movie createFromParcel(Parcel parcel) {
                    return new Movie(parcel);
                }

                @Override
                public Movie[] newArray(int size) {
                    return new Movie[size];
                }
            };

    Movie(String id,
                  String templateId,
                  Long shareId,
                  String contributor,
                  MovieState movieState,
                  String title,
                  String synopsis,
                  String author,
                  String scriptMarkup,
                  String imageUrl,
                  String movieUrl,
                  List<Part> parts) {
        this.id = id;
        this.templateId = templateId;
        this.shareId = shareId == null ? 0 : shareId;
        this.contributor = contributor;
        this.state = movieState == null ? MovieState.TEMPLATE : movieState;
        this.title = title;
        this.synopsis = synopsis;
        this.author = author;
        this.scriptMarkup = scriptMarkup;
        this.imageUrl = imageUrl;
        this.movieUrl = movieUrl;
        if (parts != null) {
            this.parts = Collections.unmodifiableList(parts);
        }
        else {
            this.parts = null;
        }
    }

    private Movie(Parcel in) {
        this.id = in.readString();
        this.templateId = in.readString();
        this.shareId = in.readLong();
        this.contributor = in.readString();
        String movieStateString = in.readString();
        this.state = movieStateString == null ? MovieState.TEMPLATE : MovieState.valueOf(movieStateString);
        this.title = in.readString();
        this.synopsis = in.readString();
        this.author = in.readString();
        this.scriptMarkup = in.readString();
        this.imageUrl = in.readString();
        this.movieUrl = in.readString();
        List<Part> partsPrototype = new ArrayList<>();
        in.readList(partsPrototype, this.getClass().getClassLoader());
        this.parts = Collections.unmodifiableList(partsPrototype);
    }

    public String getId() {
        return id;
    }

    public String getTemplateId() { return templateId; }

    public String getContributor() { return contributor; }

    @Exclude
    public MovieState getState() { return state; }

    public String getMovieState() { return (state != null) ? state.toString() : null; }

    public String getTitle() {
        return title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getAuthor() {
        return author;
    }

    public String getScriptMarkup() {
        return scriptMarkup;
    }

    public List<Part> getParts() {
        return parts;
    }

    public String getImageUrl() { return imageUrl; }

    public String getMovieUrl() { return movieUrl; }

    public Long getShareId() {
        return shareId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(templateId);
        parcel.writeLong(shareId);
        parcel.writeString(contributor);
        parcel.writeString(state.toString());
        parcel.writeString(title);
        parcel.writeString(synopsis);
        parcel.writeString(author);
        parcel.writeString(scriptMarkup);
        parcel.writeString(imageUrl);
        parcel.writeString(movieUrl);
        parcel.writeList(parts);
    }

    @Exclude
    public Movie save() {
        FirebaseStuff.saveMovie(this);
        return this;
    }

    @Exclude
    public Movie addVideo(String partId, String lineId, String videoUrl) {
        if (!(MovieState.RECORDING_STARTED == state ||
                MovieState.CONTRIBUTE == state ||
                MovieState.DOWNLOADING_CONTRIBUTOR == state ||
                MovieState.DOWNLOADING_OWNER == state))
            throw new IllegalStateException("Movie state must be RECORDING STARTED or DOWNLOADING to add video.");
        MovieBuilder builder = new MovieBuilder();
        builder.withId(getId())
                .withTemplateId(getTemplateId())
                .withShareId(getShareId())
                .withContributor(getContributor())
                .withState(getState())
                .withTitle(getTitle())
                .withSynopsis(getSynopsis())
                .withAuthor(getAuthor())
                .withScriptMarkup(getScriptMarkup())
                .withMovieUrl(getMovieUrl())
                .withImageUrl(getImageUrl());
        for (Part part : getParts()) {
            if (part.getId().equals(partId))
                builder.addPart(part.addVideo(lineId, videoUrl));
            else
                builder.addPart(part);
        }
        return builder.build();
    }

    @Exclude
    public Movie addImageUrl(String absolutePath) {
        MovieBuilder builder = new MovieBuilder();
        builder.withId(getId())
                .withTemplateId(getTemplateId())
                .withShareId(getShareId())
                .withContributor(getContributor())
                .withState(getState())
                .withTitle(getTitle())
                .withSynopsis(getSynopsis())
                .withAuthor(getAuthor())
                .withScriptMarkup(getScriptMarkup())
                .withParts(getParts())
                .withMovieUrl(getMovieUrl())
                .withImageUrl(absolutePath);
        return builder.build();
    }

    @Exclude
    List<Part> clonePartsForContribute(@NonNull final String ownerPartId) {
        List<Part> cleanParts = new ArrayList<>();
        for(final Part part : getParts()) {
            if (ownerPartId.equals(part.getId())) {
                cleanParts.add(part.cloneOwnerPart());
            }
            else {
                cleanParts.add(part.clonePartForContribute());
            }
        }
        return cleanParts;
    }


    @Exclude
    public String deepLink() {
        return "http://2mp.tv/play/" + getShareId();
    }

    public List<Line> assembleLines() {
        List<Line> lines = new java.util.ArrayList<>();
        for(Part part : getParts()) {
            lines.addAll(part.getLines());
        }
        Collections.sort(lines, new MovieLineSorter());
        return lines;
    }

    @Exclude
    public void broadcastShare(Context context) {
        Intent shareIntent = new Intent(MOVIE_SHARE).putExtra(MOVIE_EXTRA, this);
        LocalBroadcastManager.getInstance(context).sendBroadcast(shareIntent);
    }

    @Exclude
    public boolean isRecorded() {
        boolean recorded = true;
        for (Part part : getParts()) {
            recorded = part.isRecorded();
            if (!recorded) break;
        }
        return recorded;
    }

    @Exclude
    public String findPartIdOpposite(Part ownersPart) {
        if (parts == null || parts.size() != 2)
            throw new IllegalStateException("There must be two parts to determine opposite part.");
        if(ownersPart.getId().equals(parts.get(0).getId()))
            return parts.get(1).getId();
        else
            return parts.get(0).getId();
    }

    @Exclude
    public Part findExclusiveRecordedPart() {
        if (parts == null || parts.size() != 2)
            throw new IllegalStateException("There must be two parts to determine opposite part.");
        Part part1 = parts.get(0);
        Part part2 = parts.get(1);

        if(part1.isRecorded() && !part2.isRecorded())
            return part1;
        if(!part1.isRecorded() && part2.isRecorded())
            return part2;

        return null;
    }

    @Exclude
    public Part findPart(String partId) {
        for(Part part : parts) {
            if(part.getId().equals(partId))
                return part;
        }
        return null;
    }


}
