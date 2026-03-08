import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Activity } from '../../../../class/activity.model';
import {
  DashboardClassRow,
  DashboardClassesFilters,
  DashboardPageResponse
} from '../../../../class/dashboard.model';
import { User } from '../../../../class/user.model';

@Component({
  selector: 'app-classes-table',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './classes-table.component.html',
  styleUrl: './classes-table.component.css'
})
export class ClassesTableComponent implements OnChanges {
  private readonly dayNames = ['Lun', 'Mar', 'Mie', 'Jue', 'Vie', 'Sab', 'Dom'];
  private readonly monthNames = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];

  @Input() activities: Activity[] = [];
  @Input() teachers: User[] = [];
  @Input() filters: DashboardClassesFilters = {
    search: '',
    activityId: 0,
    teacherId: 0
  };
  @Input() page: DashboardPageResponse<DashboardClassRow> = {
    content: [],
    page: 0,
      size: 8,
      totalElements: 0,
      totalPages: 1
  };
  @Input() weekStart = '';
  @Input() isLoading = false;

  weekRangeLabel = '';
  weekDays: Array<{ isoDate: string; shortLabel: string; dayNumber: number }> = [];
  allDayCells: Array<{ classes: DashboardClassRow[] }> = [];
  calendarRows: Array<{ time: string; cells: Array<{ classes: DashboardClassRow[] }> }> = [];
  private expandedEventKeys = new Set<string>();

  @Output() readonly filtersChange = new EventEmitter<void>();
  @Output() readonly clearFilters = new EventEmitter<void>();
  @Output() readonly previousWeek = new EventEmitter<void>();
  @Output() readonly nextWeek = new EventEmitter<void>();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['page'] || changes['weekStart']) {
      this.rebuildCalendar();
    }
  }

  onPreviousWeek(): void {
    this.previousWeek.emit();
  }

  onNextWeek(): void {
    this.nextWeek.emit();
  }

  onFiltersChanged(): void {
    this.filtersChange.emit();
  }

  onClearFilters(): void {
    this.clearFilters.emit();
  }

  capacityLabel(item: DashboardClassRow): string {
    if (item.maxCapacity === null) {
      return `${item.enrolled} / sin limite`;
    }
    return `${item.enrolled} / ${item.maxCapacity}`;
  }

  occupancyClass(item: DashboardClassRow): string {
    if (item.maxCapacity === null) {
      return 'occupancy-open';
    }
    if (item.occupancyPercent >= 90) {
      return 'occupancy-high';
    }
    if (item.occupancyPercent >= 70) {
      return 'occupancy-medium';
    }
    return 'occupancy-low';
  }

  scheduleLine(item: DashboardClassRow): string {
    if (item.allDay) {
      return `Todo el dia - ${this.capacityLabel(item)}`;
    }
    const start = this.normalizedTime(item.startTime);
    const end = this.normalizedTime(item.endTime);
    if (start === '' || end === '') {
      return `Horario sin definir - ${this.capacityLabel(item)}`;
    }
    return `${start} - ${end} - ${this.capacityLabel(item)}`;
  }

  toggleEvent(item: DashboardClassRow): void {
    const key = this.eventKey(item);
    if (this.expandedEventKeys.has(key)) {
      this.expandedEventKeys.delete(key);
      return;
    }
    this.expandedEventKeys.add(key);
  }

  isExpanded(item: DashboardClassRow): boolean {
    return this.expandedEventKeys.has(this.eventKey(item));
  }

  private rebuildCalendar(): void {
    this.expandedEventKeys.clear();
    const weekStartDate = this.parseIsoDate(this.weekStart);
    if (weekStartDate === null) {
      this.weekDays = [];
      this.allDayCells = [];
      this.calendarRows = [];
      this.weekRangeLabel = '';
      return;
    }

    this.weekDays = Array.from({ length: 7 }, (_, index) => {
      const date = this.addDays(weekStartDate, index);
      return {
        isoDate: this.toIsoDate(date),
        shortLabel: this.dayNames[index],
        dayNumber: date.getDate()
      };
    });
    this.weekRangeLabel = this.buildWeekRangeLabel(weekStartDate, this.addDays(weekStartDate, 6));

    const validDays = new Set(this.weekDays.map((day) => day.isoDate));
    const allDayGroupedItems = new Map<string, DashboardClassRow[]>();
    const groupedItems = new Map<string, DashboardClassRow[]>();
    const timeSlots = new Set<string>();

    for (const item of this.page.content) {
      if (!validDays.has(item.date)) {
        continue;
      }
      if (item.allDay) {
        const bucket = allDayGroupedItems.get(item.date) ?? [];
        bucket.push(item);
        allDayGroupedItems.set(item.date, bucket);
        continue;
      }

      const safeTime = this.normalizedTime(item.startTime);
      if (safeTime === '') {
        continue;
      }
      const key = `${item.date}|${safeTime}`;
      const bucket = groupedItems.get(key) ?? [];
      bucket.push(item);
      groupedItems.set(key, bucket);
      timeSlots.add(safeTime);
    }

    const computedAllDayCells = this.weekDays.map((day) => ({
      classes: (allDayGroupedItems.get(day.isoDate) ?? []).slice().sort((a, b) => a.className.localeCompare(b.className))
    }));
    this.allDayCells = computedAllDayCells.some((cell) => cell.classes.length > 0) ? computedAllDayCells : [];
    const orderedSlots = Array.from(timeSlots).sort((a, b) => a.localeCompare(b));
    this.calendarRows = orderedSlots.map((slot) => ({
      time: slot,
      cells: this.weekDays.map((day) => ({
        classes: (groupedItems.get(`${day.isoDate}|${slot}`) ?? []).slice().sort((a, b) => a.className.localeCompare(b.className))
      }))
    }));
  }

  private parseIsoDate(value: string): Date | null {
    const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
    if (match === null) {
      return null;
    }
    const year = Number(match[1]);
    const month = Number(match[2]);
    const day = Number(match[3]);
    return new Date(year, month - 1, day);
  }

  private toIsoDate(value: Date): string {
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private addDays(value: Date, amount: number): Date {
    const result = new Date(value.getFullYear(), value.getMonth(), value.getDate());
    result.setDate(result.getDate() + amount);
    return result;
  }

  private normalizedTime(value: string | null): string {
    if (!value) {
      return '';
    }
    return value.length >= 5 ? value.slice(0, 5) : value;
  }

  private eventKey(item: DashboardClassRow): string {
    return `${item.classId}|${item.date}`;
  }

  private buildWeekRangeLabel(start: Date, end: Date): string {
    const startDay = start.getDate();
    const endDay = end.getDate();
    const startMonth = this.monthNames[start.getMonth()];
    const endMonth = this.monthNames[end.getMonth()];
    const startYear = start.getFullYear();
    const endYear = end.getFullYear();

    if (startYear === endYear && startMonth === endMonth) {
      return `${startDay} - ${endDay} ${startMonth} ${startYear}`;
    }
    if (startYear === endYear) {
      return `${startDay} ${startMonth} - ${endDay} ${endMonth} ${startYear}`;
    }
    return `${startDay} ${startMonth} ${startYear} - ${endDay} ${endMonth} ${endYear}`;
  }
}
