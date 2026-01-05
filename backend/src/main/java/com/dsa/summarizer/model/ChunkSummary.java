package com.dsa.summarizer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * Model representing a summarized chunk
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChunkSummary {
    
    /**
     * Chunk index/number
     */
    private int chunkIndex;
    
    /**
     * Original chunk text
     */
    private String originalText;
    
    /**
     * Summarized text
     */
    private String summary;
    
    /**
     * Word count of original chunk
     */
    private int wordCount;
    
    /**
     * Processing timestamp
     */
    private long timestamp;
}