import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges } from '@angular/core';
import { EChartsOption } from 'echarts';
import { NgxEchartsDirective } from 'ngx-echarts';
import { DashboardKpi } from '../../../../class/dashboard.model';

@Component({
  selector: 'app-occupancy-gauge-kpi',
  standalone: true,
  imports: [CommonModule, NgxEchartsDirective],
  templateUrl: './occupancy-gauge-kpi.component.html',
  styleUrl: './occupancy-gauge-kpi.component.css'
})
export class OccupancyGaugeKpiComponent implements OnChanges {
  @Input() kpi: DashboardKpi | null = null;

  options: EChartsOption = {};
  hasData = false;

  ngOnChanges(): void {
    if (!this.kpi) {
      this.hasData = false;
      this.options = {};
      return;
    }

    const value = this.parsePercentValue(this.kpi.value);
    if (value === null) {
      this.hasData = false;
      this.options = {};
      return;
    }

    this.hasData = true;
    this.options = this.buildGaugeOption(value, this.performanceLabel(value));
  }

  private parsePercentValue(raw: string): number | null {
    const cleaned = raw.replace('%', '').trim().replace(/\s/g, '').replace(',', '.');
    if (cleaned === '') {
      return null;
    }
    const value = Number(cleaned);
    if (Number.isNaN(value)) {
      return null;
    }
    return Math.max(0, Math.min(100, value));
  }

  private performanceLabel(value: number): string {
    if (value < 25) {
      return 'Malo';
    }
    if (value < 50) {
      return 'Regular';
    }
    if (value < 75) {
      return 'Bueno';
    }
    return 'Excelente';
  }

  private buildGaugeOption(value: number, status: string): EChartsOption {
    return {
      series: [
        {
          type: 'gauge',
          min: 0,
          max: 100,
          startAngle: 225,
          endAngle: -45,
          splitNumber: 4,
          axisLine: {
            lineStyle: {
              width: 18,
              color: [
                [0.25, '#111111'],
                [0.5, '#424242'],
                [0.75, '#7a7a7a'],
                [1, '#d6d6d6']
              ]
            }
          },
          pointer: {
            itemStyle: {
              color: '#f8f9fa'
            }
          },
          axisTick: {
            distance: -22,
            splitNumber: 5,
            lineStyle: {
              width: 1,
              color: 'rgba(255,255,255,0.35)'
            }
          },
          splitLine: {
            distance: -22,
            length: 14,
            lineStyle: {
              width: 2,
              color: 'rgba(255,255,255,0.55)'
            }
          },
          axisLabel: {
            distance: -40,
            color: 'rgba(255,255,255,0.75)',
            fontSize: 11
          },
          title: {
            color: 'rgba(255,255,255,0.75)',
            fontSize: 14,
            offsetCenter: [0, '50%']
          },
          detail: {
            valueAnimation: true,
            fontSize: 26,
            fontWeight: 700,
            color: '#ffffff',
            offsetCenter: [0, '75%'],
            formatter: (currentValue: number) => `${currentValue.toFixed(1)}%`
          },
          data: [
            {
              value: Number(value.toFixed(1)),
              name: status
            }
          ]
        }
      ]
    };
  }
}
