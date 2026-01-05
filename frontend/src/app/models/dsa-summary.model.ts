export interface DSASummary {
  videoId: string;
  videoTitle: string;
  problemSummary: string;
  algorithmSteps: string;
  pseudocode: string;
  timeComplexity: string;
  spaceComplexity: string;
  edgeCases: string;
  revisionNotes: string;
}

export interface SummarizeRequest {
  youtubeUrl: string;
}

export interface SummarizeResponse {
  success: boolean;
  message?: string;
  data?: DSASummary;
  error?: string;
}
