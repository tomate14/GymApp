import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { IonButton, IonCard, IonCardContent, IonIcon, IonSpinner } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { cardOutline, downloadOutline, receiptOutline } from 'ionicons/icons';
import { MobilePaymentMovement } from '../../models/mobile.model';
import { MobileAppService } from '../../services/mobile-app.service';

type PaymentCard = MobilePaymentMovement & {
  downloading: boolean;
};

@Component({
  selector: 'app-payments-page',
  standalone: true,
  imports: [CommonModule, IonCard, IonCardContent, IonButton, IonIcon, IonSpinner],
  templateUrl: './payments.page.html',
  styleUrl: './payments.page.css'
})
export class PaymentsPageComponent implements OnInit {
  payments: PaymentCard[] = [];
  errorMessage: string | null = null;
  isLoading = true;

  constructor(private readonly mobileAppService: MobileAppService) {
    addIcons({ receiptOutline, cardOutline, downloadOutline });
  }

  ngOnInit(): void {
    this.mobileAppService.getPaymentMovements().subscribe({
      next: (payments) => {
        this.payments = payments.map((item) => ({ ...item, downloading: false }));
        this.isLoading = false;
      },
      error: (err: { error?: { message?: string } }) => {
        this.errorMessage = err.error?.message ?? 'No se pudo cargar tu historial de pagos.';
        this.isLoading = false;
      }
    });
  }

  downloadReceipt(payment: PaymentCard): void {
    payment.downloading = true;
    this.errorMessage = null;

    this.mobileAppService.downloadPaymentMovementPdf(payment.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `comprobante-pago-${payment.id}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        payment.downloading = false;
      },
      error: (err: { error?: { message?: string } }) => {
        this.errorMessage = err.error?.message ?? 'No se pudo descargar el comprobante.';
        payment.downloading = false;
      }
    });
  }

  formatAmount(value: number): string {
    return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 2 }).format(value);
  }

  formatDate(value: string): string {
    return new Intl.DateTimeFormat('es-AR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(new Date(value));
  }
}
