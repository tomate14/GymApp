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
  DashboardStudentsTrendFilters,
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
import { InfoPopupComponent } from '../../popup/info-popup.component';

type DashboardInfoContent = {
  title: string;
  summary: string;
  items: string[];
};

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
    OverloadsTableComponent,
    InfoPopupComponent
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
  studentsTrendFilters: DashboardStudentsTrendFilters = {
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
  activeInfoKey: string | null = null;

  readonly dashboardInfo: Record<string, DashboardInfoContent> = {
    'students-total': {
      title: 'Alumnos totales',
      summary: 'Te muestra cuantas personas forman hoy parte de tu base de alumnos.',
      items: [
        'Sirve para tener una referencia rapida del tamano actual del gimnasio.',
        'Te ayuda a comparar si el negocio esta creciendo, estable o perdiendo volumen.',
        'Es una foto actual y no cambia por el rango de fechas que selecciones.'
      ]
    },
    'students-new-period': {
      title: 'Altas de alumnos',
      summary: 'Te muestra cuantas personas nuevas se sumaron al gimnasio en el periodo elegido.',
      items: [
        'Sirve para medir si las acciones comerciales y la captacion estan funcionando.',
        'Te permite comparar meses o periodos para ver cuando ingresan mas alumnos nuevos.',
        'Si baja, puede ser una senal para reforzar promociones, seguimiento o presencia comercial.'
      ]
    },
    teachers: {
      title: 'Profesores con clases',
      summary: 'Te muestra cuantos profesores estuvieron realmente activos dando clases en el periodo.',
      items: [
        'Sirve para entender cuanta parte del equipo docente estuvo en movimiento en ese tramo.',
        'Ayuda a leer mejor si la agenda tuvo mucha o poca actividad operativa.',
        'Tambien te da contexto para interpretar carga de trabajo y organizacion del staff.'
      ]
    },
    'payment-ok': {
      title: 'Cuotas al dia',
      summary: 'Te muestra cuantos alumnos regularizaron su cuota dentro del periodo seleccionado.',
      items: [
        'Sirve para seguir de cerca como viene la gestion de cobros.',
        'Te ayuda a ver si el trabajo de renovacion se esta transformando en pagos reales.',
        'Mientras mas alto sea este valor, mejor acompanado esta el ingreso del gimnasio en ese periodo.'
      ]
    },
    'renewals-next': {
      title: 'Vencimientos en periodo',
      summary: 'Te muestra cuantos alumnos tienen la cuota para renovar dentro del periodo elegido.',
      items: [
        'Sirve para anticiparte y no esperar a que el alumno quede vencido.',
        'Te ayuda a organizar recordatorios, mensajes y seguimiento comercial.',
        'Cuanto mejor controles estos vencimientos, mas previsible se vuelve tu ingreso mensual.'
      ]
    },
    'classes-today': {
      title: 'Clases en periodo',
      summary: 'Te muestra el volumen real de clases dadas en el periodo seleccionado.',
      items: [
        'Sirve para entender cuan intensa fue la operacion del gimnasio en ese tramo.',
        'Te ayuda a comparar periodos con mas o menos movimiento de agenda.',
        'Tambien da contexto para leer asistencia, ocupacion y carga del equipo docente.'
      ]
    },
    occupancy: {
      title: 'Ocupacion promedio',
      summary: 'Te muestra que tan llenas estuvieron tus clases, en promedio, segun la asistencia real.',
      items: [
        'Un valor alto indica que los cupos se estan aprovechando bien.',
        'Un valor bajo puede mostrar horarios flojos o capacidad ociosa.',
        'Sirve para decidir si conviene abrir mas lugares, ajustar horarios o reorganizar la oferta.'
      ]
    },
    'active-routines': {
      title: 'Rutinas en periodo',
      summary: 'Te muestra cuantas rutinas estuvieron activas durante el periodo seleccionado.',
      items: [
        'Sirve para dimensionar el trabajo de planificacion y seguimiento sobre alumnos.',
        'Te ayuda a ver si hubo mas o menos actividad en el area de entrenamiento personalizado.',
        'Es una buena referencia para entender la carga operativa del equipo que arma rutinas.'
      ]
    },
    revenue: {
      title: 'Ingresos por renovaciones',
      summary: 'Te muestra cuanto dinero ingreso por renovaciones dentro del periodo seleccionado.',
      items: [
        'Sirve para seguir la evolucion real de la caja vinculada a cuotas.',
        'Te permite comparar periodos y detectar momentos fuertes o flojos de cobranza.',
        'Es una medida directa del dinero efectivamente cobrado por renovaciones.'
      ]
    },
    'students-trend': {
      title: 'Altas de alumnos',
      summary: 'Te ayuda a ver en que momentos del tiempo entran mas alumnos nuevos al gimnasio.',
      items: [
        'Podes mirarlo por mes o por ano segun el nivel de detalle que necesites.',
        'Sirve para detectar temporadas fuertes, meses flojos y etapas de crecimiento.',
        'Te ayuda a decidir en que momentos conviene reforzar promociones o campanas comerciales.'
      ]
    },
    'students-by-teacher': {
      title: 'Alumnos por profesor',
      summary: 'Te muestra que profesores concentran hoy mas alumnos activos en sus clases.',
      items: [
        'Sirve para ver si la carga esta repartida de forma pareja o si hay desequilibrios.',
        'Te ayuda a ordenar mejor horarios, demanda y asignacion del staff.',
        'Tambien permite detectar profesores con mucha demanda o con margen para crecer.'
      ]
    },
    'classes-by-activity': {
      title: 'Clases por actividad',
      summary: 'Te muestra que actividades tienen mas presencia en la agenda del gimnasio.',
      items: [
        'Sirve para entender que parte de tu oferta tiene mas peso en la operacion diaria.',
        'Te ayuda a revisar si la agenda acompana la estrategia comercial del negocio.',
        'Tambien puede mostrar actividades sobrerrepresentadas o con poca presencia real.'
      ]
    },
    'occupancy-by-activity': {
      title: 'Ocupacion por actividad',
      summary: 'Te permite ver que actividades llenan mejor sus cupos con asistencia real.',
      items: [
        'Las actividades con mejor ocupacion suelen ser las que tienen mas traccion real.',
        'Las que muestran valores bajos pueden necesitar cambios de horario, propuesta o capacidad.',
        'Sirve para decidir donde conviene crecer y donde hace falta ajustar.'
      ]
    },
    'payment-status': {
      title: 'Estado de pagos',
      summary: 'Te da una foto simple de como esta hoy la situacion de cobro de tus alumnos.',
      items: [
        'Te permite ver rapido cuantos alumnos estan al dia y cuantos tienen deuda.',
        'Sirve para detectar si la cobranza esta sana o si empieza a crecer el atraso.',
        'Si la deuda sube, conviene reforzar seguimiento, recordatorios y renovaciones.'
      ]
    },
    'students-by-plan': {
      title: 'Alumnos por plan',
      summary: 'Te ayuda a entender como se reparte hoy tu base de alumnos entre los distintos planes activos.',
      items: [
        'Sirve para ver que planes son los mas elegidos y cuales pesan menos en el negocio.',
        'Te ayuda a analizar mezcla comercial e identificar oportunidades de mejora de ingresos.',
        'Tambien permite detectar planes que tal vez necesiten ajustes o mejor comunicacion.'
      ]
    },
    'top-exercises': {
      title: 'Top ejercicios mas usados',
      summary: 'Te muestra que ejercicios tienen mas presencia en el entrenamiento de tus alumnos.',
      items: [
        'Sirve para entender que tipo de trabajo se esta usando mas dentro del gimnasio.',
        'Ayuda a detectar tendencias de entrenamiento y preferencias operativas.',
        'Tambien puede orientar compras, reemplazos o mejoras de equipamiento.'
      ]
    },
    'top-machines': {
      title: 'Top maquinas con mayor presion por unidad',
      summary: 'Te muestra que maquinas estan mas exigidas en relacion con la cantidad que tenes disponible.',
      items: [
        'Sirve para detectar donde puede haber mas espera, mas desgaste o mas necesidad de refuerzo.',
        'Te ayuda a decidir mejor que equipamiento conviene duplicar, mantener o redistribuir.',
        'Es mas util que mirar solo el uso total, porque tiene en cuenta cuantas unidades tenes de cada maquina.'
      ]
    },
    renewals: {
      title: 'Vencimientos de cuotas',
      summary: 'Te muestra que alumnos necesitan seguimiento por su vencimiento de cuota.',
      items: [
        'Sirve para priorizar a quien contactar primero y no perder renovaciones por olvido.',
        'Te ayuda a organizar recordatorios y acciones comerciales con mas orden.',
        'Mientras mejor se trabaje esta lista, mas estable suele ser el ingreso del mes.'
      ]
    },
    ranking: {
      title: 'Ranking de actividades',
      summary: 'Compara tus actividades para mostrar cuales son mas fuertes y cuales necesitan revision.',
      items: [
        'Te muestra que actividades tienen mas movimiento en clases, alumnos y aprovechamiento de cupos.',
        'Sirve para detectar cuales conviene sostener, impulsar o revisar.',
        'Es una vista muy util para decidir cambios en la oferta del gimnasio.'
      ]
    },
    overloads: {
      title: 'Sobreasignacion de planes',
      summary: 'Marca alumnos que hoy estan usando mas dias de los que su plan deberia permitir.',
      items: [
        'Sirve para detectar desorden operativo o servicio entregado por encima de lo contratado.',
        'Te ayuda a corregir casos que pueden afectar rentabilidad o generar inequidad entre alumnos.',
        'Tambien puede mostrar alumnos a los que les convendria pasar a un plan superior.'
      ]
    }
  };

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
    this.loadStudentsTrendChart();
  }

  onStudentsTrendDateRangeApply(range: DashboardStudentsTrendFilters): void {
    this.studentsTrendFilters = {
      fromDate: range.fromDate,
      toDate: range.toDate
    };
    this.loadStudentsTrendChart();
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

  get activeInfo(): DashboardInfoContent | null {
    if (this.activeInfoKey === null) {
      return null;
    }
    return this.dashboardInfo[this.activeInfoKey] ?? null;
  }

  openInfo(key: string): void {
    if (!this.dashboardInfo[key]) {
      return;
    }
    this.activeInfoKey = key;
  }

  closeInfo(): void {
    this.activeInfoKey = null;
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
        toDate: this.summaryFilters.toDate || undefined
      })
      .subscribe({
        next: (summary) => {
          this.summary = summary;
          this.kpis = summary.kpis;
          this.alerts = summary.alerts;
          this.charts = summary.charts;
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

  private loadStudentsTrendChart(): void {
    this.dashboardService
      .getStudentsTrendChart({
        fromDate: this.studentsTrendFilters.fromDate || undefined,
        toDate: this.studentsTrendFilters.toDate || undefined,
        studentsTrendMode: this.studentsTrendMode
      })
      .subscribe({
        next: (chart) => {
          this.studentsTrendChart = chart;
        },
        error: () => {
          this.apiError = 'No se pudo obtener el grafico de altas de alumnos.';
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
    this.loadStudentsTrendChart();
    this.loadRenewals();
    this.loadRanking();
    this.loadOverloads();
  }
}
