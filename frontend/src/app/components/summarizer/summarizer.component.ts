import { Component, OnInit } from '@angular/core';
import { SummarizerService } from '../../services/summarizer.service';
import { DSASummary, SummarizeResponse } from '../../models/dsa-summary.model';
import * as jsPDF from 'jspdf';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-summarizer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './summarizer.component.html',
  styleUrls: ['./summarizer.component.css'],
})
export class SummarizerComponent implements OnInit {
  youtubeUrl: string = '';
  isLoading: boolean = false;
  summary: DSASummary | null = null;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(private summarizerService: SummarizerService) {}

  ngOnInit(): void {
    // Check backend health on component init
    this.checkBackendHealth();
  }

  /**
   * Checks if backend is running
   */
  checkBackendHealth(): void {
    this.summarizerService.checkHealth().subscribe({
      next: (response) => {
        console.log('Backend is healthy:', response);
      },
      error: (error) => {
        console.error('Backend health check failed:', error);
        this.showError(
          'Cannot connect to backend. Please ensure the server is running on port 8080.'
        );
      },
    });
  }

  /**
   * Validates YouTube URL format
   */
  isValidYouTubeUrl(url: string): boolean {
    const pattern =
      /^(https?:\/\/)?(www\.)?(youtube\.com\/watch\?v=|youtu\.be\/)([a-zA-Z0-9_-]{11})/;
    return pattern.test(url);
  }

  /**
   * Handles form submission
   */
  onSubmit(): void {
    this.clearMessages();

    // Validate URL
    if (!this.youtubeUrl.trim()) {
      this.showError('Please enter a YouTube URL');
      return;
    }

    if (!this.isValidYouTubeUrl(this.youtubeUrl)) {
      this.showError(
        'Please enter a valid YouTube URL (e.g., https://www.youtube.com/watch?v=VIDEO_ID)'
      );
      return;
    }

    // Call API
    this.isLoading = true;
    this.summary = null;

    console.log('Submitting URL:', this.youtubeUrl);

    this.summarizerService.summarizeVideo(this.youtubeUrl).subscribe({
      next: (response: SummarizeResponse) => {
        this.isLoading = false;

        console.log('Response received:', response);

        if (response.success && response.data) {
          this.summary = response.data;
          this.showSuccess('Summary generated successfully!');
          this.scrollToSummary();
        } else {
          this.showError(
            response.error || 'Failed to generate summary. Please try again.'
          );
        }
      },
      error: (error: any) => {
        this.isLoading = false;
        console.error('Error occurred:', error);

        // More specific error messages
        let errorMsg = 'An error occurred while processing the video';

        if (error.message) {
          if (error.message.includes('transcript')) {
            errorMsg =
              'No transcript available for this video. Please ensure the video has captions.';
          } else if (error.message.includes('API key')) {
            errorMsg =
              'API key configuration error. Please check server configuration.';
          } else if (error.message.includes('Http failure')) {
            errorMsg =
              'Cannot connect to backend. Please ensure the server is running.';
          } else {
            errorMsg = error.message;
          }
        }

        this.showError(errorMsg);
      },
    });
  }

  /**
   * Downloads summary as PDF
   */
  downloadPDF(): void {
    if (!this.summary) {
      this.showError('No summary available to download');
      return;
    }

    try {
      const doc = new jsPDF.jsPDF();
      const pageWidth = doc.internal.pageSize.getWidth();
      const margin = 15;
      const maxWidth = pageWidth - margin * 2;
      let yPosition = 20;

      // Title
      doc.setFontSize(18);
      doc.setFont('helvetica', 'bold');
      doc.text('DSA Revision Notes', margin, yPosition);
      yPosition += 10;

      // Video ID
      doc.setFontSize(10);
      doc.setFont('helvetica', 'normal');
      doc.text(`Video ID: ${this.summary.videoId}`, margin, yPosition);
      yPosition += 15;

      // Add sections
      yPosition = this.addSection(
        doc,
        'Problem Summary',
        this.summary.problemSummary,
        margin,
        maxWidth,
        yPosition
      );
      yPosition += 10;

      yPosition = this.addSection(
        doc,
        'Algorithm Steps',
        this.summary.algorithmSteps,
        margin,
        maxWidth,
        yPosition
      );
      yPosition += 10;

      yPosition = this.addSection(
        doc,
        'Pseudocode',
        this.summary.pseudocode,
        margin,
        maxWidth,
        yPosition
      );
      yPosition += 10;

      yPosition = this.addSection(
        doc,
        'Time Complexity',
        this.summary.timeComplexity,
        margin,
        maxWidth,
        yPosition
      );
      yPosition += 10;

      yPosition = this.addSection(
        doc,
        'Space Complexity',
        this.summary.spaceComplexity,
        margin,
        maxWidth,
        yPosition
      );
      yPosition += 10;

      yPosition = this.addSection(
        doc,
        'Edge Cases',
        this.summary.edgeCases,
        margin,
        maxWidth,
        yPosition
      );
      yPosition += 10;

      yPosition = this.addSection(
        doc,
        'Revision Notes',
        this.summary.revisionNotes,
        margin,
        maxWidth,
        yPosition
      );

      // Save PDF
      const filename = `DSA_Notes_${
        this.summary.videoId
      }_${new Date().getTime()}.pdf`;
      doc.save(filename);
      this.showSuccess('PDF downloaded successfully!');
    } catch (error) {
      console.error('Error generating PDF:', error);
      this.showError('Failed to generate PDF. Please try again.');
    }
  }

  /**
   * Adds a section to PDF and returns the final Y position
   */
  private addSection(
    doc: jsPDF.jsPDF,
    title: string,
    content: string,
    x: number,
    maxWidth: number,
    y: number
  ): number {
    // Check if we need a new page
    if (y > 250) {
      doc.addPage();
      y = 20;
    }

    // Section title
    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text(title, x, y);
    y += 7;

    // Section content
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');

    // Handle empty content
    const textContent = content || 'Not available';
    const lines = doc.splitTextToSize(textContent, maxWidth);

    for (const line of lines) {
      if (y > 280) {
        doc.addPage();
        y = 20;
      }
      doc.text(line, x, y);
      y += 5;
    }

    return y;
  }

  /**
   * Scrolls to summary section
   */
  private scrollToSummary(): void {
    setTimeout(() => {
      const element = document.getElementById('summary-section');
      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 100);
  }

  /**
   * Shows error message
   */
  private showError(message: string): void {
    this.errorMessage = message;
    this.successMessage = '';

    // Auto-hide after 8 seconds for errors (longer to read)
    setTimeout(() => {
      this.errorMessage = '';
    }, 8000);
  }

  /**
   * Shows success message
   */
  private showSuccess(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';

    // Auto-hide after 3 seconds for success
    setTimeout(() => {
      this.successMessage = '';
    }, 3000);
  }

  /**
   * Clears all messages
   */
  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  /**
   * Resets the form
   */
  reset(): void {
    this.youtubeUrl = '';
    this.summary = null;
    this.clearMessages();
  }

  /**
   * Copies content to clipboard
   */
  copyToClipboard(content: string, sectionName: string): void {
    if (!content) {
      this.showError('No content to copy');
      return;
    }

    navigator.clipboard.writeText(content).then(
      () => {
        this.showSuccess(`${sectionName} copied to clipboard!`);
      },
      (err) => {
        console.error('Failed to copy:', err);
        this.showError('Failed to copy to clipboard');
      }
    );
  }
}
