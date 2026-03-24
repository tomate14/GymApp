import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  GymClassAttendance,
  GymClassAttendanceStudent,
  GymClassAttendanceUpdateItem
} from '../../class/gym-class-attendance.model';

@Component({
  selector: 'app-class-attendance-popup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './class-attendance-popup.component.html',
  styleUrl: './class-attendance-popup.component.css'
})
export class ClassAttendancePopupComponent implements OnChanges {
  @Input() visible = false;
  @Input() attendance: GymClassAttendance | null = null;
  @Input() isLoading = false;
  @Input() isSaving = false;
  @Input() error: string | null = null;

  @Output() readonly closed = new EventEmitter<void>();
  @Output() readonly saved = new EventEmitter<GymClassAttendanceUpdateItem[]>();

  editableStudents: Array<GymClassAttendanceStudent & { present: boolean }> = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['attendance']) {
      this.editableStudents = (this.attendance?.students ?? []).map((student) => ({
        ...student,
        present: student.attendancePresent === true
      }));
    }
  }

  save(): void {
    this.saved.emit(
      this.editableStudents.map((student) => ({
        subscriptionId: student.subscriptionId,
        attendancePresent: student.present
      }))
    );
  }
}
