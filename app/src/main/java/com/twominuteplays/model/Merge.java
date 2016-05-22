package com.twominuteplays.model;

import java.util.HashMap;
import java.util.Map;

public class Merge {
    private String owner;
    private String movieId;
    private Map<String,Part> contributors;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public Map<String, Part> getContributors() {
        if (contributors == null) {
            contributors = new HashMap<>();
        }
        return contributors;
    }

    public void setContributors(Map<String, Part> contributors) {
        this.contributors = contributors;
    }
}
