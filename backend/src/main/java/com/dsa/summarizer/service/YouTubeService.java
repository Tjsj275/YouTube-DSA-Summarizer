package com.dsa.summarizer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class YouTubeService {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeService.class);

    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile(
        "(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})"
    );

    /**
     * Extracts video ID from YouTube URL
     */
    public String extractVideoId(String youtubeUrl) {
        logger.debug("Extracting video ID from URL: {}", youtubeUrl);

        Matcher matcher = VIDEO_ID_PATTERN.matcher(youtubeUrl);
        if (matcher.find()) {
            String videoId = matcher.group(1);
            logger.debug("Extracted video ID: {}", videoId);
            return videoId;
        }

        throw new IllegalArgumentException("Invalid YouTube URL format");
    }

    /**
     * Validates YouTube URL format
     */
    public boolean isValidYouTubeUrl(String url) {
        return VIDEO_ID_PATTERN.matcher(url).find();
    }
}