import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import {
  IonApp,
  IonButton,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardSubtitle,
  IonCardTitle,
  IonChip,
  IonContent,
  IonHeader,
  IonIcon,
  IonProgressBar,
  IonTitle,
  IonToolbar
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { barbellOutline, flashOutline, trophyOutline } from 'ionicons/icons';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    IonApp,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonCardSubtitle,
    IonCardContent,
    IonButton,
    IonChip,
    IonIcon,
    IonProgressBar
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  readonly routines = [
    { day: 'Lunes', name: 'Piernas + Core', status: 'Completado' },
    { day: 'Miercoles', name: 'Espalda + Biceps', status: 'Pendiente' },
    { day: 'Viernes', name: 'Pecho + Triceps', status: 'Pendiente' }
  ];

  constructor() {
    addIcons({ barbellOutline, flashOutline, trophyOutline });
  }
}
