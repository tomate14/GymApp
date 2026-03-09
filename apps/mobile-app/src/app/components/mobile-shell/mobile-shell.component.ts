import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import {
  IonBadge,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonMenu,
  IonMenuButton,
  IonMenuToggle,
  IonTitle,
  IonToolbar
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  barbellOutline,
  cardOutline,
  calendarOutline,
  chevronForwardOutline,
  closeCircleOutline,
  logOutOutline,
  menuOutline,
  personCircleOutline,
  stopwatchOutline
} from 'ionicons/icons';
import { filter } from 'rxjs';
import { AuthService } from '../../services/auth.service';

type MenuItem = {
  label: string;
  path: string;
  icon: string;
};

@Component({
  selector: 'app-mobile-shell',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    IonMenu,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonList,
    IonItem,
    IonLabel,
    IonIcon,
    IonMenuToggle,
    IonButtons,
    IonMenuButton,
    IonButton,
    IonBadge
  ],
  templateUrl: './mobile-shell.component.html',
  styleUrl: './mobile-shell.component.css'
})
export class MobileShellComponent {
  readonly menuItems: MenuItem[] = [
    { label: 'Datos personales', path: '/datos-personales', icon: 'person-circle-outline' },
    { label: 'Historial de pagos', path: '/historial-de-pagos', icon: 'card-outline' },
    { label: 'Mis rutinas', path: '/mis-rutinas', icon: 'barbell-outline' },
    { label: 'Pesos maximos', path: '/pesos-maximos', icon: 'stopwatch-outline' },
    { label: 'Clases', path: '/clases', icon: 'calendar-outline' }
  ];

  sectionTitle = 'Datos personales';

  constructor(
    private readonly router: Router,
    private readonly authService: AuthService
  ) {
    addIcons({
      personCircleOutline,
      cardOutline,
      barbellOutline,
      stopwatchOutline,
      calendarOutline,
      chevronForwardOutline,
      logOutOutline,
      menuOutline,
      closeCircleOutline
    });

    this.updateSectionTitle();
    this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => this.updateSectionTitle());
  }

  logout(): void {
    this.authService.logout();
  }

  private updateSectionTitle(): void {
    const current = this.menuItems.find((item) => this.router.url.startsWith(item.path));
    this.sectionTitle = current?.label ?? 'Fitexa Mobile';
  }
}
