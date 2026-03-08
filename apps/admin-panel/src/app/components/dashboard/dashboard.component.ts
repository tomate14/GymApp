import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  DashboardActivityRankingRow,
  DashboardAlert,
  DashboardChart,
  DashboardKpi,
  DashboardOverloadsFilters,
  DashboardPageResponse,
  DashboardRankingFilters,
  DashboardRenewalsFilters,
  DashboardPlanOverloadRow,
  DashboardRenewalRow,
  DashboardSummary,
  DashboardSummaryFilters,
  DashboardStudentsTrendMode
} from '../../class/dashboard.model';
import { Plan } from '../../class/plan.model';
import { ClassesByActivityChartComponent } from './charts/classes-by-activity-chart/classes-by-activity-chart.component';
import { OccupancyGaugeKpiComponent } from './charts/occupancy-gauge-kpi/occupancy-gauge-kpi.component';
import { OccupancyByActivityChartComponent } from './charts/occupancy-by-activity-chart/occupancy-by-activity-chart.component';
import { PaymentStatusChartComponent } from './charts/payment-status-chart/payment-status-chart.component';
import { StudentsByPlanChartComponent } from './charts/students-by-plan-chart/students-by-plan-chart.component';
import { StudentsByTeacherRaceChartComponent } from './charts/students-by-teacher-race-chart/students-by-teacher-race-chart.component';
import { StudentsTrendChartComponent } from './charts/students-trend-chart/students-trend-chart.component';
import { TopExercisesChartComponent } from './charts/top-exercises-chart/top-exercises-chart.component';
import { TopMachinesChartComponent } from './charts/top-machines-chart/top-machines-chart.component';
import { OverloadsTableComponent } from './tables/overloads-table/overloads-table.component';
import { RankingTableComponent } from './tables/ranking-table/ranking-table.component';
import { RenewalsTableComponent } from './tables/renewals-table/renewals-table.component';
import { DashboardService } from '../../services/dashboard.service';
import { PlansService } from '../../services/plans.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    StudentsTrendChartComponent,
    OccupancyGaugeKpiComponent,
    ClassesByActivityChartComponent,
    OccupancyByActivityChartComponent,
    StudentsByTeacherRaceChartComponent,
    PaymentStatusChartComponent,
    StudentsByPlanChartComponent,
    TopExercisesChartComponent,
    TopMachinesChartComponent,
    RenewalsTableComponent,
    RankingTableComponent,
    OverloadsTableComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  summary: DashboardSummary | null = null;
  kpis: DashboardKpi[] = [];
  alerts: DashboardAlert[] = [];
  charts: DashboardChart[] = [];

  studentsTrendChart: DashboardChart | null = null;
  classesByActivityChart: DashboardChart | null = null;
  occupancyByActivityChart: DashboardChart | null = null;
  studentsByTeacherChart: DashboardChart | null = null;
  paymentStatusChart: DashboardChart | null = null;
  studentsByPlanChart: DashboardChart | null = null;
  topExercisesChart: DashboardChart | null = null;
  topMachinesChart: DashboardChart | null = null;

  plans: Plan[] = [];

  summaryFilters: DashboardSummaryFilters = {
    fromDate: this.oneMonthAgoIsoDate(),
    toDate: this.todayIsoDate()
  };
  studentsTrendMode: DashboardStudentsTrendMode = 'month';

  renewalsFilters: DashboardRenewalsFilters = {
    search: '',
    planId: 0,
    paymentStatus: '',
    renewalFrom: '',
    renewalTo: ''
  };

  rankingFilters: DashboardRankingFilters = {
    search: '',
    fromDate: '',
    toDate: ''
  };

  overloadsFilters: DashboardOverloadsFilters = {
    search: ''
  };

  renewalsPage = this.emptyPage<DashboardRenewalRow>();
  rankingPage = this.emptyPage<DashboardActivityRankingRow>();
  overloadsPage = this.emptyPage<DashboardPlanOverloadRow>();

  renewalsCurrentPage = 1;
  rankingCurrentPage = 1;
  overloadsCurrentPage = 1;

  renewalsPageSize = 8;
  rankingPageSize = 8;
  overloadsPageSize = 8;
  readonly pageSizeOptions = [5, 8, 10, 15];

  isLoadingSummary = false;
  isLoadingRenewals = false;
  isLoadingRanking = false;
  isLoadingOverloads = false;

  apiError: string | null = null;

  constructor(
    private readonly dashboardService: DashboardService,
    private readonly plansService: PlansService
  ) {}

  ngOnInit(): void {
    this.loadOptions();
    this.applyGlobalDateFilter();
  }

  refreshSummary(): void {
    this.applyGlobalDateFilter();
  }

  onStudentsTrendModeChange(mode: DashboardStudentsTrendMode): void {
    this.studentsTrendMode = mode;
    this.loadSummary();
  }

  alertClass(alert: DashboardAlert): string {
    if (alert.severity === 'high') {
      return 'text-bg-danger';
    }
    if (alert.severity === 'medium') {
      return 'text-bg-warning';
    }
    return 'text-bg-secondary';
  }

  goToRenewalsPage(page: number): void {
    if (page < 1 || page > this.renewalsTotalPages) {
      return;
    }
    this.renewalsCurrentPage = page;
    this.loadRenewals();
  }

  goToRankingPage(page: number): void {
    if (page < 1 || page > this.rankingTotalPages) {
      return;
    }
    this.rankingCurrentPage = page;
    this.loadRanking();
  }

  goToOverloadsPage(page: number): void {
    if (page < 1 || page > this.overloadsTotalPages) {
      return;
    }
    this.overloadsCurrentPage = page;
    this.loadOverloads();
  }

  onRenewalsFiltersChange(): void {
    this.renewalsCurrentPage = 1;
    this.loadRenewals();
  }

  onRankingFiltersChange(): void {
    this.rankingCurrentPage = 1;
    this.loadRanking();
  }

  onOverloadsFiltersChange(): void {
    this.overloadsCurrentPage = 1;
    this.loadOverloads();
  }

  clearRenewalsFilters(): void {
    this.renewalsFilters = {
      search: '',
      planId: 0,
      paymentStatus: '',
      renewalFrom: '',
      renewalTo: ''
    };
    this.onRenewalsFiltersChange();
  }

  clearRankingFilters(): void {
    this.rankingFilters = {
      search: '',
      fromDate: '',
      toDate: ''
    };
    this.onRankingFiltersChange();
  }

  clearOverloadsFilters(): void {
    this.overloadsFilters = {
      search: ''
    };
    this.onOverloadsFiltersChange();
  }

  onRenewalsPageSizeChange(size: number): void {
    this.renewalsPageSize = size;
    this.renewalsCurrentPage = 1;
    this.loadRenewals();
  }

  onRankingPageSizeChange(size: number): void {
    this.rankingPageSize = size;
    this.rankingCurrentPage = 1;
    this.loadRanking();
  }

  onOverloadsPageSizeChange(size: number): void {
    this.overloadsPageSize = size;
    this.overloadsCurrentPage = 1;
    this.loadOverloads();
  }

  get renewalsTotalPages(): number {
    return Math.max(1, this.renewalsPage.totalPages || 1);
  }

  get rankingTotalPages(): number {
    return Math.max(1, this.rankingPage.totalPages || 1);
  }

  get overloadsTotalPages(): number {
    return Math.max(1, this.overloadsPage.totalPages || 1);
  }

  get regularKpis(): DashboardKpi[] {
    return this.kpis.filter((kpi) => !this.isOccupancyKpi(kpi));
  }

  get occupancyKpi(): DashboardKpi | null {
    return this.kpis.find((kpi) => this.isOccupancyKpi(kpi)) ?? null;
  }

  get kpiColumnLeft(): DashboardKpi[] {
    const midpoint = Math.ceil(this.regularKpis.length / 2);
    return this.regularKpis.slice(0, midpoint);
  }

  get kpiColumnRight(): DashboardKpi[] {
    const midpoint = Math.ceil(this.regularKpis.length / 2);
    return this.regularKpis.slice(midpoint);
  }

  private loadOptions(): void {
    this.plansService.findAll().subscribe({
      next: (plans) => {
        this.plans = plans;
      }
    });
  }

  private loadSummary(): void {
    this.isLoadingSummary = true;
    this.dashboardService
      .getSummary({
        fromDate: this.summaryFilters.fromDate || undefined,
        toDate: this.summaryFilters.toDate || undefined,
        studentsTrendMode: this.studentsTrendMode
      })
      .subscribe({
        next: (summary) => {
          this.summary = summary;
          this.kpis = summary.kpis;
          this.alerts = summary.alerts;
          this.charts = summary.charts;
          this.studentsTrendChart = this.findChart('students-trend');
          this.classesByActivityChart = this.findChart('classes-by-activity');
          this.occupancyByActivityChart = this.findChart('occupancy-by-activity');
          this.studentsByTeacherChart = this.findChart('students-by-teacher');
          this.paymentStatusChart = this.findChart('payment-status');
          this.studentsByPlanChart = this.findChart('students-by-plan');
          this.topExercisesChart = this.findChart('top-exercises');
          this.topMachinesChart = this.findChart('top-machines');
          this.isLoadingSummary = false;
        },
        error: () => {
          this.apiError = 'No se pudieron obtener datos del dashboard.';
          this.isLoadingSummary = false;
        }
      });
  }

  private loadRenewals(): void {
    this.isLoadingRenewals = true;
    this.dashboardService
      .getRenewals({
        search: this.renewalsFilters.search || undefined,
        planId: this.renewalsFilters.planId || undefined,
        paymentStatus: this.renewalsFilters.paymentStatus || undefined,
        fromDate: this.summaryFilters.fromDate || undefined,
        toDate: this.summaryFilters.toDate || undefined,
        renewalFrom: this.renewalsFilters.renewalFrom || undefined,
        renewalTo: this.renewalsFilters.renewalTo || undefined,
        page: this.renewalsCurrentPage - 1,
        size: this.renewalsPageSize
      })
      .subscribe({
        next: (result) => {
          this.renewalsPage = result;
          this.renewalsCurrentPage = result.page + 1;
          this.isLoadingRenewals = false;
        },
        error: () => {
          this.apiError = 'No se pudo obtener la tabla de vencimientos.';
          this.isLoadingRenewals = false;
        }
      });
  }

  private loadRanking(): void {
    this.isLoadingRanking = true;
    const globalFrom = this.summaryFilters.fromDate;
    const globalTo = this.summaryFilters.toDate;
    const localFrom = this.rankingFilters.fromDate || globalFrom;
    const localTo = this.rankingFilters.toDate || globalTo;
    const rankingFromDate = localFrom > globalFrom ? localFrom : globalFrom;
    const rankingToDate = localTo < globalTo ? localTo : globalTo;
    const safeRankingFromDate = rankingFromDate > rankingToDate ? globalFrom : rankingFromDate;
    const safeRankingToDate = rankingFromDate > rankingToDate ? globalTo : rankingToDate;
    this.dashboardService
      .getActivityRanking({
        search: this.rankingFilters.search || undefined,
        fromDate: safeRankingFromDate || undefined,
        toDate: safeRankingToDate || undefined,
        page: this.rankingCurrentPage - 1,
        size: this.rankingPageSize
      })
      .subscribe({
        next: (result) => {
          this.rankingPage = result;
          this.rankingCurrentPage = result.page + 1;
          this.isLoadingRanking = false;
        },
        error: () => {
          this.apiError = 'No se pudo obtener el ranking de actividades.';
          this.isLoadingRanking = false;
        }
      });
  }

  private loadOverloads(): void {
    this.isLoadingOverloads = true;
    this.dashboardService
      .getPlanOverloads({
        search: this.overloadsFilters.search || undefined,
        fromDate: this.summaryFilters.fromDate || undefined,
        toDate: this.summaryFilters.toDate || undefined,
        page: this.overloadsCurrentPage - 1,
        size: this.overloadsPageSize
      })
      .subscribe({
        next: (result) => {
          this.overloadsPage = result;
          this.overloadsCurrentPage = result.page + 1;
          this.isLoadingOverloads = false;
        },
        error: () => {
          this.apiError = 'No se pudo obtener el control de sobreasignacion.';
          this.isLoadingOverloads = false;
        }
      });
  }

  private emptyPage<T>(): DashboardPageResponse<T> {
    return {
      content: [],
      page: 0,
      size: 8,
      totalElements: 0,
      totalPages: 1
    };
  }

  private todayIsoDate(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private oneMonthAgoIsoDate(): string {
    const date = new Date();
    date.setMonth(date.getMonth() - 1);
    return date.toISOString().slice(0, 10);
  }

  private findChart(key: string): DashboardChart | null {
    return this.charts.find((chart) => chart.key === key) ?? null;
  }

  private isOccupancyKpi(kpi: DashboardKpi): boolean {
    return kpi.key === 'occupancy' || kpi.label.toLowerCase().includes('ocupacion promedio');
  }

  private applyGlobalDateFilter(): void {
    this.renewalsCurrentPage = 1;
    this.rankingCurrentPage = 1;
    this.overloadsCurrentPage = 1;
    this.loadSummary();
    this.loadRenewals();
    this.loadRanking();
    this.loadOverloads();
  }
}
