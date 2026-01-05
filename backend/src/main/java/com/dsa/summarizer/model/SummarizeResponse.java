package com.dsa.summarizer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model for video summarization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummarizeResponse {
    
    private boolean success;
    private String message;
    private DSASummary data;
    private String error;
    
    /**
     * Creates a success response with data
     */
    public static SummarizeResponse success(DSASummary data) {
        SummarizeResponse response = new SummarizeResponse();
        response.setSuccess(true);
        response.setMessage("Summary generated successfully");
        response.setData(data);
        response.setError(null);
        return response;
    }
    
    /**
     * Creates an error response with message
     */
    public static SummarizeResponse error(String errorMessage) {
        SummarizeResponse response = new SummarizeResponse();
        response.setSuccess(false);
        response.setMessage(null);
        response.setData(null);
        response.setError(errorMessage);
        return response;
    }
}