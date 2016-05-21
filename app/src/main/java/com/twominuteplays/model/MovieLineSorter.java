package com.twominuteplays.model;

import java.util.Comparator;

public class MovieLineSorter implements Comparator<Line> {
    @Override
    public int compare(Line line, Line line2) {
        return line.getSortOrder().compareTo(line2.getSortOrder());
    }
}
