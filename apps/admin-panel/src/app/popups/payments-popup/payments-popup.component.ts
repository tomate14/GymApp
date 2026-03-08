import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { UserPaymentMovement } from '../../class/user-payment-movement.model';
import { User } from '../../class/user.model';

@Component({
  selector: 'app-payments-popup',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payments-popup.component.html',
  styleUrl: './payments-popup.component.css'
})
export class PaymentsPopupComponent {
  @Input() visible = false;
  @Input() user: User | null = null;
  @Input() movements: UserPaymentMovement[] = [];
  @Input() isLoading = false;
  @Input() error: string | null = null;

  @Output() closed = new EventEmitter<void>();
}
