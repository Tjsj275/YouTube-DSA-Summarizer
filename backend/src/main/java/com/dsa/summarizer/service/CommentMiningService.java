package com.dsa.summarizer.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentMiningService {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentMiningService.class);
    
    @Value("${youtube.api.key}")
    private String youtubeApiKey;
    
    @Value("${youtube.data.api.url}")
    private String youtubeDataApiUrl;
    
    @Value("${comment.mining.max.comments:100}")
    private int maxComments;
    
    @Value("${comment.mining.max.replies:50}")
    private int maxReplies;
    
    @Value("${comment.mining.min.comment.length:20}")
    private int minCommentLength;
    
    @Value("${comment.mining.relevance.threshold:0.3}")
    private double relevanceThreshold;
    
    @Value("${dsa.keywords}")
    private String dsaKeywordsString;
    
    private Set<String> dsaKeywords;
    
    /**
     * Builds a pseudo-transcript from video metadata and comments
     */
    public String buildPseudoTranscript(String videoId) throws Exception {
        logger.info("Building pseudo-transcript from comments for video: {}", videoId);
        
        // Initialize DSA keywords
        initializeDsaKeywords();
        
        StringBuilder pseudoTranscript = new StringBuilder();
        
        try {
            // Step 1: Fetch video metadata (title + description)
            logger.info("Step 1: Fetching video metadata");
            VideoMetadata metadata = fetchVideoMetadata(videoId);
            
            pseudoTranscript.append("VIDEO TITLE: ").append(metadata.getTitle()).append("\n\n");
            pseudoTranscript.append("VIDEO DESCRIPTION: ").append(metadata.getDescription()).append("\n\n");
            
            // Step 2: Fetch and filter comments
            logger.info("Step 2: Fetching comments");
            List<Comment> comments = fetchComments(videoId);
            logger.info("Fetched {} total comments", comments.size());
            
            // Step 3: Filter relevant comments
            logger.info("Step 3: Filtering relevant DSA comments");
            List<Comment> relevantComments = filterRelevantComments(comments);
            logger.info("Filtered to {} relevant comments", relevantComments.size());
            
            if (relevantComments.isEmpty()) {
                logger.warn("No relevant DSA comments found");
                return null;
            }
            
            // Step 4: Build pseudo-transcript from comments
            pseudoTranscript.append("COMMUNITY INSIGHTS:\n\n");
            
            for (Comment comment : relevantComments) {
                pseudoTranscript.append(comment.getText()).append("\n");
                
                // Add replies if available
                if (!comment.getReplies().isEmpty()) {
                    for (String reply : comment.getReplies()) {
                        pseudoTranscript.append("  → ").append(reply).append("\n");
                    }
                }
                pseudoTranscript.append("\n");
            }
            
            String result = pseudoTranscript.toString();
            logger.info("Pseudo-transcript built successfully. Length: {} characters", result.length());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to build pseudo-transcript: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to mine comments: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fetches video metadata (title and description)
     */
    private VideoMetadata fetchVideoMetadata(String videoId) throws Exception {
        String urlString = String.format(
            "%s/videos?part=snippet&id=%s&key=%s",
            youtubeDataApiUrl, videoId, youtubeApiKey
        );
        
        String response = fetchUrl(urlString);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        JsonArray items = json.getAsJsonArray("items");
        
        if (items.size() == 0) {
            throw new RuntimeException("Video not found");
        }
        
        JsonObject snippet = items.get(0).getAsJsonObject().getAsJsonObject("snippet");
        String title = snippet.get("title").getAsString();
        String description = snippet.has("description") ? 
            snippet.get("description").getAsString() : "";
        
        logger.debug("Video title: {}", title);
        logger.debug("Description length: {} characters", description.length());
        
        return new VideoMetadata(title, description);
    }
    
    /**
     * Fetches comments and replies for a video
     */
    private List<Comment> fetchComments(String videoId) throws Exception {
        List<Comment> comments = new ArrayList<>();
        String pageToken = null;
        int fetchedCount = 0;
        
        do {
            String urlString = String.format(
                "%s/commentThreads?part=snippet,replies&videoId=%s&maxResults=100&order=relevance&key=%s",
                youtubeDataApiUrl, videoId, youtubeApiKey
            );
            
            if (pageToken != null) {
                urlString += "&pageToken=" + URLEncoder.encode(pageToken, StandardCharsets.UTF_8);
            }
            
            String response = fetchUrl(urlString);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("items");
            
            for (int i = 0; i < items.size() && fetchedCount < maxComments; i++) {
                JsonObject item = items.get(i).getAsJsonObject();
                JsonObject topLevelComment = item.getAsJsonObject("snippet")
                    .getAsJsonObject("topLevelComment")
                    .getAsJsonObject("snippet");
                
                String commentText = topLevelComment.get("textDisplay").getAsString();
                commentText = cleanHtmlTags(commentText);
                
                // Get replies
                List<String> replies = new ArrayList<>();
                if (item.has("replies")) {
                    JsonArray replyArray = item.getAsJsonObject("replies")
                        .getAsJsonArray("comments");
                    
                    for (int j = 0; j < replyArray.size() && j < maxReplies; j++) {
                        JsonObject reply = replyArray.get(j).getAsJsonObject()
                            .getAsJsonObject("snippet");
                        String replyText = reply.get("textDisplay").getAsString();
                        replyText = cleanHtmlTags(replyText);
                        
                        if (replyText.length() >= minCommentLength) {
                            replies.add(replyText);
                        }
                    }
                }
                
                comments.add(new Comment(commentText, replies));
                fetchedCount++;
            }
            
            pageToken = json.has("nextPageToken") ? 
                json.get("nextPageToken").getAsString() : null;
                
        } while (pageToken != null && fetchedCount < maxComments);
        
        return comments;
    }
    
    /**
     * Filters comments based on DSA keyword relevance
     */
    private List<Comment> filterRelevantComments(List<Comment> comments) {
        return comments.stream()
            .filter(comment -> {
                // Check minimum length
                if (comment.getText().length() < minCommentLength) {
                    return false;
                }
                
                // Check if it's spam (too many links, emojis, etc.)
                if (isSpam(comment.getText())) {
                    return false;
                }
                
                // Calculate relevance score
                double relevance = calculateRelevanceScore(comment.getText());
                return relevance >= relevanceThreshold;
            })
            .sorted((c1, c2) -> {
                // Sort by relevance score (descending)
                double score1 = calculateRelevanceScore(c1.getText());
                double score2 = calculateRelevanceScore(c2.getText());
                return Double.compare(score2, score1);
            })
            .limit(50) // Top 50 most relevant comments
            .collect(Collectors.toList());
    }
    
    /**
     * Calculates relevance score based on DSA keywords
     */
    private double calculateRelevanceScore(String text) {
        String lowerText = text.toLowerCase();
        int keywordMatches = 0;
        int totalWords = text.split("\\s+").length;
        
        for (String keyword : dsaKeywords) {
            if (lowerText.contains(keyword.toLowerCase())) {
                keywordMatches++;
            }
        }
        
        // Score = (matched keywords / total keywords) * weight
        double score = (double) keywordMatches / dsaKeywords.size();
        
        // Boost score if it contains complexity notation
        if (lowerText.contains("o(") || lowerText.contains("complexity")) {
            score *= 1.5;
        }
        
        // Boost score for longer, detailed comments
        if (totalWords > 50) {
            score *= 1.2;
        }
        
        return Math.min(score, 1.0); // Cap at 1.0
    }
    
    /**
     * Checks if comment is spam
     */
    private boolean isSpam(String text) {
        String lower = text.toLowerCase();
        
        // Check for spam indicators
        if (text.split("http").length > 3) return true; // Too many links
        if (lower.contains("subscribe") && lower.contains("channel")) return true;
        if (lower.contains("check out my")) return true;
        if (lower.matches(".*[\\p{So}\\p{Cn}]{5,}.*")) return true; // Too many emojis
        if (text.matches("^[^a-zA-Z]*$")) return true; // No letters
        
        return false;
    }
    
    /**
     * Cleans HTML tags from text
     */
    private String cleanHtmlTags(String text) {
        return text.replaceAll("<br>", "\n")
                   .replaceAll("<[^>]+>", "")
                   .replaceAll("&quot;", "\"")
                   .replaceAll("&amp;", "&")
                   .replaceAll("&lt;", "<")
                   .replaceAll("&gt;", ">")
                   .replaceAll("&#39;", "'")
                   .trim();
    }
    
    /**
     * Initializes DSA keywords set
     */
    private void initializeDsaKeywords() {
        if (dsaKeywords == null) {
            dsaKeywords = Arrays.stream(dsaKeywordsString.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
            logger.debug("Initialized {} DSA keywords", dsaKeywords.size());
        }
    }
    
    /**
     * Fetches content from URL
     */
    private String fetchUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("YouTube API error: " + responseCode);
        }
        
        BufferedReader in = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
        );
        String inputLine;
        StringBuilder content = new StringBuilder();
        
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        
        in.close();
        conn.disconnect();
        
        return content.toString();
    }
    
    /**
     * Inner class for video metadata
     */
    private static class VideoMetadata {
        private final String title;
        private final String description;
        
        public VideoMetadata(String title, String description) {
            this.title = title;
            this.description = description;
        }
        
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }
    
    /**
     * Inner class for comment data
     */
    private static class Comment {
        private final String text;
        private final List<String> replies;
        
        public Comment(String text, List<String> replies) {
            this.text = text;
            this.replies = replies;
        }
        
        public String getText() { return text; }
        public List<String> getReplies() { return replies; }
    }
}