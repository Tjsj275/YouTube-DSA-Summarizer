package com.dsa.summarizer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.List;

/**
 * Model for Gemini API request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeminiRequest {
    
    private List<Content> contents;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Content {
        private List<Part> parts;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Part {
        private String text;
    }
    
    /**
     * Creates a simple request with single text prompt
     */
    public static GeminiRequest createSimpleRequest(String prompt) {
        Part part = Part.builder()
                .text(prompt)
                .build();
        
        Content content = Content.builder()
                .parts(List.of(part))
                .build();
        
        return GeminiRequest.builder()
                .contents(List.of(content))
                .build();
    }
}