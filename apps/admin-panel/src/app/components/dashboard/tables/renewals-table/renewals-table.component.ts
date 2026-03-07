import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  DashboardPageResponse,
  DashboardRenewalRow,
  DashboardRenewalsFilters
} from '../../../../class/dashboard.model';
import { Plan } from '../../../../class/plan.model';

@Component({
  selector: 'app-renewals-table',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './renewals-table.component.html',
  styleUrl: './renewals-table.component.css'
})
export class RenewalsTableComponent {
  @Input() plans: Plan[] = [];
  @Input() filters: DashboardRenewalsFilters = {
    search: '',
    planId: 0,
    paymentStatus: '',
    renewalFrom: '',
    renewalTo: ''
  };
  @Input() page: DashboardPageResponse<DashboardRenewalRow> = {
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
