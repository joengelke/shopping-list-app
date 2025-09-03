import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';

import { ShoppingList, ShoppingListService } from '../../core/shopping-list/shopping-list';
import { AnalysisService, ShoppingItemActivity } from '../../core/analysis/analysis';

@Component({
  standalone: true,
  selector: 'app-analytics',
  templateUrl: './analytics.html',
  styleUrl: './analytics.scss',
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatListModule,
    MatIconModule
  ]
})
export class AnalyticsComponent implements OnInit {

  activeSection: string = 'analytics';
  
  shoppingLists: ShoppingList[] = [];
  selectedList: ShoppingList | null = null;

  displayedColumns: string[] = ['timestamp', 'name', 'amount'];
  shoppingItemActivities: ShoppingItemActivity[] = [];
  activitiesLoading = false;

  filterUserId: string | null = null;
  filterFrom: Date | null = null;
  filterTo: Date | null = null;
  filterName: string | null = null;

  availableActivityNames: string[] = [];
  averageAmount: number | null = null;
  averagePeriod: string = '';

  constructor(
    private shoppingListService: ShoppingListService,
    private analysisService: AnalysisService
  ) {}

  ngOnInit() {
    this.loadShoppingLists();
  }

  loadShoppingLists() {
    this.shoppingListService.getShoppingLists().subscribe({
      next: (lists) => this.shoppingLists = lists,
      error: (err) => console.log('Error loading shopping lists:', err)
    });
  }

  loadActivityNames() {
    if (!this.selectedList) return;
    this.analysisService.getActivityNames(this.selectedList.id).subscribe({
      next: names => this.availableActivityNames = names,
      error: err => this.availableActivityNames = []
    });
  }

  loadFilteredActivities() {
    if (!this.selectedList) {
      this.shoppingItemActivities = [];
      return;
    }
    this.activitiesLoading = true;
    this.analysisService.getActivities(
      this.selectedList.id,
      this.filterUserId || undefined,
      this.filterName || undefined,
      this.filterFrom || undefined,
      this.filterTo || undefined
    ).subscribe({
      next: (activities) => {
        this.shoppingItemActivities = activities;
        this.activitiesLoading = false;
      },
      error: (err) => {
        this.shoppingItemActivities = [];
        this.activitiesLoading = false;
      }
    });
  }

  showAverage(period: 'week' | 'month' | 'year') {
    if (!this.filterName) {
      this.averageAmount = null;
      this.averagePeriod = '';
      return;
    }
    this.averagePeriod = period;
    this.averageAmount = this.analysisService.getAverageAmount(
      this.shoppingItemActivities,
      this.filterName,
      period
    );
  }

  compareStrings = (a: string, b: string) => a === b;

  selectList(list: ShoppingList) {
    this.selectedList = list;
    this.filterUserId = null;
    this.filterFrom = null;
    this.filterTo = null;
    this.filterName = null;
    this.averageAmount = null;
    this.averagePeriod = '';
    this.loadActivityNames();
    this.loadFilteredActivities();
  }
}
