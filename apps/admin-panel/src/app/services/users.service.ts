import { Injectable } from '@angular/core';
import { User, UserDraft, UserType } from '../class/user.model';
import { nextNumericId } from '../utils/id.util';

@Injectable({
  providedIn: 'root'
})
export class UsersService {
  private users: User[] = [
    {
      id: 1,
      firstName: 'Lucia',
      lastName: 'Gimenez',
      dni: '32450987',
      address: 'Belgrano 145',
      phone: '3415011002',
      email: 'lucia.gimenez@gymapp.com',
      birthDate: '1992-09-05',
      userType: 'profesor',
      planId: null,
      registrationDate: '2025-11-02',
      renewalDate: '2026-03-10',
      paymentStatus: 'al-dia'
    },
    {
      id: 2,
      firstName: 'Matias',
      lastName: 'Lopez',
      dni: '38221009',
      address: 'Brown 210',
      phone: '3415667733',
      email: 'matias.lopez@email.com',
      birthDate: '1998-01-20',
      userType: 'alumno',
      planId: 2,
      registrationDate: '2025-10-18',
      renewalDate: '2026-02-20',
      paymentStatus: 'adeuda'
    },
    {
      id: 3,
      firstName: 'Diego',
      lastName: 'Ferrero',
      dni: '29877441',
      address: 'San Luis 932',
      phone: '3416120044',
      email: 'diego.ferrero@gymapp.com',
      birthDate: '1987-04-14',
      userType: 'profesor',
      planId: null,
      registrationDate: '2025-09-10',
      renewalDate: '2026-03-05',
      paymentStatus: 'al-dia'
    },
    {
      id: 4,
      firstName: 'Maria',
      lastName: 'Torres',
      dni: '40155672',
      address: 'Italia 455',
      phone: '3415233344',
      email: 'maria.torres@email.com',
      birthDate: '2000-11-02',
      userType: 'alumno',
      planId: 1,
      registrationDate: '2025-12-03',
      renewalDate: '2026-03-03',
      paymentStatus: 'al-dia'
    },
    {
      id: 5,
      firstName: 'Agustina',
      lastName: 'Ruiz',
      dni: '31244876',
      address: 'Mitre 1880',
      phone: '3415772200',
      email: 'agustina.ruiz@gymapp.com',
      birthDate: '1990-06-25',
      userType: 'profesor',
      planId: null,
      registrationDate: '2025-08-15',
      renewalDate: '2026-02-28',
      paymentStatus: 'al-dia'
    },
    {
      id: 6,
      firstName: 'Juan',
      lastName: 'Perez',
      dni: '38992011',
      address: 'Mendoza 640',
      phone: '3415448877',
      email: 'juan.perez@email.com',
      birthDate: '1997-02-18',
      userType: 'alumno',
      planId: 1,
      registrationDate: '2025-11-21',
      renewalDate: '2026-03-12',
      paymentStatus: 'adeuda'
    },
    {
      id: 7,
      firstName: 'Camila',
      lastName: 'Navarro',
      dni: '41588231',
      address: 'Roca 310',
      phone: '3415099877',
      email: 'camila.navarro@email.com',
      birthDate: '2001-07-09',
      userType: 'alumno',
      planId: 3,
      registrationDate: '2026-01-07',
      renewalDate: '2026-03-20',
      paymentStatus: 'al-dia'
    }
  ];

  getAll(): User[] {
    return [...this.users];
  }

  getByType(userType: UserType): User[] {
    return this.users.filter((user) => user.userType === userType);
  }

  upsert(draft: UserDraft, editingId: number | null): User {
    const existing = editingId === null ? null : this.users.find((user) => user.id === editingId) ?? null;
    const registrationDate = draft.registrationDate || existing?.registrationDate || this.todayIsoDate();
    const renewalDate = existing?.renewalDate ?? this.addOneMonth(registrationDate);

    const payload: User = {
      id: editingId ?? nextNumericId(this.users),
      ...draft,
      registrationDate,
      renewalDate
    };

    if (editingId === null) {
      this.users = [payload, ...this.users];
      return payload;
    }

    this.users = this.users.map((user) => (user.id === editingId ? payload : user));
    return payload;
  }

  renewSubscription(userId: number): User | null {
    const user = this.users.find((item) => item.id === userId);
    if (!user || user.userType !== 'alumno') {
      return null;
    }

    const renewed: User = {
      ...user,
      renewalDate: this.addOneMonth(this.todayIsoDate()),
      paymentStatus: 'al-dia'
    };

    this.users = this.users.map((item) => (item.id === userId ? renewed : item));
    return renewed;
  }

  remove(userId: number): void {
    this.users = this.users.filter((user) => user.id !== userId);
  }

  clearPlanReferences(planId: number): void {
    this.users = this.users.map((user) =>
      user.planId === planId
        ? {
            ...user,
            planId: null
          }
        : user
    );
  }

  getById(userId: number): User | null {
    const user = this.users.find((item) => item.id === userId);
    return user ? { ...user } : null;
  }

  fullName(userId: number | null): string {
    if (userId === null) {
      return 'Sin usuario';
    }

    const user = this.users.find((item) => item.id === userId);
    return user ? `${user.firstName} ${user.lastName}` : 'Sin usuario';
  }

  private todayIsoDate(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private addOneMonth(baseDateIso: string): string {
    const baseDate = new Date(`${baseDateIso}T00:00:00`);
    baseDate.setMonth(baseDate.getMonth() + 1);
    return baseDate.toISOString().slice(0, 10);
  }
}
