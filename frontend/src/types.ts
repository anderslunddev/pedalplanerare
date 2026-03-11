export type Board = {
  id: string;
  name: string;
  width: number;
  height: number;
  pedals?: Pedal[];
};

export type Pedal = {
  id: string;
  name: string;
  width: number;
  height: number;
  color: string;
  x: number;
  y: number;
  placement?: number;
};

export type PathPoint = {
  x: number;
  y: number;
};

export type Cable = {
  id: string;
  boardId: string;
  sourcePedalId: string;
  destinationPedalId: string;
  pathPoints: PathPoint[];
  totalLength: number;
};

export type User = {
  id: string;
  username: string;
  email: string;
};

export const PIXELS_PER_UNIT = 8;
