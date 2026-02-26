import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
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
import { RoutinesService } from '../../services/routines.service';
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
  routineAssignments: RoutineAssignment[] = [];

  templateDraft: RoutineTemplateDraft = createEmptyRoutineTemplateDraft();
  templateExerciseDraft: RoutineTemplateExerciseDraft = createEmptyRoutineTemplateExerciseDraft();
  assignmentDraft: RoutineAssignmentDraft = createEmptyRoutineAssignmentDraft();

  editingTemplateId: number | null = null;
  editingAssignmentId: number | null = null;

  showTemplateForm = false;
  showAssignmentForm = false;

  deleteTemplateCandidate: RoutineTemplate | null = null;
  deleteAssignmentCandidate: RoutineAssignment | null = null;

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
    return this.routineTemplates.filter((template) => {
      const matchesName =
        this.templateFilters.name.trim() === '' ||
        template.name.toLowerCase().includes(this.templateFilters.name.trim().toLowerCase());
      const matchesExerciseCount = template.exercises.length >= this.templateFilters.minExercises;
      const matchesStudentCount = this.templateUsageCount(template.id) >= this.templateFilters.minStudents;
      return matchesName && matchesExerciseCount && matchesStudentCount;
    });
  }

  get filteredAssignments(): RoutineAssignment[] {
    return this.routineAssignments.filter((assignment) => {
      const studentName = this.usersService.fullName(assignment.userId).toLowerCase();
      const matchesStudent =
        this.assignmentFilters.student.trim() === '' ||
        studentName.includes(this.assignmentFilters.student.trim().toLowerCase());
      const matchesTemplate =
        this.assignmentFilters.templateId === 0 || assignment.routineTemplateId === this.assignmentFilters.templateId;
      return matchesStudent && matchesTemplate;
    });
  }

  get templateTotalPages(): number {
    return Math.max(1, Math.ceil(this.filteredTemplates.length / this.templatePageSize));
  }

  get assignmentTotalPages(): number {
    return Math.max(1, Math.ceil(this.filteredAssignments.length / this.assignmentPageSize));
  }

  get paginatedTemplates(): RoutineTemplate[] {
    const safePage = Math.min(this.templateCurrentPage, this.templateTotalPages);
    const start = (safePage - 1) * this.templatePageSize;
    return this.filteredTemplates.slice(start, start + this.templatePageSize);
  }

  get paginatedAssignments(): RoutineAssignment[] {
    const safePage = Math.min(this.assignmentCurrentPage, this.assignmentTotalPages);
    const start = (safePage - 1) * this.assignmentPageSize;
    return this.filteredAssignments.slice(start, start + this.assignmentPageSize);
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

    this.routinesService.upsertTemplate(this.templateDraft, this.editingTemplateId);
    this.cancelTemplateEdit();
    this.loadData();
    this.onTemplateFilterChange();
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
      return;
    }

    if (typeof value === 'string' && /^\d+$/.test(value)) {
      this.assignmentDraft.userId = Number(value);
      return;
    }

    this.assignmentDraft.userId = 0;
  }

  assignmentStudentValue(): SearchableSelectValue {
    return this.assignmentDraft.userId > 0 ? this.assignmentDraft.userId : null;
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

    this.routinesService.upsertAssignment(payload, this.editingAssignmentId);
    this.cancelAssignmentEdit();
    this.loadData();
    this.onAssignmentFilterChange();
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
  }

  askDeleteTemplate(template: RoutineTemplate): void {
    this.deleteTemplateCandidate = template;
  }

  confirmDeleteTemplate(): void {
    if (!this.deleteTemplateCandidate) {
      return;
    }

    const templateId = this.deleteTemplateCandidate.id;
    this.routinesService.removeTemplate(templateId);
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
    this.onTemplateFilterChange();
    this.onAssignmentFilterChange();
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
    this.routinesService.removeAssignment(assignmentId);
    this.deleteAssignmentCandidate = null;

    if (this.editingAssignmentId === assignmentId) {
      this.cancelAssignmentEdit();
    }

    this.loadData();
    this.onAssignmentFilterChange();
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

  studentName(userId: number): string {
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
  }

  clearTemplateFilters(): void {
    this.templateFilters = {
      name: '',
      minExercises: 0,
      minStudents: 0
    };
    this.templateCurrentPage = 1;
  }

  goToTemplatePage(page: number): void {
    if (page < 1 || page > this.templateTotalPages) {
      return;
    }

    this.templateCurrentPage = page;
  }

  onTemplatePageSizeChange(): void {
    this.templateCurrentPage = 1;
  }

  onAssignmentFilterChange(): void {
    this.assignmentCurrentPage = 1;
  }

  clearAssignmentFilters(): void {
    this.assignmentFilters = {
      student: '',
      templateId: 0
    };
    this.assignmentCurrentPage = 1;
  }

  goToAssignmentPage(page: number): void {
    if (page < 1 || page > this.assignmentTotalPages) {
      return;
    }

    this.assignmentCurrentPage = page;
  }

  onAssignmentPageSizeChange(): void {
    this.assignmentCurrentPage = 1;
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

    const activeAssignments = this.routineAssignments.filter((assignment) => {
      if (assignment.userId !== student.id) {
        return false;
      }

      if (this.editingAssignmentId !== null && assignment.id === this.editingAssignmentId) {
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
    this.assignmentDraft.days.forEach((day) => uniqueDays.add(day));

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
    this.students = this.usersService.getByType('alumno');
    this.studentSelectItems = this.students.map((student) => ({
      id: student.id,
      firstName: student.firstName,
      lastName: student.lastName,
      dni: student.dni,
      fullName: `${student.firstName} ${student.lastName}`
    }));

    this.exercises = this.exercisesService.getAll();
    this.routineTemplates = this.routinesService.getTemplates();
    this.routineAssignments = this.routinesService.getAssignments();

    if (this.assignmentDraft.routineTemplateId > 0) {
      this.syncAssignmentPlansWithTemplate();
    }
  }

  private todayIsoDate(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
