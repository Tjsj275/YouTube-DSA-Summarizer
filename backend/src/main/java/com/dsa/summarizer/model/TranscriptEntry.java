package com.dsa.summarizer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * Model representing a single transcript entry with timing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranscriptEntry {
    
    /**
     * Start time in seconds
     */
    private double start;
    
    /**
     * Duration in seconds
     */
    private double duration;
    
    /**
     * Transcript text content
     */
    private String text;
    
    /**
     * Calculates end time
     */
    public double getEndTime() {
        return start + duration;
    }
}