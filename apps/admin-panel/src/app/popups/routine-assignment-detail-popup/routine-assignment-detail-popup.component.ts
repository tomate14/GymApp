import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DayOfWeek, RoutineAssignment, RoutineAssignmentExercise } from '../../class/routine.model';

@Component({
  selector: 'app-routine-assignment-detail-popup',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './routine-assignment-detail-popup.component.html',
  styleUrl: './routine-assignment-detail-popup.component.css'
})
export class RoutineAssignmentDetailPopupComponent {
  @Input() visible = false;
  @Input() assignment: RoutineAssignment | null = null;
  @Input() studentName = '-';
  @Input() routineName = '-';
  @Output() readonly closed = new EventEmitter<void>();

  readonly dayLabels: Record<DayOfWeek, string> = {
    lunes: 'Lunes',
    martes: 'Martes',
    miercoles: 'Miercoles',
    jueves: 'Jueves',
    viernes: 'Viernes',
    sabado: 'Sabado',
    domingo: 'Domingo'
  };

  close(): void {
    this.closed.emit();
  }

  daysLabel(days: DayOfWeek[]): string {
    if (days.length === 0) {
      return '-';
    }
    return days.map((day) => this.dayLabels[day] ?? day).join(', ');
  }

  modeLabel(mode: RoutineAssignmentExercise['prescriptionMode']): string {
    return mode === 'dinamico' ? 'Dinamico' : 'Fijo';
  }

  repsLabel(item: RoutineAssignmentExercise): string {
    if (item.prescriptionMode === 'dinamico') {
      return `${item.minReps ?? 0} - ${item.maxReps ?? 0}`;
    }
    return String(item.reps ?? 0);
  }

  weightLabel(item: RoutineAssignmentExercise): string {
    if (item.prescriptionMode === 'dinamico') {
      return `${item.minWeight ?? 0} - ${item.maxWeight ?? 0}`;
    }
    return String(item.weight ?? 0);
  }
}
