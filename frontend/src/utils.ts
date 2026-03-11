type Rect = { x: number; y: number; width: number; height: number };

export function getContrastTextColor(hex: string): string {
  const c = hex.replace("#", "");
  const r = parseInt(c.substring(0, 2), 16);
  const g = parseInt(c.substring(2, 4), 16);
  const b = parseInt(c.substring(4, 6), 16);
  const luminance = 0.299 * r + 0.587 * g + 0.114 * b;
  return luminance > 140 ? "#1e293b" : "#f1f5f9";
}

export function isOverlapping(a: Rect, b: Rect): boolean {
  return !(
    a.x + a.width <= b.x ||
    b.x + b.width <= a.x ||
    a.y + a.height <= b.y ||
    b.y + b.height <= a.y
  );
}

export function findDefaultPosition(
  boardWidth: number,
  boardHeight: number,
  existingPedals: Rect[],
  w: number,
  h: number
): { x: number; y: number } {
  const margin = 1;
  const centerX = (boardWidth - w) / 2;
  const centerY = (boardHeight - h) / 2;
  const candidate = { x: centerX, y: centerY, width: w, height: h };

  const withinBoard = (x: number, y: number) =>
    x >= 0 && y >= 0 && x + w <= boardWidth && y + h <= boardHeight;

  const overlapsAny = (x: number, y: number) =>
    existingPedals.some((p) => isOverlapping({ ...candidate, x, y }, p));

  if (!overlapsAny(centerX, centerY)) return { x: centerX, y: centerY };

  const tries = [
    { x: centerX + w + margin, y: centerY },
    { x: centerX - w - margin, y: centerY },
    { x: centerX, y: centerY + h + margin },
    { x: centerX, y: centerY - h - margin },
    { x: centerX + w + margin, y: centerY + h + margin },
    { x: centerX - w - margin, y: centerY - h - margin },
  ];
  for (const t of tries) {
    if (withinBoard(t.x, t.y) && !overlapsAny(t.x, t.y)) return t;
  }
  return { x: centerX, y: centerY };
}
