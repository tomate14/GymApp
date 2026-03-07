import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges } from '@angular/core';
import { EChartsOption } from 'echarts';
import { NgxEchartsDirective } from 'ngx-echarts';
import { DashboardChart } from '../../../../class/dashboard.model';
import { buildStackedPercentOption, getChartSeries } from '../../../../utils/dashboard-echarts.util';

@Component({
  selector: 'app-occupancy-by-activity-chart',
  standalone: true,
  imports: [CommonModule, NgxEchartsDirective],
  templateUrl: './occupancy-by-activity-chart.component.html',
  styleUrl: './occupancy-by-activity-chart.component.css'
})
export class OccupancyByActivityChartComponent implements OnChanges {
  @Input() chart: DashboardChart | null = null;

  options: EChartsOption = {};
  hasData = false;

  ngOnChanges(): void {
    const series = getChartSeries(this.chart);
    const sorted = series.labels.map((label, index) => ({ label, value: series.values[index] ?? 0 })).sort((a, b) => b.value - a.value);
    const labels = sorted.map((item) => item.label);
    const values = sorted.map((item) => item.value);
    this.hasData = values.length > 0;
    this.options = this.hasData ? buildStackedPercentOption(labels, values) : {};
  }
}
