import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  SummarizeRequest,
  SummarizeResponse,
} from '../models/dsa-summary.model';

@Injectable({
  providedIn: 'root',
})
export class SummarizerService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Calls backend to summarize YouTube video
   */
  summarizeVideo(youtubeUrl: string): Observable<SummarizeResponse> {
    const request: SummarizeRequest = { youtubeUrl };

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    return this.http
      .post<SummarizeResponse>(`${this.apiUrl}/summarize`, request, { headers })
      .pipe(catchError(this.handleError));
  }

  /**
   * Checks if backend is healthy
   */
  checkHealth(): Observable<string> {
    return this.http
      .get(`${this.apiUrl}/health`, { responseType: 'text' })
      .pipe(catchError(this.handleError));
  }

  /**
   * Handles HTTP errors
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage =
        error.error?.error || error.message || `Server error: ${error.status}`;
    }

    console.error('Service error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
