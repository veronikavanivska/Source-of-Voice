package org.example.sourceofvoice.services;

import org.springframework.stereotype.Service;

@Service
public class TextSimilarityService {

    public double calculateScore(String expectedText, String transcriptText) {
        String expected = normalize(expectedText);
        String actual = normalize(transcriptText);

        if (expected.isBlank() || actual.isBlank()) {
            return 0.0;
        }

        int distance = levenshtein(expected, actual);
        int maxLength = Math.max(expected.length(), actual.length());

        double similarity = 1.0 - ((double) distance / maxLength);
        double score = similarity * 100.0;

        return Math.max(0.0, Math.min(100.0, score));
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text.toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int levenshtein(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];

        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;

            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;

                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + cost
                );
            }

            int[] temporary = previous;
            previous = current;
            current = temporary;
        }

        return previous[b.length()];
    }
}
