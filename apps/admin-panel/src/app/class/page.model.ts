export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export const createEmptyPageResponse = <T>(): PageResponse<T> => ({
  content: [],
  page: 0,
  size: 8,
  totalElements: 0,
  totalPages: 0
});
