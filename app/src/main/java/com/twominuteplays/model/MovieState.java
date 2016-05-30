package com.twominuteplays.model;

import com.twominuteplays.db.FirebaseStuff;

import java.util.UUID;

/**
 * Movie state plus state transitions.
 */
public enum MovieState {
    TEMPLATE,
    SELECTED,
    PART_SELECTED,
    RECORDING_STARTED,
    CONTRIBUTE, // like RECORDING_STARTED except for when I accept a share
    RECORDED,
    CONTRIBUTED, // for when we detect someone has recorded their part
    SINGLE_USER, // for when you choose to record all the parts yourself
    SINGLE_USER_MERGED,
    SHARED,
    SHARE_CLONED,
    DOWNLOADING_OWNER,
    DOWNLOADING_CONTRIBUTOR,
    DOWNLOADED,
    MERGED;


    private Movie copyWithState(final Movie movie) {
        Movie newMovie =  (new MovieBuilder())
                .withId(movie.getId())
                .withTemplateId(movie.getTemplateId())
                .withShareId(movie.getShareId())
                .withContributor(movie.getContributor())
                .withState(this)
                .withTitle(movie.getTitle())
                .withSynopsis(movie.getSynopsis())
                .withAuthor(movie.getAuthor())
                .withScriptMarkup(movie.getScriptMarkup())
                .withImageUrl(movie.getImageUrl())
                .withMovieUrl(movie.getMovieUrl())
                .withParts(movie.getParts())
                .build();
        return newMovie.save(); // TODO: don't save. Find usages and explicitly add save.
    }

    public Movie startRecording(Movie movie) {
        if (CONTRIBUTE == movie.state)
            return movie;
        if (PART_SELECTED != movie.state)
            throw new IllegalStateException("Movie state must be PART SELECTED or CONTRIBUTE to start recording.");
        return RECORDING_STARTED.copyWithState(movie);
    }

    public Movie merged(String moviePath, Movie movie) {
        MovieBuilder builder = new MovieBuilder();
        builder.withId(movie.getId())
                .withTemplateId(movie.getTemplateId())
                .withShareId(movie.getShareId())
                .withContributor(movie.getContributor())
                .withState(MERGED)
                .withTitle(movie.getTitle())
                .withSynopsis(movie.getSynopsis())
                .withAuthor(movie.getAuthor())
                .withScriptMarkup(movie.getScriptMarkup())
                .withParts(movie.getParts())
                .withMovieUrl(moviePath)
                .withImageUrl(movie.getImageUrl());
        return builder.build();
    }

    public Movie selectPart(final Movie movie, final Part thePart) {
        if (!(MovieState.SELECTED == movie.getState() || MovieState.RECORDING_STARTED == movie.getState() || MovieState.CONTRIBUTE == movie.getState()))
            throw new IllegalStateException("Movie state must be SELECTED, RECORDING STARTED, or RECORDED to select part. Is " + movie.getState());
        Part selectedPart = (new Part.Builder())
                .withActorUid(FirebaseStuff.getUid())
                .withDescription(thePart.getDescription())
                .withCharacterName(thePart.getCharacterName())
                .withId(thePart.getId())
                .withLines(thePart.getLines())
                .build();
        MovieBuilder builder = new MovieBuilder();
        builder.withId(movie.getId())
                .withTemplateId(movie.getTemplateId())
                .withShareId(movie.getShareId())
                .withContributor(movie.getContributor())
                .withState(movie.state == CONTRIBUTE ? CONTRIBUTE : MovieState.PART_SELECTED)
                .withTitle(movie.getTitle())
                .withSynopsis(movie.getSynopsis())
                .withAuthor(movie.getAuthor())
                .withScriptMarkup(movie.getScriptMarkup())
                .withMovieUrl(movie.getMovieUrl())
                .withImageUrl(movie.getImageUrl());
        for (Part part : movie.getParts()) {
            if (part.getId().equals(selectedPart.getId()))
                builder.addPart(selectedPart);
            else
                builder.addPart(part);
        }
        return builder.build();
    }

    public Movie downloaded(Movie movie) {
        return DOWNLOADED.copyWithState(movie);
    }

    public Movie downloading(Movie movie) {
        if (!(SHARE_CLONED == movie.getState() || CONTRIBUTED == movie.getState()))
            throw new IllegalStateException("Movie state must be SHARE CLONED or CONTRIBUTED to download the movie.");
        return (SHARE_CLONED == movie.getState() ? DOWNLOADING_OWNER : DOWNLOADING_CONTRIBUTOR).copyWithState(movie);
    }

    public Movie revertDownloading(Movie movie) {
        if (!(DOWNLOADING_OWNER == movie.getState() || DOWNLOADING_CONTRIBUTOR == movie.getState()))
            throw new IllegalStateException("Movie state must be DOWNLOADING OWNER or CONTRIBUTOR to revert downloading of the movie.");
        return (DOWNLOADING_OWNER == movie.getState() ? SHARE_CLONED : CONTRIBUTED).copyWithState(movie);
    }

    public boolean isRecordable() {
        return CONTRIBUTE == this ||
                RECORDING_STARTED == this ||
                SELECTED == this ||
                PART_SELECTED == this;
    }

    public Movie contributed(Movie movie) {
        if (CONTRIBUTE != movie.getState())
            throw new IllegalStateException("Movie state must be CONTRIBUTE to mark the movie as contributed.");
        return CONTRIBUTED.copyWithState(movie);
    }

    public Movie recorded(Movie movie) {
        if (RECORDING_STARTED != movie.getState())
            throw new IllegalStateException("Movie state must be RECORDING STARTED to mark the movie as recorded.");
        return RECORDED.copyWithState(movie);
    }

    public Movie select(final Movie movie) {
        if (TEMPLATE != movie.getState())
            throw new IllegalStateException("Movie state must be TEMPLATE to select.");
        Movie selectedMovie = (new MovieBuilder())
                .withId(UUID.randomUUID().toString())
                .withTemplateId(movie.getId())
                .withContributor(movie.getContributor())
                .withShareId(movie.getShareId())
                .withState(SELECTED)
                .withTitle(movie.getTitle())
                .withSynopsis(movie.getSynopsis())
                .withAuthor(movie.getAuthor())
                .withScriptMarkup(movie.getScriptMarkup())
                .withImageUrl(movie.getImageUrl())
                .withMovieUrl(movie.getMovieUrl())
                .withParts(movie.getParts())
                .build();
        return selectedMovie.save();
    }

    /**
     * TODO: this should be like PART-SELECTED
     * The movie passed in has the
     */
    public Movie contribute(final Movie movie, final Share share) {
        MovieBuilder builder = new MovieBuilder();
        builder.withId(UUID.randomUUID().toString())
                .withTemplateId(movie.getTemplateId())
                .withContributor(movie.getContributor())
                .withShareId(movie.getShareId())
                .withState(CONTRIBUTE)
                .withTitle(movie.getTitle())
                .withSynopsis(movie.getSynopsis())
                .withAuthor(movie.getAuthor())
                .withScriptMarkup(movie.getScriptMarkup())
                .withParts(movie.clonePartsForContribute(share.getOwnersPartId()))
                .withMovieUrl(null)
                .withImageUrl(null);
        return builder.build().save();
    }

    public Movie shareClone(final String contributorKey, final Movie movie) {
        MovieBuilder builder = new MovieBuilder();
        builder.withId(UUID.randomUUID().toString())
                .withTemplateId(movie.getTemplateId())
                .withShareId(movie.getShareId())
                .withContributor(contributorKey)
                .withState(SHARE_CLONED)
                .withTitle(movie.getTitle())
                .withSynopsis(movie.getSynopsis())
                .withAuthor(movie.getAuthor())
                .withScriptMarkup(movie.getScriptMarkup())
                .withParts(movie.getParts())
                .withMovieUrl(null)
                .withImageUrl(movie.getImageUrl());
        return builder.build().save();
    }

    public Movie share(Long shareId, Movie movie) {
        if (RECORDED != movie.getState())
            throw new IllegalStateException("Movie state must be RECORDED to share the movie.");
        // Also, this movie must have only one part recorded (do a check for this?)

        MovieBuilder builder = new MovieBuilder();
        builder.withId(movie.getId())
                .withTemplateId(movie.getTemplateId())
                .withShareId(shareId)
                .withContributor(movie.getContributor())
                .withState(SHARED)
                .withTitle(movie.getTitle())
                .withSynopsis(movie.getSynopsis())
                .withAuthor(movie.getAuthor())
                .withScriptMarkup(movie.getScriptMarkup())
                .withParts(movie.getParts())
                .withMovieUrl(movie.getMovieUrl())
                .withImageUrl(movie.getImageUrl());
        return builder.build().save();
    }
}
