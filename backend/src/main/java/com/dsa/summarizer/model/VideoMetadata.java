package com.dsa.summarizer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * Model representing YouTube video metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoMetadata {
    
    /**
     * Video ID
     */
    private String videoId;
    
    /**
     * Video title
     */
    private String title;
    
    /**
     * Video description
     */
    private String description;
    
    /**
     * Channel name
     */
    private String channelName;
    
    /**
     * Video duration in seconds
     */
    private long duration;
    
    /**
     * Number of views
     */
    private long viewCount;
    
    /**
     * Upload date
     */
    private String uploadDate;
    
    /**
     * Whether captions/subtitles are available
     */
    private boolean captionsAvailable;
    
    /**
     * Caption language
     */
    private String captionLanguage;
}