package com.twominuteplays.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;

public class ContributionsTest {

    @Test
    public void testMappyConstructor() {
        Map<String, Object> map = new HashMap<>();
        map.put("contributorName", "Steve");
        map.put("partId", "1");
        map.put("cloned", "false");

        Map<String,Object> clips = new HashMap<>();
        clips.put("1", "ABC");
        clips.put("2", "XYZ");

        map.put("clips", clips);

        Contributions contributions = new Contributions(map);
        assertExpectedResults(contributions);
    }

    @Test
    public void testMappyConstructorPrimitives() {
        Map<String, Object> map = new HashMap<>();
        map.put("contributorName", "Steve");
        map.put("partId", 1L);
        map.put("cloned", false);

        Map<String,Object> clips = new HashMap<>();
        clips.put("1", "ABC");
        clips.put("2", "XYZ");

        map.put("clips", clips);

        Contributions contributions = new Contributions(map);
        assertExpectedResults(contributions);
    }

    @Test
    public void testMappyConstructorNulls() {
        Map<String, Object> map = new HashMap<>();
        map.put("contributorName", null);
        map.put("partId", null);
        map.put("cloned", null);

        Map<String,Object> clips = new HashMap<>();
        clips.put("1", null);
        clips.put("2", null);

        map.put("clips", clips);

        Contributions contributions = new Contributions(map);

        assertNull(contributions.getContributorName());
        assertNull(contributions.getPartId());
        assertFalse(contributions.isCloned());
        assertEquals(2, contributions.getClips().size());
        assertNull(contributions.getClips().get("1"));
    }

    @Test
    public void testMappyConstructorNullClips() {
        Map<String, Object> map = new HashMap<>();
        Contributions contributions = new Contributions(map);

        assertNull(contributions.getContributorName());
        assertNull(contributions.getPartId());
        assertFalse(contributions.isCloned());
        assertEquals(0, contributions.getClips().size());
        assertNull(contributions.getClips().get("1"));
    }

    private void assertExpectedResults(Contributions contributions) {
        assertEquals("Steve", contributions.getContributorName());
        assertEquals("1", contributions.getPartId());
        assertEquals(false, contributions.isCloned());
        assertEquals(2, contributions.getClips().size());
        assertEquals("ABC", contributions.getClips().get("1"));
    }

}