import { Component } from '@angular/core';
import { Routes } from '@angular/router';
import { combineLatest } from 'rxjs';
import { SummarizerComponent } from './components/summarizer/summarizer.component';

export const routes: Routes = [{ path: '', component: SummarizerComponent }];
