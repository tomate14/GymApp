export type UserType = 'admin' | 'profesor' | 'alumno';
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
  roleId: number | null;
  roleName: string | null;
  planId: number | null;
  registrationDate: string;
  renewalDate: string;
  paymentStatus: PaymentStatus;
}

export type UserDraft = Omit<User, 'id'> & {
  password: string;
};

export const createEmptyUserDraft = (): UserDraft => ({
  firstName: '',
  lastName: '',
  dni: '',
  address: '',
  phone: '',
  email: '',
  birthDate: '',
  userType: 'alumno',
  roleId: null,
  roleName: null,
  planId: null,
  password: '',
  registrationDate: '',
  renewalDate: '',
  paymentStatus: 'al-dia'
});
