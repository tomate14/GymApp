import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges } from '@angular/core';
import { EChartsOption } from 'echarts';
import { NgxEchartsDirective } from 'ngx-echarts';
import { DashboardChart } from '../../../../class/dashboard.model';
import { buildBarRaceOption, getChartSeries } from '../../../../utils/dashboard-echarts.util';

@Component({
  selector: 'app-students-by-teacher-race-chart',
  standalone: true,
  imports: [CommonModule, NgxEchartsDirective],
  templateUrl: './students-by-teacher-race-chart.component.html',
  styleUrl: './students-by-teacher-race-chart.component.css'
})
export class StudentsByTeacherRaceChartComponent implements OnChanges {
  @Input() chart: DashboardChart | null = null;

  options: EChartsOption = {};
  hasData = false;

  ngOnChanges(): void {
    const series = getChartSeries(this.chart);
    const sorted = series.labels
      .map((label, index) => ({ label, value: series.values[index] ?? 0 }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);
    const labels = sorted.map((item) => item.label);
    const values = sorted.map((item) => item.value);
    this.hasData = values.length > 0;
    this.options = this.hasData ? buildBarRaceOption(labels, values) : {};
  }
}
