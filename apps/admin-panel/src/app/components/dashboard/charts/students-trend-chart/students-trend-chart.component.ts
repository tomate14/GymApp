import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EChartsOption } from 'echarts';
import { NgxEchartsDirective } from 'ngx-echarts';
import { DashboardChart, DashboardStudentsTrendMode } from '../../../../class/dashboard.model';
import { buildVerticalBarWithTrendOption, getChartSeries } from '../../../../utils/dashboard-echarts.util';

@Component({
  selector: 'app-students-trend-chart',
  standalone: true,
  imports: [CommonModule, FormsModule, NgxEchartsDirective],
  templateUrl: './students-trend-chart.component.html',
  styleUrl: './students-trend-chart.component.css'
})
export class StudentsTrendChartComponent implements OnChanges {
  @Input() chart: DashboardChart | null = null;
  @Input() mode: DashboardStudentsTrendMode = 'month';
  @Input() fromDate = '';
  @Input() toDate = '';
  @Output() readonly modeChange = new EventEmitter<DashboardStudentsTrendMode>();
  @Output() readonly dateRangeApply = new EventEmitter<{ fromDate: string; toDate: string }>();

  options: EChartsOption = {};
  selectedMode: DashboardStudentsTrendMode = 'month';
  selectedFromDate = '';
  selectedToDate = '';
  hasData = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['mode']) {
      this.selectedMode = this.mode;
    }
    if (changes['fromDate']) {
      this.selectedFromDate = this.fromDate;
    }
    if (changes['toDate']) {
      this.selectedToDate = this.toDate;
    }
    this.refreshOptions();
  }

  emitModeChange(mode: DashboardStudentsTrendMode): void {
    this.modeChange.emit(mode);
  }

  applyDateRange(): void {
    this.dateRangeApply.emit({
      fromDate: this.selectedFromDate,
      toDate: this.selectedToDate
    });
  }

  private refreshOptions(): void {
    const series = getChartSeries(this.chart);
    const sorted = series.labels.map((label, index) => ({ label, value: series.values[index] ?? 0 })).sort((a, b) => b.value - a.value);
    const labels = sorted.map((item) => item.label);
    const values = sorted.map((item) => item.value);
    this.hasData = values.length > 0;
    this.options = this.hasData ? buildVerticalBarWithTrendOption(labels, values) : {};
  }
}
