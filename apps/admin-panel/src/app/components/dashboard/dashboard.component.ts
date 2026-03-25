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
      summary: 'Muestra la cantidad total de alumnos cargados hoy en el sistema.',
      items: [
        'Incluye toda la base actual de alumnos, sin importar cuando se hayan dado de alta.',
        'Sirve para tener una referencia rapida del tamano actual del gimnasio.',
        'No depende del rango de fechas seleccionado.'
      ]
    },
    'students-new-period': {
      title: 'Altas de alumnos',
      summary: 'Muestra cuantos alumnos nuevos ingresaron en el rango de fechas elegido.',
      items: [
        'Cuenta alumnos cuya fecha de alta cae dentro del periodo seleccionado.',
        'Sirve para medir captacion y crecimiento en ese tramo de tiempo.',
        'Si el numero baja, conviene revisar acciones comerciales y seguimiento de prospectos.'
      ]
    },
    teachers: {
      title: 'Profesores con clases',
      summary: 'Muestra cuantos profesores tuvieron al menos una clase dentro del periodo seleccionado.',
      items: [
        'No cuenta profesores por fecha de alta, sino profesores que realmente dictaron clases en ese rango.',
        'Sirve para ver cuanta dotacion docente estuvo activa en la agenda.',
        'Ayuda a interpretar mejor la carga operativa del gimnasio en el periodo.'
      ]
    },
    'payment-ok': {
      title: 'Cuotas al dia',
      summary: 'Muestra cuantos alumnos quedaron al dia con una renovacion cobrada dentro del periodo elegido.',
      items: [
        'Se toma la renovacion registrada como pago dentro del periodo, no la fecha de alta del alumno.',
        'Solo cuenta alumnos que quedaron al dia y tienen un cobro de renovacion en ese rango.',
        'Sirve para medir cuantas cuotas se regularizaron efectivamente en ese tramo.'
      ]
    },
    'renewals-next': {
      title: 'Vencimientos en periodo',
      summary: 'Muestra cuantos alumnos tienen vencimiento de cuota dentro del rango elegido.',
      items: [
        'Sirve para anticipar contactos, recordatorios y acciones de renovacion.',
        'Ayuda a ordenar la gestion de cobranzas antes de que la cuota quede vencida.',
        'Mientras mas controlado este este numero, mejor previsibilidad tenes sobre los ingresos.'
      ]
    },
    'classes-today': {
      title: 'Clases en periodo',
      summary: 'Muestra cuantas clases se dictaron dentro del rango de fechas seleccionado.',
      items: [
        'Cuenta ocurrencias reales de clase dentro del periodo, no solo la ficha base de cada actividad.',
        'Sirve para ver el volumen operativo real del gimnasio en ese tramo.',
        'Ayuda a relacionar agenda, asistencia y uso del equipo docente.'
      ]
    },
    occupancy: {
      title: 'Ocupacion promedio',
      summary: 'Muestra que porcentaje promedio del cupo se lleno con asistentes reales en el periodo.',
      items: [
        'Se calcula usando alumnos marcados como presentes sobre el cupo de cada clase.',
        'No se basa solo en inscriptos, sino en asistencia real.',
        'Sirve para detectar si la agenda esta bien aprovechada o si hay capacidad ociosa.'
      ]
    },
    'active-routines': {
      title: 'Rutinas en periodo',
      summary: 'Muestra cuantas rutinas tuvieron vigencia dentro del rango elegido.',
      items: [
        'Cuenta rutinas cuya fecha se cruza con el periodo seleccionado.',
        'Sirve para dimensionar el trabajo operativo de planificacion y seguimiento.',
        'No depende de si la rutina empezo exactamente en ese rango, sino de si estuvo vigente.'
      ]
    },
    revenue: {
      title: 'Ingresos por renovaciones',
      summary: 'Muestra el monto cobrado por renovaciones registradas dentro del periodo elegido.',
      items: [
        'Se toma el comprobante de pago registrado al renovar la cuota.',
        'No usa la fecha de alta del alumno ni una estimacion teorica del plan.',
        'Sirve para ver cuanto dinero ingreso realmente por renovaciones en ese tramo.'
      ]
    },
    'students-trend': {
      title: 'Altas de alumnos',
      summary: 'Te ayuda a ver en que momentos entran mas alumnos nuevos al gimnasio.',
      items: [
        'Cada barra muestra cuantas altas de alumnos hubo dentro del rango que elegiste para este grafico.',
        'Podes mirarlo por mes o por ano para comparar etapas de crecimiento.',
        'Sirve para detectar temporadas fuertes, meses flojos y momentos donde conviene reforzar acciones comerciales.'
      ]
    },
    'students-by-teacher': {
      title: 'Alumnos por profesor',
      summary: 'Muestra cuantos alumnos con plan activo hoy tienen clases asociadas a cada profesor.',
      items: [
        'Permite ver que profesores concentran mas alumnos activos y cuales hoy tienen menor carga.',
        'Ayuda a equilibrar horarios, demanda y organizacion del equipo docente.',
        'Sirve tambien para detectar rapidamente si algun profesor esta quedando sobrecargado o subutilizado.'
      ]
    },
    'classes-by-activity': {
      title: 'Clases por actividad',
      summary: 'Te muestra que actividades ocuparon mas espacio en la agenda general del gimnasio.',
      items: [
        'Cada barra representa cuantas clases se dictaron por actividad desde el inicio de uso del sistema hasta hoy.',
        'Sirve para ver si la oferta real acompana lo que queres impulsar en el negocio.',
        'Tambien ayuda a detectar actividades demasiado presentes o con poca participacion en la agenda.'
      ]
    },
    'occupancy-by-activity': {
      title: 'Ocupacion por actividad',
      summary: 'Te permite entender que actividades convierten mejor sus cupos en asistentes reales.',
      items: [
        'Un porcentaje alto indica que esa actividad suele tener buena asistencia real respecto de su cupo.',
        'Un porcentaje bajo puede mostrar clases con espacio ocioso o baja presencia de alumnos.',
        'Es util para decidir si conviene abrir mas horarios, reducirlos o ajustar la capacidad.'
      ]
    },
    'payment-status': {
      title: 'Estado de pagos',
      summary: 'Resume rapidamente como esta la salud de cobro de la base de alumnos.',
      items: [
        'Te muestra el total actual de alumnos al dia y alumnos con deuda.',
        'Sirve para tener una foto general de cobranzas desde que usas la aplicacion hasta hoy.',
        'Si aumenta la deuda, conviene revisar renovaciones, recordatorios y seguimiento comercial.'
      ]
    },
    'students-by-plan': {
      title: 'Alumnos por plan',
      summary: 'Te ayuda a entender como se reparte hoy la base de alumnos con planes activos.',
      items: [
        'Permite ver que planes activos concentran mas alumnos hoy.',
        'Es clave para analizar mezcla comercial y oportunidades de mejora de ingresos.',
        'Tambien ayuda a detectar planes poco elegidos o con bajo nivel de adopcion real.'
      ]
    },
    'top-exercises': {
      title: 'Top ejercicios mas usados',
      summary: 'Muestra que ejercicios aparecen con mas frecuencia en las rutinas del gimnasio.',
      items: [
        'Sirve para entender que tipo de trabajo se esta prescribiendo mas a los alumnos.',
        'Ayuda a ver tendencias de entrenamiento dentro del gimnasio.',
        'Tambien puede orientar compras, reemplazos o mejoras de equipamiento asociado.'
      ]
    },
    'top-machines': {
      title: 'Top maquinas mas usadas',
      summary: 'Te muestra que maquinas aparecen mas en la planificacion de entrenamiento.',
      items: [
        'Sirve para detectar cuales son las maquinas con mayor demanda potencial.',
        'Ayuda a priorizar mantenimiento, reemplazo o incorporacion de nuevos equipos.',
        'Tambien permite ver si hay maquinas subutilizadas que no estan aportando valor.'
      ]
    },
    renewals: {
      title: 'Vencimientos de cuotas',
      summary: 'Te muestra que alumnos tienen su cuota cerca de vencer o dentro del rango elegido.',
      items: [
        'Sirve para hacer seguimiento comercial antes de que el alumno quede fuera de termino.',
        'Te ayuda a priorizar contactos, recordatorios y acciones de renovacion.',
        'Mientras mas ordenada este esta tabla, mejor control tenes sobre los ingresos del mes.'
      ]
    },
    ranking: {
      title: 'Ranking de actividades',
      summary: 'Compara las actividades segun volumen de clases, cantidad de anotados y nivel de ocupacion.',
      items: [
        'Clases te dice que actividades tuvieron mas presencia en la agenda.',
        'Anotados muestra cuales mueven mas alumnos en total.',
        'Ocupacion ayuda a ver cuales aprovechan mejor sus cupos disponibles.',
        'En conjunto, sirve para decidir que actividades conviene sostener, expandir o revisar.'
      ]
    },
    overloads: {
      title: 'Sobreasignacion de planes',
      summary: 'Marca alumnos que hoy tienen mas dias asignados de los que su plan permite.',
      items: [
        'Sirve para detectar desorden operativo o casos donde se esta regalando servicio sin control.',
        'Ayuda a corregir asignaciones antes de que impacten en rentabilidad o en la experiencia del alumno.',
        'Tambien permite identificar alumnos que deberian pasar a un plan superior.'
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
