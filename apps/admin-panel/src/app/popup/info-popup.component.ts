import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-info-popup',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './info-popup.component.html',
  styleUrl: './info-popup.component.css'
})
export class InfoPopupComponent {
  @Input() visible = false;
  @Input() title = 'Como se calcula';
  @Input() summary = '';
  @Input() items: string[] = [];

  @Output() closed = new EventEmitter<void>();
}
