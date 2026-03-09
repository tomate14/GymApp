import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Exercise } from '../../class/exercise.model';

@Component({
  selector: 'app-custom-routine-popup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './custom-routine-popup.component.html',
  styleUrl: './custom-routine-popup.component.css'
})
export class CustomRoutinePopupComponent implements OnChanges {
  @Input() visible = false;
  @Input() exercises: Exercise[] = [];
  @Input() selectedExerciseIds: number[] = [];

  @Output() closed = new EventEmitter<void>();
  @Output() confirmed = new EventEmitter<number[]>();

  searchTerm = '';
  draftSelection = new Set<number>();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] || changes['selectedExerciseIds']) {
      this.searchTerm = '';
      this.draftSelection = new Set(this.selectedExerciseIds);
    }
  }

  get filteredExercises(): Exercise[] {
    const query = this.searchTerm.trim().toLowerCase();
    if (query === '') {
      return [...this.exercises].sort((current, next) => current.name.localeCompare(next.name));
    }

    return this.exercises
      .filter((exercise) => exercise.name.toLowerCase().includes(query))
      .sort((current, next) => current.name.localeCompare(next.name));
  }

  isSelected(exerciseId: number): boolean {
    return this.draftSelection.has(exerciseId);
  }

  toggleExercise(exerciseId: number, enabled: boolean): void {
    if (enabled) {
      this.draftSelection.add(exerciseId);
      return;
    }

    this.draftSelection.delete(exerciseId);
  }

  confirm(): void {
    this.confirmed.emit(Array.from(this.draftSelection));
  }
}
