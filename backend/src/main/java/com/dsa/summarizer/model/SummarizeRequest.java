package com.dsa.summarizer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request model for video summarization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummarizeRequest {
    
    @NotBlank(message = "YouTube URL is required")
    @Pattern(
        regexp = "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11}).*$",
        message = "Invalid YouTube URL format. Expected format: https://www.youtube.com/watch?v=VIDEO_ID"
    )
    private String youtubeUrl;
}