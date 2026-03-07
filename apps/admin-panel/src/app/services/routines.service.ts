import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { PageResponse } from '../class/page.model';
import {
  DayOfWeek,
  RoutineAssignment,
  RoutineAssignmentDraft,
  RoutineAssignmentExercise,
  RoutineTemplate,
  RoutineTemplateDraft
} from '../class/routine.model';
import { API_BASE_URL } from '../utils/api.util';

type RoutineTemplateExerciseApiDto = {
  exerciseId: number;
  exerciseName: string;
};

type RoutineTemplateApiDto = {
  id: number;
  name: string;
  createdAt: string;
  exercises: RoutineTemplateExerciseApiDto[];
};

type RoutineTemplateApiRequest = {
  name: string;
  createdAt: string;
  exerciseIds: number[];
};

type RoutineAssignmentExerciseApiDto = {
  exerciseId: number;
  exerciseName: string;
  sets: number;
  prescriptionMode: RoutineAssignmentExercise['prescriptionMode'];
  reps: number | null;
  weight: number | null;
  minReps: number | null;
  maxReps: number | null;
  minWeight: number | null;
  maxWeight: number | null;
};

type RoutineAssignmentApiDto = {
  id: number;
  userId: number;
  routineTemplateId: number;
  days: DayOfWeek[];
  startDate: string;
  endDate: string;
  exercisePlans: RoutineAssignmentExerciseApiDto[];
};

type RoutineAssignmentApiRequest = {
  userId: number;
  routineTemplateId: number;
  days: DayOfWeek[];
  startDate: string;
  endDate: string;
  exercisePlans: Array<{
    exerciseId: number;
    sets: number;
    prescriptionMode: RoutineAssignmentExercise['prescriptionMode'];
    reps: number | null;
    weight: number | null;
    minReps: number | null;
    maxReps: number | null;
    minWeight: number | null;
    maxWeight: number | null;
  }>;
};

type RoutineAssignmentSaveResultApiDto = {
  assignment: RoutineAssignmentApiDto;
  warning: string | null;
};

type PageApiDto<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type RoutineTemplatePageFilters = {
  name?: string;
  minExercises?: number;
  minStudents?: number;
};

export type RoutineAssignmentPageFilters = {
  student?: string;
  templateId?: number | 0;
  currentOnly?: boolean;
};

export interface RoutineAssignmentSaveResult {
  assignment: RoutineAssignment;
  warning: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class RoutinesService {
  private readonly templateEndpoint = `${API_BASE_URL}/routine-templates`;
  private readonly assignmentEndpoint = `${API_BASE_URL}/routine-assignments`;

  private routineTemplates: RoutineTemplate[] = [];
  private routineAssignments: RoutineAssignment[] = [];

  constructor(private readonly http: HttpClient) {}

  findTemplates(): Observable<RoutineTemplate[]> {
    return this.http.get<RoutineTemplateApiDto[]>(this.templateEndpoint).pipe(
      map((items) => items.map((item) => this.mapTemplateFromApi(item))),
      tap((items) => this.setTemplates(items))
    );
  }

  findAssignments(): Observable<RoutineAssignment[]> {
    return this.http.get<RoutineAssignmentApiDto[]>(this.assignmentEndpoint).pipe(
      map((items) => items.map((item) => this.mapAssignmentFromApi(item))),
      tap((items) => this.setAssignments(items))
    );
  }

  findTemplatesPage(
    filters: RoutineTemplatePageFilters,
    page: number,
    size: number
  ): Observable<PageResponse<RoutineTemplate>> {
    let params = new HttpParams()
      .set('page', String(Math.max(0, page)))
      .set('size', String(size));

    if (filters.name && filters.name.trim() !== '') {
      params = params.set('name', filters.name.trim());
    }
    if (typeof filters.minExercises === 'number' && filters.minExercises > 0) {
      params = params.set('minExercises', String(filters.minExercises));
    }
    if (typeof filters.minStudents === 'number' && filters.minStudents > 0) {
      params = params.set('minStudents', String(filters.minStudents));
    }

    return this.http.get<PageApiDto<RoutineTemplateApiDto>>(`${this.templateEndpoint}/page`, { params }).pipe(
      map((response) => this.mapTemplatePageFromApi(response))
    );
  }

  findAssignmentsPage(
    filters: RoutineAssignmentPageFilters,
    page: number,
    size: number
  ): Observable<PageResponse<RoutineAssignment>> {
    let params = new HttpParams()
      .set('page', String(Math.max(0, page)))
      .set('size', String(size))
      .set('currentOnly', String(filters.currentOnly ?? true));

    if (filters.student && filters.student.trim() !== '') {
      params = params.set('student', filters.student.trim());
    }
    if (filters.templateId && filters.templateId > 0) {
      params = params.set('templateId', String(filters.templateId));
    }

    return this.http.get<PageApiDto<RoutineAssignmentApiDto>>(`${this.assignmentEndpoint}/page`, { params }).pipe(
      map((response) => this.mapAssignmentPageFromApi(response))
    );
  }

  filterCurrentAssignments(assignments: RoutineAssignment[], referenceDate: string = this.todayIsoDate()): RoutineAssignment[] {
    return assignments.filter((assignment) => this.isCurrentAssignment(assignment, referenceDate));
  }

  getTemplates(): RoutineTemplate[] {
    return this.routineTemplates.map((template) => this.cloneTemplate(template));
  }

  setTemplates(templates: RoutineTemplate[]): void {
    this.routineTemplates = templates.map((item) => this.cloneTemplate(item));
  }

  getAssignments(): RoutineAssignment[] {
    return this.routineAssignments.map((assignment) => this.cloneAssignment(assignment));
  }

  setAssignments(assignments: RoutineAssignment[]): void {
    this.routineAssignments = assignments.map((item) => this.cloneAssignment(item));
  }

  getTemplateById(templateId: number): RoutineTemplate | null {
    const template = this.routineTemplates.find((item) => item.id === templateId);
    return template ? this.cloneTemplate(template) : null;
  }

  upsertTemplate(draft: RoutineTemplateDraft, editingId: number | null): Observable<RoutineTemplate> {
    const payload: RoutineTemplateApiRequest = {
      name: draft.name,
      createdAt: draft.createdAt,
      exerciseIds: draft.exercises.map((item) => item.exerciseId)
    };

    const request$ =
      editingId === null
        ? this.http.post<RoutineTemplateApiDto>(this.templateEndpoint, payload)
        : this.http.put<RoutineTemplateApiDto>(`${this.templateEndpoint}/${editingId}`, payload);

    return request$.pipe(
      map((item) => this.mapTemplateFromApi(item)),
      tap((saved) => this.upsertTemplateInCache(saved))
    );
  }

  upsertAssignment(draft: RoutineAssignmentDraft, editingId: number | null): Observable<RoutineAssignmentSaveResult> {
    const payload: RoutineAssignmentApiRequest = {
      userId: draft.userId,
      routineTemplateId: draft.routineTemplateId,
      days: [...draft.days],
      startDate: draft.startDate,
      endDate: draft.endDate,
      exercisePlans: draft.exercisePlans.map((item) => ({
        exerciseId: item.exerciseId,
        sets: item.sets,
        prescriptionMode: item.prescriptionMode,
        reps: item.reps,
        weight: item.weight,
        minReps: item.minReps,
        maxReps: item.maxReps,
        minWeight: item.minWeight,
        maxWeight: item.maxWeight
      }))
    };

    const request$ =
      editingId === null
        ? this.http.post<RoutineAssignmentSaveResultApiDto>(this.assignmentEndpoint, payload)
        : this.http.put<RoutineAssignmentSaveResultApiDto>(`${this.assignmentEndpoint}/${editingId}`, payload);

    return request$.pipe(
      map((result) => ({
        assignment: this.mapAssignmentFromApi(result.assignment),
        warning: result.warning
      })),
      tap((result) => this.upsertAssignmentInCache(result.assignment))
    );
  }

  removeTemplate(templateId: number): Observable<void> {
    return this.http.delete<void>(`${this.templateEndpoint}/${templateId}`).pipe(
      tap(() => {
        this.routineTemplates = this.routineTemplates.filter((template) => template.id !== templateId);
        this.routineAssignments = this.routineAssignments.filter((assignment) => assignment.routineTemplateId !== templateId);
      })
    );
  }

  removeAssignment(assignmentId: number): Observable<void> {
    return this.http.delete<void>(`${this.assignmentEndpoint}/${assignmentId}`).pipe(
      tap(() => {
        this.routineAssignments = this.routineAssignments.filter((assignment) => assignment.id !== assignmentId);
      })
    );
  }

  downloadAssignmentPdf(assignmentId: number): Observable<Blob> {
    return this.http.get(`${this.assignmentEndpoint}/${assignmentId}/pdf`, { responseType: 'blob' });
  }

  clearUserReferences(userId: number): void {
    this.routineAssignments = this.routineAssignments.filter((assignment) => assignment.userId !== userId);
  }

  removeExerciseFromRoutines(exerciseId: number): void {
    this.routineTemplates = this.routineTemplates.map((template) => ({
      ...template,
      exercises: template.exercises.filter((item) => item.exerciseId !== exerciseId)
    }));

    this.routineAssignments = this.routineAssignments.map((assignment) => ({
      ...assignment,
      exercisePlans: assignment.exercisePlans.filter((item) => item.exerciseId !== exerciseId)
    }));
  }

  renameExerciseReference(exerciseId: number, exerciseName: string): void {
    this.routineTemplates = this.routineTemplates.map((template) => ({
      ...template,
      exercises: template.exercises.map((item) => (item.exerciseId === exerciseId ? { ...item, exerciseName } : item))
    }));

    this.routineAssignments = this.routineAssignments.map((assignment) => ({
      ...assignment,
      exercisePlans: assignment.exercisePlans.map((item) =>
        item.exerciseId === exerciseId ? { ...item, exerciseName } : item
      )
    }));
  }

  templateNameById(templateId: number): string {
    return this.routineTemplates.find((template) => template.id === templateId)?.name ?? 'Sin rutina';
  }

  private upsertTemplateInCache(payload: RoutineTemplate): void {
    const exists = this.routineTemplates.some((item) => item.id === payload.id);
    if (!exists) {
      this.routineTemplates = [this.cloneTemplate(payload), ...this.routineTemplates];
      return;
    }

    this.routineTemplates = this.routineTemplates.map((item) =>
      item.id === payload.id ? this.cloneTemplate(payload) : item
    );
  }

  private upsertAssignmentInCache(payload: RoutineAssignment): void {
    const exists = this.routineAssignments.some((item) => item.id === payload.id);
    if (!exists) {
      this.routineAssignments = [this.cloneAssignment(payload), ...this.routineAssignments];
      return;
    }

    this.routineAssignments = this.routineAssignments.map((item) =>
      item.id === payload.id ? this.cloneAssignment(payload) : item
    );
  }

  private mapTemplateFromApi(dto: RoutineTemplateApiDto): RoutineTemplate {
    return {
      id: Number(dto.id),
      name: dto.name,
      createdAt: dto.createdAt,
      exercises: dto.exercises.map((item) => ({
        exerciseId: Number(item.exerciseId),
        exerciseName: item.exerciseName
      }))
    };
  }

  private mapAssignmentFromApi(dto: RoutineAssignmentApiDto): RoutineAssignment {
    return {
      id: Number(dto.id),
      userId: Number(dto.userId),
      routineTemplateId: Number(dto.routineTemplateId),
      days: dto.days.map((day) => day),
      startDate: dto.startDate,
      endDate: dto.endDate,
      exercisePlans: dto.exercisePlans.map((item) => ({
        exerciseId: Number(item.exerciseId),
        exerciseName: item.exerciseName,
        sets: Number(item.sets),
        prescriptionMode: item.prescriptionMode,
        reps: item.reps === null ? null : Number(item.reps),
        weight: item.weight === null ? null : Number(item.weight),
        minReps: item.minReps === null ? null : Number(item.minReps),
        maxReps: item.maxReps === null ? null : Number(item.maxReps),
        minWeight: item.minWeight === null ? null : Number(item.minWeight),
        maxWeight: item.maxWeight === null ? null : Number(item.maxWeight)
      }))
    };
  }

  private mapTemplatePageFromApi(dto: PageApiDto<RoutineTemplateApiDto>): PageResponse<RoutineTemplate> {
    return {
      content: dto.content.map((item) => this.mapTemplateFromApi(item)),
      page: Number(dto.page),
      size: Number(dto.size),
      totalElements: Number(dto.totalElements),
      totalPages: Number(dto.totalPages)
    };
  }

  private mapAssignmentPageFromApi(dto: PageApiDto<RoutineAssignmentApiDto>): PageResponse<RoutineAssignment> {
    return {
      content: dto.content.map((item) => this.mapAssignmentFromApi(item)),
      page: Number(dto.page),
      size: Number(dto.size),
      totalElements: Number(dto.totalElements),
      totalPages: Number(dto.totalPages)
    };
  }

  private cloneTemplate(template: RoutineTemplate): RoutineTemplate {
    return {
      ...template,
      exercises: template.exercises.map((item) => ({ ...item }))
    };
  }

  private cloneAssignment(assignment: RoutineAssignment): RoutineAssignment {
    return {
      ...assignment,
      days: [...assignment.days],
      exercisePlans: assignment.exercisePlans.map((item) => ({ ...item }))
    };
  }

  private isCurrentAssignment(assignment: RoutineAssignment, referenceDate: string): boolean {
    return assignment.startDate <= referenceDate && assignment.endDate >= referenceDate;
  }

  private todayIsoDate(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
