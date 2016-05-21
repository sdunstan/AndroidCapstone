package com.twominuteplays.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MovieTestDataFactory {

    public static Movie[] factoryMyMovies() {

        Movie scripts[] = new Movie[2];

        scripts[0] = movieFactory("5", "Spork! Spork! Spork!", "Raucous fun and musical amazement.");
        scripts[1] = movieFactory("6", "Jazz Hands", "Timmy thought his grandad was just a plumber until one day he discovers his roots in vaudeville.");

        return scripts;

    }

    public static void printJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(factoryAllMovies()));
    }

    public static Movie[] factoryAllMovies() {
        Movie[] scripts = new Movie[6];

        scripts[0] = movieFactory("1", "A Time for Clowns", "Some clowns go fishing. What happens?");
        scripts[1] = movieFactory("2", "Frahad's Clock", "A touching tale of the human spirit.");
        scripts[2] = movieFactory("3", "The Postman Only Rings A Few Times", "A steamy thriller about love -- or is it obsession?");
        scripts[3] = movieFactory("4", "The Oddest Couple", "Smell this!");
        scripts[4] = movieFactory("5", "Spork! Spork! Spork!", "Raucous fun and musical amazement.");
        scripts[5] = movieFactory("6", "Jazz Hands", "Timmy thought his grandad was just a plumber until one day he discovers his roots in vaudeville.");

        return scripts;
    }


    private static Movie movieFactory(String id, String title, String synopsis) {

        // Makes all movies THE BLAND WITCH PROJECT script.
        Part.Builder patBuilder = new Part.Builder();
        Part pat = patBuilder.withId("1")
                .withCharacterName("PAT")
                .withDescription("a camper convinced a witch is afoot")
                .addLine(makeLine("1", 0, "Did you hear that?"))
                .addLine(makeLine("2", 1, "No, that creepy cackling sound. It came from over there."))
                .addLine(makeLine("3", 2, "I distinctly heard mouth breathing. Kind of like (PANTS LINE A DOG)"))
                .addLine(makeLine("4", 3, "Yes! That's it. (PAUSES DRAMATICALLY) I'm so scared."))
                .addLine(makeLine("5", 4, "Thank you for that. I hope the witch eats you."))
                .build();

        Part.Builder samBuilder = new Part.Builder();
        Part sam = samBuilder.withId("2")
                .withCharacterName("SAM")
                .withDescription("a nonbeliever")
                .addLine(makeLine("6", 0, "Sorry, that was me. I just had cabbage."))
                .addLine(makeLine("7", 1, "I didn't hear anything."))
                .addLine(makeLine("8", 2, "Oh, sorry, yep. I hear it now. It is kind of like (PANTS LIKE A DOG)"))
                .addLine(makeLine("9", 3, "(WHISPER SLOWLY) Me too. (SAM makes FLATULENT SOUND)"))
                .build();

        Movie.Builder movieBuilder = new Movie.Builder();
        return movieBuilder.withAuthor("Steve")
                .withId(id)
                .withScriptMarkup("<html><body><p class=\"c0 c2\"><span>THE BLAND WITCH PROJECT</span></p><p class=\"c0 c1\"><span></span></p><p class=\"c0\"><span>SETTING: a darkened room or a desolate outdoor area at night</span></p><p class=\"c0 c1\"><span></span></p><p class=\"c0\"><span>CHARACTERS:</span></p><p class=\"c0\"><span>PAT, a camper convinced a witch is afoot</span></p><p class=\"c0\"><span>SAM, a nonbeliever</span></p><p class=\"c0 c1\"><span></span></p><p class=\"c0 c2\"><span>PAT</span></p><p class=\"c0\"><span>Did you hear that?</span></p><p class=\"c0 c1\"><span></span></p><p class=\"c0 c2\"><span>SAM</span></p><p class=\"c0\"><span>Sorry, that was me. I just had cabbage.</span></p><p class=\"c0 c1\"><span></span></p><p class=\"c0 c2\"><span>PAT</span></p><p class=\"c0\"><span>No, that creepy cackling sound. It came from over there.</span></p><p class=\"c0 c1\"><span></span></p><p class=\"c0 c2\"><span>SAM</span></p><p class=\"c0\"><span>I didn&rsquo;t hear anything.</span></p><p class=\"c0 c1\"><span></span></p><p class=\"c0 c2\"><span>PAT</span></p><p class=\"c0\"><span>I distinctly heard mouth breathing. Kind of like (PANTS LIKE A DOG)</span></p><p class=\"c0 c1\"><span></span></p><p class=\"c0 c2\"><span>SAM</span></p><p class=\"c0\"><span>Oh, sorry, yep. I hear it now. It is kind of like (PANTS LIKE A DOG)</span></p><p class=\"c0 c1\"><span></span></p><p class=\"c0 c2\"><span>PAT</span></p><p class=\"c0\"><span>Yes! That&rsquo;s it. (PAUSES DRAMATICALLY) I&rsquo;m so scared.</span></p><p class=\"c0 c1\"><span></span></p><p class=\"c0 c2\"><span>SAM</span></p><p class=\"c0 c2\"><span>(whispering, slowly) </span></p><p class=\"c0\"><span>Me too. (SAM makes FLATULENT SOUND)</span></p><p class=\"c0 c1\"><span></span></p><p class=\"c0 c2\"><span>PAT</span></p><p class=\"c0\"><span>Thank you for that. I hope the witch eats you.</span></p><p class=\"c0 c1\"><span></span></p></body></html>")
                .withSynopsis(synopsis)
                .withTitle(title)
                .addPart(pat)
                .addPart(sam)
                .build();
    }

    private static Line makeLine(String id, Integer sortOrder, String lineText) {
        Line.Builder lineBuilder = new Line.Builder();
        return lineBuilder.withId(id)
                .withSortOrder(sortOrder)
                .withLine(lineText)
                .build();
    }


}
