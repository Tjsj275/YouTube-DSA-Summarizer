package com.dsa.summarizer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * Model representing structured DSA summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DSASummary {
    
    /**
     * YouTube video ID
     */
    private String videoId;
    
    /**
     * Video title or description
     */
    private String videoTitle;
    
    /**
     * Brief overview of the problem/concept (2-3 sentences)
     */
    private String problemSummary;
    
    /**
     * Step-by-step algorithm explanation
     */
    private String algorithmSteps;
    
    /**
     * Pseudocode implementation
     */
    private String pseudocode;
    
    /**
     * Time complexity analysis with explanation
     */
    private String timeComplexity;
    
    /**
     * Space complexity analysis with explanation
     */
    private String spaceComplexity;
    
    /**
     * Important edge cases to consider
     */
    private String edgeCases;
    
    /**
     * Quick revision notes (5 key points)
     */
    private String revisionNotes;
}