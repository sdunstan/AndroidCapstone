package com.twominuteplays.model;

import java.util.List;
import java.util.Map;

public class MovieBuilder {
    private String id;
    private String templateId;
    private MovieState movieState;
    private String title;
    private String synopsis;
    private String author;
    private String scriptMarkup;
    private String imageUrl;
    private List<Part> parts;
    private String movieUrl;
    private Long shareId;
    private String contributor;

    public MovieBuilder withJson(Map<String, Object> jsonSnapshot) {
        this.id = (String)jsonSnapshot.get("id");
        this.templateId = (String)jsonSnapshot.get("templateId");
        this.contributor = (String)jsonSnapshot.get("contributor");
        this.movieState = marshalMovieState(jsonSnapshot.get("movieState"));
        this.title = (String)jsonSnapshot.get("title");
        this.synopsis = (String)jsonSnapshot.get("synopsis");
        this.author = (String)jsonSnapshot.get("author");
        this.scriptMarkup = (String)jsonSnapshot.get("scriptMarkup");
        this.imageUrl = (String)jsonSnapshot.get("imageUrl");
        this.movieUrl = (String)jsonSnapshot.get("movieUrl");
        this.shareId = (Long) jsonSnapshot.get("shareId");
        marshalParts(jsonSnapshot);

        return this;
    }

    private void marshalParts(Map<String, Object> jsonSnapshot) {
        for(Object partObject : (Iterable)jsonSnapshot.get("parts")) {
            Map<String,Object> partMap = (Map<String, Object>) partObject;
            addPart(new Part.Builder().withJson(partMap).build());
        }
    }

    private MovieState marshalMovieState(Object movieState) {
        MovieState movieStateEnum = null;
        if (movieState != null) {
            movieStateEnum = MovieState.valueOf((String) movieState);
        }
        return movieStateEnum;
    }

    public MovieBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public MovieBuilder withState(MovieState movieState) {
        this.movieState = movieState;
        return this;
    }

    public MovieBuilder withTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public MovieBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public MovieBuilder withSynopsis(String synopsis) {
        this.synopsis = synopsis;
        return this;
    }

    public MovieBuilder withAuthor(String author) {
        this.author = author;
        return this;
    }

    public MovieBuilder withScriptMarkup(String scriptMarkup) {
        this.scriptMarkup = scriptMarkup;
        return this;
    }

    public MovieBuilder withImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    private List<Part> getParts() {
        if (parts == null) {
            parts = new java.util.ArrayList<>();
        }
        return parts;
    }

    public MovieBuilder addPart(Part part) {
        getParts().add(part);
        return this;
    }

    public MovieBuilder withParts(List<Part> parts) {
        for(Part part : parts) {
            addPart(part);
        }
        return this;
    }

    public Movie build() {
        return new Movie(id, templateId, shareId, contributor, movieState, title, synopsis, author, scriptMarkup, imageUrl, movieUrl, getParts());
    }

    public MovieBuilder withMovieUrl(String movieUrl) {
        this.movieUrl = movieUrl;
        return this;
    }

    public MovieBuilder withShareId(Long shareId) {
        this.shareId = shareId;
        return this;
    }

    public MovieBuilder withContributor(String contributorKey) {
        this.contributor = contributorKey;
        return this;
    }
}
