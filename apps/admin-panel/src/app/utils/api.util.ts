export const API_BASE_URL = 'http://localhost:8080/api/v1';
export const API_ORIGIN = API_BASE_URL.replace(/\/api\/v1$/, '');

export const resolveApiAssetUrl = (value: string): string => {
  if (!value) {
    return value;
  }
  if (value.startsWith('http://') || value.startsWith('https://') || value.startsWith('blob:')) {
    return value;
  }
  return `${API_ORIGIN}${value.startsWith('/') ? value : `/${value}`}`;
};
