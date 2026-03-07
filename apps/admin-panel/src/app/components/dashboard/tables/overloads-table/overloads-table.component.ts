import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  DashboardOverloadsFilters,
  DashboardPageResponse,
  DashboardPlanOverloadRow
} from '../../../../class/dashboard.model';

@Component({
  selector: 'app-overloads-table',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './overloads-table.component.html',
  styleUrl: './overloads-table.component.css'
})
export class OverloadsTableComponent {
  @Input() filters: DashboardOverloadsFilters = {
    search: ''
  };
  @Input() page: DashboardPageResponse<DashboardPlanOverloadRow> = {
    content: [],
    page: 0,
    size: 8,
    totalElements: 0,
    totalPages: 1
  };
  @Input() currentPage = 1;
  @Input() pageSize = 8;
  @Input() pageSizeOptions: number[] = [5, 8, 10, 15];
  @Input() isLoading = false;

  @Output() readonly filtersChange = new EventEmitter<void>();
  @Output() readonly clearFilters = new EventEmitter<void>();
  @Output() readonly pageChange = new EventEmitter<number>();
  @Output() readonly pageSizeChange = new EventEmitter<number>();

  get totalPages(): number {
    return Math.max(1, this.page.totalPages || 1);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, index) => index + 1);
  }

  onFiltersChanged(): void {
    this.filtersChange.emit();
  }

  onClearFilters(): void {
    this.clearFilters.emit();
  }

  onPageSizeChanged(value: number): void {
    this.pageSizeChange.emit(value);
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return;
    }
    this.pageChange.emit(page);
  }
}
