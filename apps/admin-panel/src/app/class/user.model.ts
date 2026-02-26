export type UserType = 'profesor' | 'alumno';
export type PaymentStatus = 'al-dia' | 'adeuda';

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  dni: string;
  address: string;
  phone: string;
  email: string;
  birthDate: string;
  userType: UserType;
  planId: number | null;
  registrationDate: string;
  renewalDate: string;
  paymentStatus: PaymentStatus;
}

export type UserDraft = Omit<User, 'id'>;

export const createEmptyUserDraft = (): UserDraft => ({
  firstName: '',
  lastName: '',
  dni: '',
  address: '',
  phone: '',
  email: '',
  birthDate: '',
  userType: 'alumno',
  planId: null,
  registrationDate: '',
  renewalDate: '',
  paymentStatus: 'al-dia'
});
