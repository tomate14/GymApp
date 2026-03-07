import { EChartsOption } from 'echarts';
import { DashboardChart } from '../class/dashboard.model';

const TEXT_COLOR = '#f4f5f7';
const SOFT_TEXT_COLOR = '#a7a9b2';
const GRID_LINE_COLOR = 'rgba(255, 255, 255, 0.08)';
const BAR_GRADIENT_START = '#ff5f1f';
const BAR_GRADIENT_END = '#ff8a00';

const PALETTE = ['#ff5f1f', '#ff8a00', '#00b4d8', '#2a9d8f', '#90e0ef', '#e76f51', '#f4a261', '#8ecae6', '#219ebc', '#fb8500'];

export function getChartSeries(chart: DashboardChart | null): { labels: string[]; values: number[] } {
  if (!chart) {
    return { labels: [], values: [] };
  }
  return {
    labels: chart.points.map((point) => point.label),
    values: chart.points.map((point) => point.value)
  };
}

export function buildVerticalBarOption(labels: string[], values: number[], unit = ''): EChartsOption {
  return {
    animationDuration: 500,
    color: [BAR_GRADIENT_START],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: '#0f1015',
      borderColor: 'rgba(255,255,255,0.12)',
      textStyle: { color: TEXT_COLOR },
      formatter: (params) => {
        const point = Array.isArray(params) ? params[0] : params;
        const value = typeof point?.value === 'number' ? point.value : 0;
        const label = ((point as { axisValueLabel?: string } | undefined)?.axisValueLabel ?? point?.name ?? '') as string;
        return `${label}<br/><strong>${value}${unit}</strong>`;
      }
    },
    grid: {
      top: 18,
      left: 8,
      right: 12,
      bottom: 8,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: GRID_LINE_COLOR } },
      axisTick: { show: false },
      axisLabel: { color: SOFT_TEXT_COLOR, interval: 0, rotate: labels.length > 7 ? 25 : 0 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: GRID_LINE_COLOR } },
      axisLabel: { color: SOFT_TEXT_COLOR },
      axisLine: { show: false }
    },
    series: [
      {
        type: 'bar',
        data: values,
        barMaxWidth: 34,
        itemStyle: {
          borderRadius: [10, 10, 0, 0],
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: BAR_GRADIENT_END },
              { offset: 1, color: BAR_GRADIENT_START }
            ]
          }
        }
      }
    ]
  };
}

export function buildVerticalBarWithTrendOption(labels: string[], values: number[]): EChartsOption {
  const barOption = buildVerticalBarOption(labels, values);
  return {
    ...barOption,
    legend: {
      top: 0,
      right: 6,
      textStyle: { color: SOFT_TEXT_COLOR },
      itemWidth: 12,
      itemHeight: 8
    },
    series: [
      {
        type: 'bar',
        name: 'Altas',
        data: values,
        barMaxWidth: 30,
        itemStyle: {
          borderRadius: [10, 10, 0, 0],
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: BAR_GRADIENT_END },
              { offset: 1, color: BAR_GRADIENT_START }
            ]
          }
        }
      },
      {
        type: 'line',
        name: 'Tendencia',
        data: values,
        smooth: true,
        symbolSize: 7,
        lineStyle: { width: 3, color: '#42d4f4' },
        itemStyle: { color: '#42d4f4' },
        emphasis: { focus: 'series' }
      }
    ]
  };
}

export function buildHorizontalBarOption(labels: string[], values: number[], unit = ''): EChartsOption {
  return {
    animationDuration: 500,
    color: [BAR_GRADIENT_START],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: '#0f1015',
      borderColor: 'rgba(255,255,255,0.12)',
      textStyle: { color: TEXT_COLOR },
      formatter: (params) => {
        const point = Array.isArray(params) ? params[0] : params;
        const value = typeof point?.value === 'number' ? point.value : 0;
        return `${point?.name ?? ''}<br/><strong>${value}${unit}</strong>`;
      }
    },
    grid: {
      top: 8,
      left: 12,
      right: 12,
      bottom: 8,
      containLabel: true
    },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: GRID_LINE_COLOR } },
      axisLabel: { color: SOFT_TEXT_COLOR },
      axisLine: { show: false }
    },
    yAxis: {
      type: 'category',
      inverse: true,
      data: labels,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: GRID_LINE_COLOR } },
      axisLabel: { color: SOFT_TEXT_COLOR }
    },
    series: [
      {
        type: 'bar',
        data: values,
        barMaxWidth: 24,
        itemStyle: {
          borderRadius: [0, 10, 10, 0],
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: BAR_GRADIENT_START },
              { offset: 1, color: BAR_GRADIENT_END }
            ]
          }
        }
      }
    ]
  };
}

export function buildBarRaceOption(labels: string[], values: number[]): EChartsOption {
  return {
    animation: false,
    grid: {
      top: 8,
      left: 12,
      right: 18,
      bottom: 8,
      containLabel: true
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: '#0f1015',
      borderColor: 'rgba(255,255,255,0.12)',
      textStyle: { color: TEXT_COLOR },
      formatter: (params) => {
        const point = Array.isArray(params) ? params[0] : params;
        const value = (point?.value ?? 0) as number;
        return `${point?.name ?? ''}<br/><strong>${value} alumnos</strong>`;
      }
    },
    xAxis: {
      type: 'value',
      axisLabel: { color: SOFT_TEXT_COLOR },
      splitLine: { lineStyle: { color: GRID_LINE_COLOR } }
    },
    yAxis: {
      type: 'category',
      inverse: true,
      max: 9,
      data: labels,
      axisLabel: {
        color: SOFT_TEXT_COLOR,
        fontWeight: 600
      },
      axisTick: { show: false },
      axisLine: { show: false }
    },
    series: [
      {
        type: 'bar',
        data: values.map((value, index) => ({
          value,
          itemStyle: {
            color: PALETTE[index % PALETTE.length],
            borderRadius: [0, 10, 10, 0]
          }
        })),
        realtimeSort: false,
        barCategoryGap: '35%',
        label: {
          show: true,
          position: 'right',
          color: TEXT_COLOR,
          fontWeight: 700,
          valueAnimation: true,
          formatter: '{c}'
        }
      }
    ]
  };
}

export function buildStackedPercentOption(labels: string[], occupancyValues: number[]): EChartsOption {
  const occupied = occupancyValues.map((value) => Math.max(0, Math.min(100, Math.round(value * 100) / 100)));
  const free = occupied.map((value) => Math.max(0, 100 - value));

  return {
    animationDuration: 500,
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: '#0f1015',
      borderColor: 'rgba(255,255,255,0.12)',
      textStyle: { color: TEXT_COLOR },
      formatter: (params) => {
        const rows = Array.isArray(params) ? params : [params];
        const label = rows[0]?.name ?? '';
        const values = rows.map((row) => `${row.seriesName}: ${row.value}%`).join('<br/>');
        return `${label}<br/>${values}`;
      }
    },
    legend: {
      top: 0,
      right: 6,
      textStyle: { color: SOFT_TEXT_COLOR }
    },
    grid: {
      top: 28,
      left: 12,
      right: 12,
      bottom: 8,
      containLabel: true
    },
    xAxis: {
      type: 'value',
      min: 0,
      max: 100,
      splitLine: { lineStyle: { color: GRID_LINE_COLOR } },
      axisLabel: { color: SOFT_TEXT_COLOR, formatter: '{value}%' }
    },
    yAxis: {
      type: 'category',
      inverse: true,
      data: labels,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: GRID_LINE_COLOR } },
      axisLabel: { color: SOFT_TEXT_COLOR }
    },
    series: [
      {
        name: 'Ocupado',
        type: 'bar',
        stack: 'total',
        data: occupied,
        barMaxWidth: 24,
        itemStyle: { color: '#ff8a00', borderRadius: [0, 0, 0, 0] }
      },
      {
        name: 'Libre',
        type: 'bar',
        stack: 'total',
        data: free,
        barMaxWidth: 24,
        itemStyle: { color: '#2c313d', borderRadius: [0, 10, 10, 0] }
      }
    ]
  };
}

export function buildDonutOption(labels: string[], values: number[]): EChartsOption {
  return {
    animationDuration: 500,
    color: PALETTE,
    tooltip: {
      trigger: 'item',
      backgroundColor: '#0f1015',
      borderColor: 'rgba(255,255,255,0.12)',
      textStyle: { color: TEXT_COLOR },
      formatter: (params) => {
        const point = Array.isArray(params) ? params[0] : params;
        const name = (point?.name ?? '') as string;
        const value = (point?.value ?? 0) as number | string;
        const percent = ((point as { percent?: number } | undefined)?.percent ?? 0) as number;
        return `${name}: ${value} (${percent}%)`;
      }
    },
    legend: {
      bottom: 0,
      textStyle: { color: SOFT_TEXT_COLOR },
      itemWidth: 12,
      itemHeight: 8
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '72%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        minAngle: 5,
        label: {
          color: TEXT_COLOR,
          formatter: '{b}: {d}%'
        },
        labelLine: { length: 10, length2: 8 },
        data: labels.map((label, index) => ({
          name: label,
          value: values[index] ?? 0
        }))
      }
    ]
  };
}
