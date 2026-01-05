package com.dsa.summarizer.controller;

import com.dsa.summarizer.model.DSASummary;
import com.dsa.summarizer.model.SummarizeRequest;
import com.dsa.summarizer.model.SummarizeResponse;
import com.dsa.summarizer.service.ChunkingService;
import com.dsa.summarizer.service.GeminiService;
import com.dsa.summarizer.service.TranscriptService;
import com.dsa.summarizer.service.YouTubeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class SummarizerController {
    
    private static final Logger logger = LoggerFactory.getLogger(SummarizerController.class);
    
    @Autowired
    private YouTubeService youTubeService;
    
    @Autowired
    private TranscriptService transcriptService;
    
    @Autowired
    private ChunkingService chunkingService;
    
    @Autowired
    private GeminiService geminiService;
    
    /**
     * Main endpoint to summarize YouTube DSA lecture
     * OPTIMIZED: Now uses only ONE Gemini API call per video
     */
    @PostMapping("/summarize")
    public ResponseEntity<SummarizeResponse> summarizeVideo(
            @Valid @RequestBody SummarizeRequest request) {
        
        logger.info("=== STARTING SUMMARIZATION (SINGLE API CALL MODE) ===");
        logger.info("Received request for URL: {}", request.getYoutubeUrl());
        
        try {
            // Step 1: Extract video ID
            String videoId = youTubeService.extractVideoId(request.getYoutubeUrl());
            logger.info("✓ Extracted video ID: {}", videoId);
            
            // Step 2: Fetch transcript (tries 3 fallback methods)
            String transcript = transcriptService.fetchTranscript(videoId);
            logger.info("✓ Fetched transcript. Length: {} characters", transcript.length());
            
            if (transcript.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(SummarizeResponse.error("No transcript available for this video"));
            }
            
            // Step 3: Chunk transcript for safety (600-800 words per chunk)
            List<String> chunks = chunkingService.chunkTranscript(transcript);
            logger.info("✓ Created {} chunks from transcript", chunks.size());
            
            // Step 4: Generate DSA summary with SINGLE API call
            // This method internally:
            // - Selects top 3-4 most information-dense chunks
            // - Merges them into one prompt
            // - Makes ONE Gemini API call
            // - Returns structured DSA summary
            logger.info("⚡ Generating DSA summary with SINGLE Gemini API call...");
            DSASummary dsaSummary = geminiService.generateDSASummaryFromMergedChunks(chunks, videoId);
            dsaSummary.setVideoTitle("YouTube Video - " + videoId);
            
            logger.info("✓ Successfully generated DSA summary");
            logger.info("=== SUMMARIZATION COMPLETE (1 API call used) ===");
            
            return ResponseEntity.ok(SummarizeResponse.success(dsaSummary));
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(SummarizeResponse.error("Invalid YouTube URL: " + e.getMessage()));
            
        } catch (Exception e) {
            logger.error("Error processing video", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SummarizeResponse.error("Failed to process video: " + e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is running (Single API call mode)");
    }
}