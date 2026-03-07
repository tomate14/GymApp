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
  @Output() readonly modeChange = new EventEmitter<DashboardStudentsTrendMode>();

  options: EChartsOption = {};
  selectedMode: DashboardStudentsTrendMode = 'month';
  hasData = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['mode']) {
      this.selectedMode = this.mode;
    }
    this.refreshOptions();
  }

  emitModeChange(mode: DashboardStudentsTrendMode): void {
    this.modeChange.emit(mode);
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
