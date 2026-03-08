export interface UserPaymentMovement {
  id: number;
  userId: number;
  studentName: string;
  planId: number | null;
  planName: string;
  amount: number;
  detail: string;
  paidAt: string;
}
