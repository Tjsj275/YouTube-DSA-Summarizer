package com.dsa.summarizer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Model for Gemini API response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiResponse {
    
    private List<Candidate> candidates;
    private PromptFeedback promptFeedback;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate {
        private Content content;
        private String finishReason;
        private int index;
        private List<SafetyRating> safetyRatings;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private List<Part> parts;
        private String role;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetyRating {
        private String category;
        private String probability;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptFeedback {
        private List<SafetyRating> safetyRatings;
    }
    
    /**
     * Extracts text from the first candidate
     */
    public String extractText() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        
        Candidate candidate = candidates.get(0);
        if (candidate.getContent() == null || candidate.getContent().getParts() == null) {
            return null;
        }
        
        List<Part> parts = candidate.getContent().getParts();
        if (parts.isEmpty()) {
            return null;
        }
        
        return parts.get(0).getText();
    }
}