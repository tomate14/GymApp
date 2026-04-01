import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EChartsOption } from 'echarts';
import { NgxEchartsDirective } from 'ngx-echarts';
import { IonButton, IonCard, IonCardContent, IonIcon, IonInput, IonSpinner } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { addOutline, analyticsOutline, chevronDownOutline, chevronUpOutline } from 'ionicons/icons';
import { MobilePersonalRecord, MobilePersonalRecordDetail, MobilePersonalRecordHistoryPoint } from '../../models/mobile.model';
import { MobileAppService } from '../../services/mobile-app.service';

type EditableRecord = MobilePersonalRecord & {
  draftWeight: number | null;
  draftDate: string;
  saving: boolean;
  detailLoading: boolean;
  formOpen: boolean;
  detail: MobilePersonalRecordDetail | null;
  chartOptions: EChartsOption;
};

@Component({
  selector: 'app-personal-records-page',
  standalone: true,
  imports: [CommonModule, FormsModule, NgxEchartsDirective, IonCard, IonCardContent, IonInput, IonButton, IonIcon, IonSpinner],
  templateUrl: './personal-records.page.html',
  styleUrl: './personal-records.page.css'
})
export class PersonalRecordsPageComponent implements OnInit {
  records: EditableRecord[] = [];
  errorMessage: string | null = null;
  isLoading = true;
  selectedExerciseId: number | null = null;
  searchTerm = '';

  constructor(private readonly mobileAppService: MobileAppService) {
    addIcons({ addOutline, analyticsOutline, chevronDownOutline, chevronUpOutline });
  }

  ngOnInit(): void {
    this.loadRecords();
  }

  toggleRecord(record: EditableRecord): void {
    if (this.selectedExerciseId === record.exerciseId) {
      this.selectedExerciseId = null;
      record.formOpen = false;
      return;
    }

    this.selectedExerciseId = record.exerciseId;
    this.loadRecordDetail(record);
  }

  toggleEntryForm(record: EditableRecord, event: Event): void {
    event.stopPropagation();
    this.selectedExerciseId = record.exerciseId;
    record.formOpen = !record.formOpen;
    this.loadRecordDetail(record);
  }

  saveRecord(record: EditableRecord): void {
    const weight = record.draftWeight === null ? null : Number(record.draftWeight);

    if (weight === null || Number.isNaN(weight) || weight < 0 || record.draftDate === '') {
      this.errorMessage = 'Completa peso y fecha para guardar la entrada.';
      return;
    }

    this.errorMessage = null;
    record.saving = true;

    this.mobileAppService.savePersonalRecord(record.exerciseId, {
      maxWeight: weight,
      recordedAt: record.draftDate
    }).subscribe({
      next: (saved) => {
        record.maxWeight = saved.maxWeight;
        record.recordedAt = saved.recordedAt;
        record.draftWeight = saved.maxWeight;
        record.draftDate = new Date().toISOString().slice(0, 10);
        record.saving = false;
        record.formOpen = false;
        this.loadRecordDetail(record, true);
      },
      error: (err: { error?: { message?: string } }) => {
        this.errorMessage = err.error?.message ?? 'No se pudo guardar el peso maximo.';
        record.saving = false;
      }
    });
  }

  isExpanded(record: EditableRecord): boolean {
    return this.selectedExerciseId === record.exerciseId;
  }

  get filteredRecords(): EditableRecord[] {
    const normalized = this.searchTerm.trim().toLowerCase();
    if (!normalized) {
      return this.records;
    }

    return this.records.filter((record) => record.exerciseName.toLowerCase().includes(normalized));
  }

  formatWeight(value: number | null): string {
    return value === null ? 'Sin marca' : `${value} kg`;
  }

  formatDate(value: string | null): string {
    if (!value) {
      return 'Sin fecha';
    }

    return new Intl.DateTimeFormat('es-AR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    }).format(new Date(`${value}T00:00:00`));
  }

  private loadRecords(): void {
    this.mobileAppService.getPersonalRecords().subscribe({
      next: (records) => {
        this.records = records.map((item) => this.toEditableRecord(item));
        this.isLoading = false;
      },
      error: (err: { error?: { message?: string } }) => {
        this.errorMessage = err.error?.message ?? 'No se pudieron cargar tus registros.';
        this.isLoading = false;
      }
    });
  }

  private loadRecordDetail(record: EditableRecord, force = false): void {
    if (!force && record.detail !== null) {
      return;
    }

    record.detailLoading = true;

    this.mobileAppService.getPersonalRecordDetail(record.exerciseId).subscribe({
      next: (detail) => {
        record.detail = detail;
        record.chartOptions = this.buildHistoryChart(detail.history);
        record.detailLoading = false;
      },
      error: (err: { error?: { message?: string } }) => {
        this.errorMessage = err.error?.message ?? 'No se pudo cargar el detalle del ejercicio.';
        record.detailLoading = false;
      }
    });
  }

  private buildHistoryChart(history: MobilePersonalRecordHistoryPoint[]): EChartsOption {
    return {
      animationDuration: 500,
      grid: { left: 16, right: 16, top: 24, bottom: 24, containLabel: true },
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(8, 12, 20, 0.94)',
        borderColor: 'rgba(255, 255, 255, 0.08)',
        textStyle: { color: '#f6f7fb' }
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: history.map((item) => this.formatDate(item.recordedAt)),
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.12)' } },
        axisLabel: { color: '#98a2be', fontSize: 11 }
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.08)' } },
        axisLabel: { color: '#98a2be', formatter: '{value} kg' }
      },
      series: [
        {
          type: 'line',
          smooth: true,
          data: history.map((item) => item.weight),
          symbolSize: 8,
          lineStyle: { width: 3, color: '#ff8f42' },
          itemStyle: { color: '#ff6a2d' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(255, 106, 45, 0.34)' },
                { offset: 1, color: 'rgba(255, 106, 45, 0.04)' }
              ]
            }
          }
        }
      ]
    };
  }

  private toEditableRecord(item: MobilePersonalRecord): EditableRecord {
    return {
      ...item,
      draftWeight: item.maxWeight,
      draftDate: new Date().toISOString().slice(0, 10),
      saving: false,
      detailLoading: false,
      formOpen: false,
      detail: null,
      chartOptions: {}
    };
  }
}
