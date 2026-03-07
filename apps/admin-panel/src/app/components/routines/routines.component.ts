import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { createEmptyPageResponse, PageResponse } from '../../class/page.model';
import { Exercise } from '../../class/exercise.model';
import { SearchableFieldConfig, SearchableSelectValue } from '../../class/searchable-select.model';
import {
  createEmptyRoutineAssignmentDraft,
  createEmptyRoutineTemplateDraft,
  createEmptyRoutineTemplateExerciseDraft,
  DayOfWeek,
  RoutineAssignment,
  RoutineAssignmentDraft,
  RoutineAssignmentExercise,
  RoutineTemplate,
  RoutineTemplateDraft,
  RoutineTemplateExerciseDraft
} from '../../class/routine.model';
import { User } from '../../class/user.model';
import { SearchableSelectComponent } from '../searchable-select/searchable-select.component';
import { ConfirmPopupComponent } from '../../popup/confirm-popup.component';
import { ExercisesService } from '../../services/exercises.service';
import { PlansService } from '../../services/plans.service';
import {
  RoutineAssignmentPageFilters,
  RoutineTemplatePageFilters,
  RoutinesService
} from '../../services/routines.service';
import { UsersService } from '../../services/users.service';
import { ensureMinNumber } from '../../utils/id.util';

type TemplateFilters = {
  name: string;
  minExercises: number;
  minStudents: number;
};

type AssignmentFilters = {
  student: string;
  templateId: number | 0;
};

@Component({
  selector: 'app-routines',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmPopupComponent, SearchableSelectComponent],
  templateUrl: './routines.component.html',
  styleUrl: './routines.component.css'
})
export class RoutinesComponent implements OnInit {
  readonly dayOptions: Array<{ value: DayOfWeek; label: string }> = [
    { value: 'lunes', label: 'Lunes' },
    { value: 'martes', label: 'Martes' },
    { value: 'miercoles', label: 'Miercoles' },
    { value: 'jueves', label: 'Jueves' },
    { value: 'viernes', label: 'Viernes' },
    { value: 'sabado', label: 'Sabado' },
    { value: 'domingo', label: 'Domingo' }
  ];

  students: User[] = [];
  studentSelectItems: Record<string, unknown>[] = [];
  exercises: Exercise[] = [];
  routineTemplates: RoutineTemplate[] = [];
  allRoutineAssignments: RoutineAssignment[] = [];
  routineAssignments: RoutineAssignment[] = [];
  templateTablePage: PageResponse<RoutineTemplate> = createEmptyPageResponse<RoutineTemplate>();
  assignmentTablePage: PageResponse<RoutineAssignment> = createEmptyPageResponse<RoutineAssignment>();

  templateDraft: RoutineTemplateDraft = createEmptyRoutineTemplateDraft();
  templateExerciseDraft: RoutineTemplateExerciseDraft = createEmptyRoutineTemplateExerciseDraft();
  assignmentDraft: RoutineAssignmentDraft = createEmptyRoutineAssignmentDraft();

  editingTemplateId: number | null = null;
  editingAssignmentId: number | null = null;

  showTemplateForm = false;
  showAssignmentForm = false;
  historyStudentId: number | null = null;

  deleteTemplateCandidate: RoutineTemplate | null = null;
  deleteAssignmentCandidate: RoutineAssignment | null = null;
  isLoading = false;
  apiError: string | null = null;

  readonly studentFilterFields: SearchableFieldConfig[] = [
    { field: 'firstName', type: 'text' },
    { field: 'lastName', type: 'text' },
    { field: 'dni', type: 'number' }
  ];

  templateFilters: TemplateFilters = {
    name: '',
    minExercises: 0,
    minStudents: 0
  };

  assignmentFilters: AssignmentFilters = {
    student: '',
    templateId: 0
  };

  templateCurrentPage = 1;
  assignmentCurrentPage = 1;
  templatePageSize = 8;
  assignmentPageSize = 8;
  readonly pageSizeOptions = [5, 8, 10, 15];

  constructor(
    private readonly routinesService: RoutinesService,
    private readonly usersService: UsersService,
    private readonly exercisesService: ExercisesService,
    private readonly plansService: PlansService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.templateDraft.createdAt = this.todayIsoDate();
  }

  get filteredTemplates(): RoutineTemplate[] {
    return this.templateTablePage.content;
  }

  get filteredAssignments(): RoutineAssignment[] {
    return this.assignmentTablePage.content;
  }

  get templateTotalPages(): number {
    return Math.max(1, this.templateTablePage.totalPages || 1);
  }

  get assignmentTotalPages(): number {
    return Math.max(1, this.assignmentTablePage.totalPages || 1);
  }

  get paginatedTemplates(): RoutineTemplate[] {
    return this.templateTablePage.content;
  }

  get paginatedAssignments(): RoutineAssignment[] {
    return this.assignmentTablePage.content;
  }

  get templatePageNumbers(): number[] {
    return Array.from({ length: this.templateTotalPages }, (_, idx) => idx + 1);
  }

  get assignmentPageNumbers(): number[] {
    return Array.from({ length: this.assignmentTotalPages }, (_, idx) => idx + 1);
  }

  get assignmentPlanWarningMessage(): string | null {
    return this.calculateAssignmentPlanWarning();
  }

  get selectedStudentHistory(): RoutineAssignment[] {
    if (this.historyStudentId === null) {
      return [];
    }

    return this.allRoutineAssignments
      .filter((assignment) => assignment.userId === this.historyStudentId)
      .sort((current, next) => next.startDate.localeCompare(current.startDate));
  }

  toggleTemplateForm(): void {
    this.showTemplateForm = !this.showTemplateForm;
  }

  toggleAssignmentForm(): void {
    this.showAssignmentForm = !this.showAssignmentForm;
  }

  onAssignmentDayToggle(day: DayOfWeek, enabled: boolean): void {
    if (enabled) {
      if (!this.assignmentDraft.days.includes(day)) {
        this.assignmentDraft.days = [...this.assignmentDraft.days, day];
      }
      return;
    }

    this.assignmentDraft.days = this.assignmentDraft.days.filter((item) => item !== day);
  }

  isAssignmentDaySelected(day: DayOfWeek): boolean {
    return this.assignmentDraft.days.includes(day);
  }

  addExerciseToTemplate(): void {
    if (this.templateExerciseDraft.exerciseId === null) {
      return;
    }

    const selectedExercise = this.exercises.find((exercise) => exercise.id === this.templateExerciseDraft.exerciseId);
    if (!selectedExercise) {
      return;
    }

    const exists = this.templateDraft.exercises.some((item) => item.exerciseId === selectedExercise.id);
    if (exists) {
      this.templateExerciseDraft = createEmptyRoutineTemplateExerciseDraft();
      return;
    }

    this.templateDraft.exercises = [
      ...this.templateDraft.exercises,
      { exerciseId: selectedExercise.id, exerciseName: selectedExercise.name }
    ];

    this.templateExerciseDraft = createEmptyRoutineTemplateExerciseDraft();
  }

  removeTemplateExercise(index: number): void {
    this.templateDraft.exercises = this.templateDraft.exercises.filter((_, itemIndex) => itemIndex !== index);
  }

  saveTemplate(): void {
    if (this.templateDraft.name.trim() === '' || this.templateDraft.exercises.length === 0) {
      return;
    }

    if (this.templateDraft.createdAt === '') {
      this.templateDraft.createdAt = this.todayIsoDate();
    }

    this.routinesService.upsertTemplate(this.templateDraft, this.editingTemplateId).subscribe({
      next: () => {
        this.cancelTemplateEdit();
        this.loadData();
      },
      error: () => {
        this.apiError = 'No se pudo guardar la rutina en el backend.';
      }
    });
  }

  editTemplate(template: RoutineTemplate): void {
    this.showTemplateForm = true;
    this.editingTemplateId = template.id;
    this.templateDraft = {
      name: template.name,
      createdAt: template.createdAt,
      exercises: template.exercises.map((item) => ({ ...item }))
    };
  }

  cancelTemplateEdit(): void {
    this.editingTemplateId = null;
    this.templateDraft = createEmptyRoutineTemplateDraft();
    this.templateDraft.createdAt = this.todayIsoDate();
    this.templateExerciseDraft = createEmptyRoutineTemplateExerciseDraft();
  }

  onAssignmentTemplateChange(): void {
    this.syncAssignmentPlansWithTemplate();
  }

  onAssignmentStudentChange(value: SearchableSelectValue): void {
    if (typeof value === 'number' && value > 0) {
      this.assignmentDraft.userId = value;
    } else if (typeof value === 'string' && /^\d+$/.test(value)) {
      this.assignmentDraft.userId = Number(value);
    } else {
      this.assignmentDraft.userId = 0;
    }

    if (this.historyStudentId !== null) {
      this.historyStudentId = this.assignmentDraft.userId > 0 ? this.assignmentDraft.userId : null;
    }
  }

  assignmentStudentValue(): SearchableSelectValue {
    return this.assignmentDraft.userId > 0 ? this.assignmentDraft.userId : null;
  }

  openSelectedStudentHistory(): void {
    if (this.assignmentDraft.userId <= 0) {
      return;
    }

    this.historyStudentId = this.assignmentDraft.userId;
  }

  closeSelectedStudentHistory(): void {
    this.historyStudentId = null;
  }

  saveAssignment(): void {
    if (
      this.assignmentDraft.userId <= 0 ||
      this.assignmentDraft.routineTemplateId <= 0 ||
      this.assignmentDraft.days.length === 0 ||
      this.assignmentDraft.startDate === '' ||
      this.assignmentDraft.endDate === '' ||
      this.assignmentDraft.exercisePlans.length === 0
    ) {
      return;
    }

    if (this.assignmentDraft.endDate < this.assignmentDraft.startDate) {
      return;
    }

    const normalizedPlans = this.assignmentDraft.exercisePlans.map((plan) => this.normalizeAssignmentPlan(plan));
    const payload: RoutineAssignmentDraft = { ...this.assignmentDraft, exercisePlans: normalizedPlans };

    this.routinesService.upsertAssignment(payload, this.editingAssignmentId).subscribe({
      next: () => {
        this.cancelAssignmentEdit();
        this.loadData();
        this.onAssignmentFilterChange();
      },
      error: () => {
        this.apiError = 'No se pudo guardar la asignacion en el backend.';
      }
    });
  }

  editAssignment(assignment: RoutineAssignment): void {
    this.showAssignmentForm = true;
    this.editingAssignmentId = assignment.id;
    this.assignmentDraft = {
      userId: assignment.userId,
      routineTemplateId: assignment.routineTemplateId,
      days: [...assignment.days],
      startDate: assignment.startDate,
      endDate: assignment.endDate,
      exercisePlans: assignment.exercisePlans.map((item) => ({ ...item }))
    };
    this.syncAssignmentPlansWithTemplate();
  }

  cancelAssignmentEdit(): void {
    this.editingAssignmentId = null;
    this.assignmentDraft = createEmptyRoutineAssignmentDraft();
    this.historyStudentId = null;
  }

  askDeleteTemplate(template: RoutineTemplate): void {
    this.deleteTemplateCandidate = template;
  }

  confirmDeleteTemplate(): void {
    if (!this.deleteTemplateCandidate) {
      return;
    }

    const templateId = this.deleteTemplateCandidate.id;
    this.routinesService.removeTemplate(templateId).subscribe({
      next: () => {
        this.deleteTemplateCandidate = null;

        if (this.editingTemplateId === templateId) {
          this.cancelTemplateEdit();
        }

        if (this.assignmentDraft.routineTemplateId === templateId) {
          this.assignmentDraft.routineTemplateId = 0;
          this.assignmentDraft.days = [];
          this.assignmentDraft.exercisePlans = [];
        }

        this.loadData();
      },
      error: () => {
        this.apiError = 'No se pudo eliminar la rutina en el backend.';
      }
    });
  }

  cancelDeleteTemplate(): void {
    this.deleteTemplateCandidate = null;
  }

  askDeleteAssignment(assignment: RoutineAssignment): void {
    this.deleteAssignmentCandidate = assignment;
  }

  confirmDeleteAssignment(): void {
    if (!this.deleteAssignmentCandidate) {
      return;
    }

    const assignmentId = this.deleteAssignmentCandidate.id;
    this.routinesService.removeAssignment(assignmentId).subscribe({
      next: () => {
        this.deleteAssignmentCandidate = null;

        if (this.editingAssignmentId === assignmentId) {
          this.cancelAssignmentEdit();
        }

        this.loadData();
      },
      error: () => {
        this.apiError = 'No se pudo eliminar la asignacion en el backend.';
      }
    });
  }

  cancelDeleteAssignment(): void {
    this.deleteAssignmentCandidate = null;
  }

  daysLabel(days: DayOfWeek[]): string {
    if (days.length === 0) {
      return '-';
    }

    return this.dayOptions
      .filter((day) => days.includes(day.value))
      .map((day) => day.label)
      .join(', ');
  }

  assignmentDaysLabel(assignment: RoutineAssignment): string {
    return this.daysLabel(assignment.days);
  }

  assignmentSummary(item: RoutineAssignmentExercise): string {
    if (item.prescriptionMode === 'fijo') {
      return `${item.sets}x${item.reps ?? 0} - ${item.weight ?? 0} kg`;
    }

    return `${item.sets}x${item.minReps ?? 0}-${item.maxReps ?? 0} - ${item.minWeight ?? 0}-${item.maxWeight ?? 0} kg`;
  }

  downloadAssignmentPdf(assignment: RoutineAssignment): void {
    this.routinesService.downloadAssignmentPdf(assignment.id).subscribe({
      next: (pdfBlob) => {
        const objectUrl = URL.createObjectURL(pdfBlob);
        const anchor = document.createElement('a');
        anchor.href = objectUrl;
        anchor.download = `rutina-asignacion-${assignment.id}.pdf`;
        anchor.click();
        URL.revokeObjectURL(objectUrl);
      },
      error: () => {
        this.apiError = 'No se pudo generar el PDF de la rutina.';
      }
    });
  }

  studentName(userId: number | null): string {
    return this.usersService.fullName(userId);
  }

  templateName(templateId: number): string {
    return this.routinesService.templateNameById(templateId);
  }

  templateUsageCount(templateId: number): number {
    return this.routineAssignments.filter((assignment) => assignment.routineTemplateId === templateId).length;
  }

  onTemplateFilterChange(): void {
    this.templateCurrentPage = 1;
    this.loadTemplatePage();
  }

  clearTemplateFilters(): void {
    this.templateFilters = {
      name: '',
      minExercises: 0,
      minStudents: 0
    };
    this.templateCurrentPage = 1;
    this.loadTemplatePage();
  }

  goToTemplatePage(page: number): void {
    if (page < 1 || page > this.templateTotalPages) {
      return;
    }

    this.templateCurrentPage = page;
    this.loadTemplatePage();
  }

  onTemplatePageSizeChange(): void {
    this.templateCurrentPage = 1;
    this.loadTemplatePage();
  }

  onAssignmentFilterChange(): void {
    this.assignmentCurrentPage = 1;
    this.loadAssignmentPage();
  }

  clearAssignmentFilters(): void {
    this.assignmentFilters = {
      student: '',
      templateId: 0
    };
    this.assignmentCurrentPage = 1;
    this.loadAssignmentPage();
  }

  goToAssignmentPage(page: number): void {
    if (page < 1 || page > this.assignmentTotalPages) {
      return;
    }

    this.assignmentCurrentPage = page;
    this.loadAssignmentPage();
  }

  onAssignmentPageSizeChange(): void {
    this.assignmentCurrentPage = 1;
    this.loadAssignmentPage();
  }

  private calculateAssignmentPlanWarning(): string | null {
    if (this.assignmentDraft.userId <= 0) {
      return null;
    }

    const student = this.usersService.getById(this.assignmentDraft.userId);
    if (!student || student.userType !== 'alumno') {
      return null;
    }

    if (student.planId === null) {
      return 'Warning: el alumno no tiene plan asignado.';
    }

    const plan = this.plansService.getById(student.planId);
    if (!plan) {
      return 'Warning: el alumno tiene un plan inexistente.';
    }

    const today = this.todayIsoDate();
    const activeAssignments = this.allRoutineAssignments.filter((assignment) => {
      if (assignment.userId !== student.id) {
        return false;
      }

      if (this.editingAssignmentId !== null && assignment.id === this.editingAssignmentId) {
        return false;
      }

      if (assignment.endDate < today) {
        return false;
      }

      if (this.assignmentDraft.startDate === '' || this.assignmentDraft.endDate === '') {
        return true;
      }

      return this.rangesOverlap(
        this.assignmentDraft.startDate,
        this.assignmentDraft.endDate,
        assignment.startDate,
        assignment.endDate
      );
    });

    const uniqueDays = new Set<DayOfWeek>();
    activeAssignments.forEach((assignment) => {
      assignment.days.forEach((day) => uniqueDays.add(day));
    });
    if (this.assignmentDraft.endDate === '' || this.assignmentDraft.endDate >= today) {
      this.assignmentDraft.days.forEach((day) => uniqueDays.add(day));
    }

    if (uniqueDays.size > plan.maxDaysPerWeek) {
      return `Warning: el alumno tiene el plan ${plan.name} con maximo de ${plan.maxDaysPerWeek} dias semanales.`;
    }

    return null;
  }

  private rangesOverlap(startA: string, endA: string, startB: string, endB: string): boolean {
    return startA <= endB && startB <= endA;
  }

  private syncAssignmentPlansWithTemplate(): void {
    const templateId = this.assignmentDraft.routineTemplateId;
    if (templateId <= 0) {
      this.assignmentDraft.exercisePlans = [];
      return;
    }

    const template = this.routinesService.getTemplateById(templateId);
    if (!template) {
      this.assignmentDraft.exercisePlans = [];
      return;
    }

    const existingByExerciseId = new Map(this.assignmentDraft.exercisePlans.map((item) => [item.exerciseId, item]));
    this.assignmentDraft.exercisePlans = template.exercises.map((exercise) => {
      const existing = existingByExerciseId.get(exercise.exerciseId);
      if (existing) {
        return { ...existing, exerciseName: exercise.exerciseName };
      }

      return this.createDefaultAssignmentPlan(exercise.exerciseId, exercise.exerciseName);
    });
  }

  private createDefaultAssignmentPlan(exerciseId: number, exerciseName: string): RoutineAssignmentExercise {
    return {
      exerciseId,
      exerciseName,
      sets: 4,
      prescriptionMode: 'fijo',
      reps: 10,
      weight: 0,
      minReps: null,
      maxReps: null,
      minWeight: null,
      maxWeight: null
    };
  }

  private normalizeAssignmentPlan(plan: RoutineAssignmentExercise): RoutineAssignmentExercise {
    const sets = ensureMinNumber(plan.sets, 1);

    if (plan.prescriptionMode === 'fijo') {
      return {
        ...plan,
        sets,
        reps: ensureMinNumber(plan.reps, 1),
        weight: ensureMinNumber(plan.weight, 0),
        minReps: null,
        maxReps: null,
        minWeight: null,
        maxWeight: null
      };
    }

    const minReps = ensureMinNumber(plan.minReps, 1);
    const maxReps = Math.max(minReps, ensureMinNumber(plan.maxReps, minReps));
    const minWeight = ensureMinNumber(plan.minWeight, 0);
    const maxWeight = Math.max(minWeight, ensureMinNumber(plan.maxWeight, minWeight));

    return {
      ...plan,
      sets,
      reps: null,
      weight: null,
      minReps,
      maxReps,
      minWeight,
      maxWeight
    };
  }

  private loadData(): void {
    this.isLoading = true;
    this.apiError = null;

    forkJoin({
      students: this.usersService.findAll('alumno'),
      plans: this.plansService.findAll(),
      exercises: this.exercisesService.findAll(),
      routineTemplates: this.routinesService.findTemplates(),
      routineAssignments: this.routinesService.findAssignments(),
      templatePage: this.routinesService.findTemplatesPage(
        this.toTemplatePageFilters(),
        this.templateCurrentPage - 1,
        this.templatePageSize
      ),
      assignmentPage: this.routinesService.findAssignmentsPage(
        this.toAssignmentPageFilters(),
        this.assignmentCurrentPage - 1,
        this.assignmentPageSize
      )
    }).subscribe({
      next: (result) => {
        this.students = result.students;
        this.exercises = result.exercises;
        this.routineTemplates = result.routineTemplates;
        this.allRoutineAssignments = result.routineAssignments;
        this.routineAssignments = this.routinesService.filterCurrentAssignments(result.routineAssignments);
        this.templateTablePage = result.templatePage;
        this.assignmentTablePage = result.assignmentPage;

        this.studentSelectItems = this.students.map((student) => ({
          id: student.id,
          firstName: student.firstName,
          lastName: student.lastName,
          dni: student.dni,
          fullName: `${student.firstName} ${student.lastName}`
        }));

        if (this.assignmentDraft.routineTemplateId > 0) {
          this.syncAssignmentPlansWithTemplate();
        }

        if (this.historyStudentId !== null && !this.students.some((student) => student.id === this.historyStudentId)) {
          this.historyStudentId = null;
        }

        this.isLoading = false;
      },
      error: () => {
        this.apiError = 'No se pudieron obtener rutinas del backend.';
        this.students = this.usersService.getByType('alumno');
        this.exercises = this.exercisesService.getAll();
        this.routineTemplates = this.routinesService.getTemplates();
        this.allRoutineAssignments = this.routinesService.getAssignments();
        this.routineAssignments = this.routinesService.filterCurrentAssignments(this.allRoutineAssignments);
        this.templateTablePage = createEmptyPageResponse<RoutineTemplate>();
        this.assignmentTablePage = createEmptyPageResponse<RoutineAssignment>();
        this.studentSelectItems = this.students.map((student) => ({
          id: student.id,
          firstName: student.firstName,
          lastName: student.lastName,
          dni: student.dni,
          fullName: `${student.firstName} ${student.lastName}`
        }));
        if (this.historyStudentId !== null && !this.students.some((student) => student.id === this.historyStudentId)) {
          this.historyStudentId = null;
        }
        this.isLoading = false;
      }
    });
  }

  private loadTemplatePage(): void {
    this.isLoading = true;
    this.apiError = null;

    this.routinesService
      .findTemplatesPage(this.toTemplatePageFilters(), this.templateCurrentPage - 1, this.templatePageSize)
      .subscribe({
        next: (pageResult) => {
          this.templateTablePage = pageResult;
          this.isLoading = false;
        },
        error: () => {
          this.apiError = 'No se pudieron obtener plantillas de rutina del backend.';
          this.templateTablePage = createEmptyPageResponse<RoutineTemplate>();
          this.isLoading = false;
        }
      });
  }

  private loadAssignmentPage(): void {
    this.isLoading = true;
    this.apiError = null;

    this.routinesService
      .findAssignmentsPage(this.toAssignmentPageFilters(), this.assignmentCurrentPage - 1, this.assignmentPageSize)
      .subscribe({
        next: (pageResult) => {
          this.assignmentTablePage = pageResult;
          this.isLoading = false;
        },
        error: () => {
          this.apiError = 'No se pudieron obtener asignaciones de rutina del backend.';
          this.assignmentTablePage = createEmptyPageResponse<RoutineAssignment>();
          this.isLoading = false;
        }
      });
  }

  private toTemplatePageFilters(): RoutineTemplatePageFilters {
    return {
      name: this.templateFilters.name,
      minExercises: this.templateFilters.minExercises,
      minStudents: this.templateFilters.minStudents
    };
  }

  private toAssignmentPageFilters(): RoutineAssignmentPageFilters {
    return {
      student: this.assignmentFilters.student,
      templateId: this.assignmentFilters.templateId,
      currentOnly: true
    };
  }

  private todayIsoDate(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
