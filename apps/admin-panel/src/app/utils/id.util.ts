export const nextNumericId = (collection: Array<{ id: number }>): number => {
  if (collection.length === 0) {
    return 1;
  }

  return Math.max(...collection.map((item) => item.id)) + 1;
};

export const ensureMinNumber = (value: number | null, fallback: number): number => {
  if (value === null || Number.isNaN(value)) {
    return fallback;
  }

  return Math.max(value, fallback);
};

