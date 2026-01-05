package com.dsa.summarizer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Model representing processing status of a video summarization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingStatus {
    
    /**
     * Video ID being processed
     */
    private String videoId;
    
    /**
     * Current processing stage
     */
    private ProcessingStage stage;
    
    /**
     * Progress percentage (0-100)
     */
    private int progress;
    
    /**
     * Current status message
     */
    private String message;
    
    /**
     * Processing start time
     */
    private LocalDateTime startTime;
    
    /**
     * Processing end time
     */
    private LocalDateTime endTime;
    
    /**
     * Whether processing completed successfully
     */
    private boolean completed;
    
    /**
     * Error message if processing failed
     */
    private String error;
    
    /**
     * Number of chunks created
     */
    private int totalChunks;
    
    /**
     * Number of chunks processed
     */
    private int chunksProcessed;
    
    /**
     * Processing stages enum
     */
    public enum ProcessingStage {
        INITIALIZING("Initializing"),
        FETCHING_TRANSCRIPT("Fetching transcript"),
        CLEANING_TRANSCRIPT("Cleaning transcript"),
        CHUNKING("Creating chunks"),
        SUMMARIZING_CHUNKS("Summarizing chunks"),
        MERGING_SUMMARIES("Merging summaries"),
        GENERATING_STRUCTURE("Generating structured output"),
        COMPLETED("Completed"),
        FAILED("Failed");
        
        private final String displayName;
        
        ProcessingStage(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}