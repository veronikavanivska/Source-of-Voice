package org.example.sourceofvoice.services;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TextMetricsService {

    private static final double WORDS_PER_MINUTE  = 140.0;

    public String cleanText(String text){
        if(text == null) return "";

        return text
                .replaceAll("\\[[0-9]+]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public int countWords(String text){
        if(text == null || text.isBlank()) return 0;

        return text.trim().split("\\s+").length;
    }

    public int countCharacters(String text){
        if(text == null || text.isBlank()) return 0;

        return text.length();
    }

    public double calculateDifficultyScore(String text){
        if(text == null || text.isBlank()) return 1;

        String[] words = text.trim().split("\\s+");

        int wordCount = words.length;
        int longWords = 0;
        int numbers = 0;
        int punctuation = 0;

        for(String word: words){
            String cleanedWord = word.replaceAll("[^\\p{L}\\p{N}]", "");

            if(cleanedWord.length() >= 10) longWords++;

            if (cleanedWord.matches(".*\\d.*")) numbers++;

            for(char c: text.toCharArray()){
                if (",;:!?()\"".indexOf(c) >= 0) {
                    punctuation++;
                }
            }
        }

        double difficulty = 1.0;

        difficulty += ((double) longWords / Math.max(wordCount, 1)) * 0.60;
        difficulty += ((double) numbers / Math.max(wordCount, 1)) * 0.50;
        difficulty += ((double) punctuation / Math.max(wordCount, 1)) * 0.25;

        return Math.round(difficulty * 100.0) / 100.0;
    }

    public int estimateReadingSeconds(int wordCount) {
        return (int) Math.ceil((wordCount / WORDS_PER_MINUTE) * 60.0);
    }

    public BigDecimal calculateBasePrice(
            int wordCount,
            double difficultyScore,
            BigDecimal baseRatePerWord
    ){
        return  baseRatePerWord
                .multiply(BigDecimal.valueOf(wordCount))
                .multiply(BigDecimal.valueOf(difficultyScore))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public boolean difficultyMatches(
            double difficultyScore,
            Double minDifficultyScore,
            Double maxDifficultyScore
    ) {
        if (minDifficultyScore != null && difficultyScore < minDifficultyScore) {
            return false;
        }

        if (maxDifficultyScore != null && difficultyScore > maxDifficultyScore) {
            return false;
        }

        return true;
    }


}
