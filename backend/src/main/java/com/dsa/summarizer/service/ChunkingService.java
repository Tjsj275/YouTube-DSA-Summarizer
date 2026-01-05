package com.dsa.summarizer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChunkingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChunkingService.class);
    
    @Value("${chunking.min.words:600}")
    private int minWords;
    
    @Value("${chunking.max.words:800}")
    private int maxWords;
    
    /**
     * Splits transcript into semantic chunks without breaking sentences
     */
    public List<String> chunkTranscript(String transcript) {
        logger.debug("Chunking transcript into {}-{} word segments", minWords, maxWords);
        
        List<String> chunks = new ArrayList<>();
        List<String> sentences = splitIntoSentences(transcript);
        
        StringBuilder currentChunk = new StringBuilder();
        int currentWordCount = 0;
        
        for (String sentence : sentences) {
            int sentenceWordCount = countWords(sentence);
            
            // If adding this sentence would exceed max words, start new chunk
            if (currentWordCount > 0 && currentWordCount + sentenceWordCount > maxWords) {
                // Only create chunk if it meets minimum word count
                if (currentWordCount >= minWords) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                    currentWordCount = 0;
                }
            }
            
            currentChunk.append(sentence).append(" ");
            currentWordCount += sentenceWordCount;
        }
        
        // Add remaining chunk if it has content
        if (currentWordCount > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        logger.debug("Created {} chunks from transcript", chunks.size());
        return chunks;
    }
    
    /**
     * Splits text into sentences
     */
    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        
        // Pattern to match sentence boundaries
        Pattern pattern = Pattern.compile("[^.!?]+[.!?]+");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            String sentence = matcher.group().trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }
        
        // Handle any remaining text without punctuation
        int lastEnd = 0;
        matcher = pattern.matcher(text);
        while (matcher.find()) {
            lastEnd = matcher.end();
        }
        
        if (lastEnd < text.length()) {
            String remaining = text.substring(lastEnd).trim();
            if (!remaining.isEmpty()) {
                sentences.add(remaining);
            }
        }
        
        return sentences;
    }
    
    /**
     * Counts words in text
     */
    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}