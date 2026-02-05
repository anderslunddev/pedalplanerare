import React, { useState } from "react";

type Board = {
  id: string;
  name: string;
  width: number;
  height: number;
};

type Pedal = {
  id: string;
  name: string;
  width: number;
  height: number;
  color: string;
  x: number;
  y: number;
};

const PIXELS_PER_UNIT = 5;

const App: React.FC = () => {
  const [width, setWidth] = useState<string>("");
  const [height, setHeight] = useState<string>("");
  const [board, setBoard] = useState<Board | null>(null);
  const [pedals, setPedals] = useState<Pedal[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [showPedalForm, setShowPedalForm] = useState(false);
  const [pedalName, setPedalName] = useState("");
  const [pedalWidth, setPedalWidth] = useState("");
  const [pedalHeight, setPedalHeight] = useState("");
  const [pedalColor, setPedalColor] = useState("#4f46e5");

  const [draggingId, setDraggingId] = useState<string | null>(null);
  const [dragOffset, setDragOffset] = useState<{ dx: number; dy: number } | null>(null);

  const handleCreateBoard = async () => {
    setError(null);
    const w = Number(width);
    const h = Number(height);
    if (!w || !h || w <= 0 || h <= 0) {
      setError("Width and height must be positive numbers.");
      return;
    }

    setLoading(true);
    try {
      const response = await fetch("/api/boards", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          name: "My Board",
          width: w,
          height: h
        })
      });

      if (!response.ok) {
        throw new Error(`Request failed with status ${response.status}`);
      }

      const data: Board = await response.json();
      setBoard(data);
      setPedals([]);
    } catch (e: any) {
      setError(e.message || "Failed to create board.");
    } finally {
      setLoading(false);
    }
  };

  const handleAddPedal = async () => {
    if (!board) return;
    setError(null);
    const w = Number(pedalWidth);
    const h = Number(pedalHeight);
    if (!pedalName || !w || !h || w <= 0 || h <= 0) {
      setError("Please provide pedal name, positive width and height.");
      return;
    }

    const x = (board.width - w) / 2;
    const y = (board.height - h) / 2;

    try {
      const response = await fetch(`/api/boards/${board.id}/pedals`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          name: pedalName,
          width: w,
          height: h,
          color: pedalColor,
          x,
          y
        })
      });
      if (!response.ok) {
        throw new Error(`Failed to add pedal (${response.status})`);
      }
      const created: Pedal = await response.json();
      setPedals((prev) => [...prev, created]);

      setShowPedalForm(false);
      setPedalName("");
      setPedalWidth("");
      setPedalHeight("");
    } catch (e: any) {
      setError(e.message || "Failed to add pedal.");
    }
  };

  const handleMouseDownPedal = (
    e: React.MouseEvent<HTMLDivElement>,
    pedal: Pedal
  ) => {
    if (!board) return;
    const boardRect = (e.currentTarget.parentElement as HTMLDivElement).getBoundingClientRect();
    const startX = e.clientX - boardRect.left;
    const startY = e.clientY - boardRect.top;
    const pedalX = pedal.x * PIXELS_PER_UNIT;
    const pedalY = pedal.y * PIXELS_PER_UNIT;
    setDraggingId(pedal.id);
    setDragOffset({
      dx: startX - pedalX,
      dy: startY - pedalY
    });
  };

  const handleMouseMoveBoard = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!draggingId || !dragOffset || !board) return;
    const boardRect = e.currentTarget.getBoundingClientRect();
    const px = e.clientX - boardRect.left - dragOffset.dx;
    const py = e.clientY - boardRect.top - dragOffset.dy;

    const maxX = (board.width - 0) * PIXELS_PER_UNIT;
    const maxY = (board.height - 0) * PIXELS_PER_UNIT;

    const clampedPx = Math.max(0, Math.min(px, maxX));
    const clampedPy = Math.max(0, Math.min(py, maxY));

    const newXUnits = clampedPx / PIXELS_PER_UNIT;
    const newYUnits = clampedPy / PIXELS_PER_UNIT;

    setPedals((prev) =>
      prev.map((p) =>
        p.id === draggingId ? { ...p, x: newXUnits, y: newYUnits } : p
      )
    );
  };

  const handleMouseUpBoard = async () => {
    if (!draggingId) return;
    const pedal = pedals.find((p) => p.id === draggingId);
    setDraggingId(null);
    setDragOffset(null);
    if (!pedal) return;
    try {
      await fetch(`/api/pedals/${pedal.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          x: pedal.x,
          y: pedal.y
        })
      });
    } catch {
      // ignore for now; in real app you'd handle error
    }
  };

  let rectWidth = 0;
  let rectHeight = 0;

  if (board) {
    rectWidth = board.width * PIXELS_PER_UNIT;
    rectHeight = board.height * PIXELS_PER_UNIT;
  }

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="w-full max-w-4xl px-4 py-8">
        <h1 className="text-2xl font-semibold mb-6">Pedalboard Planner</h1>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="md:col-span-1 space-y-4">
            <div>
              <label className="block text-sm mb-1">Board Width (units)</label>
              <input
                type="number"
                value={width}
                onChange={(e) => setWidth(e.target.value)}
                className="w-full rounded border border-slate-600 bg-slate-800 px-2 py-1 text-sm"
                placeholder="e.g. 60"
              />
            </div>
            <div>
              <label className="block text-sm mb-1">Board Height (units)</label>
              <input
                type="number"
                value={height}
                onChange={(e) => setHeight(e.target.value)}
                className="w-full rounded border border-slate-600 bg-slate-800 px-2 py-1 text-sm"
                placeholder="e.g. 30"
              />
            </div>
            <button
              onClick={handleCreateBoard}
              disabled={loading}
              className="mt-2 inline-flex items-center justify-center rounded bg-indigo-500 px-3 py-1.5 text-sm font-medium hover:bg-indigo-400 disabled:opacity-60"
            >
              {loading ? "Creating..." : "Create Board"}
            </button>
            {board && (
              <button
                onClick={() => setShowPedalForm(true)}
                className="mt-2 inline-flex items-center justify-center rounded bg-emerald-500 px-3 py-1.5 text-sm font-medium hover:bg-emerald-400"
              >
                Add Pedal
              </button>
            )}
            {error && (
              <p className="mt-2 text-sm text-red-400">
                {error}
              </p>
            )}
            {board && (
              <p className="mt-2 text-xs text-slate-400">
                Scale: 1 unit = {PIXELS_PER_UNIT}px
              </p>
            )}
          </div>
          <div className="md:col-span-2 flex items-center justify-center">
            {board ? (
              <div className="flex flex-col items-center space-y-3">
                <div className="text-sm text-slate-300">
                  Board {board.id.slice(0, 8)} – {board.width} × {board.height}
                </div>
                <div
                  className="relative bg-slate-700"
                  style={{
                    width: rectWidth,
                    height: rectHeight
                  }}
                  onMouseMove={handleMouseMoveBoard}
                  onMouseUp={handleMouseUpBoard}
                  onMouseLeave={handleMouseUpBoard}
                >
                  {pedals.map((pedal) => (
                    <div
                      key={pedal.id}
                      className="absolute rounded shadow cursor-move flex items-center justify-center text-[10px] font-medium"
                      style={{
                        width: pedal.width * PIXELS_PER_UNIT,
                        height: pedal.height * PIXELS_PER_UNIT,
                        left: pedal.x * PIXELS_PER_UNIT,
                        top: pedal.y * PIXELS_PER_UNIT,
                        backgroundColor: pedal.color
                      }}
                      onMouseDown={(e) => handleMouseDownPedal(e, pedal)}
                    >
                      <span className="px-1 text-slate-900">{pedal.name}</span>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div className="text-sm text-slate-400">
                Enter dimensions and create a board to see a preview here.
              </div>
            )}
          </div>
        </div>

        {showPedalForm && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center">
            <div className="bg-slate-800 rounded-lg p-4 w-full max-w-sm space-y-3">
              <h2 className="text-lg font-semibold">Add Pedal</h2>
              <div>
                <label className="block text-sm mb-1">Name</label>
                <input
                  type="text"
                  value={pedalName}
                  onChange={(e) => setPedalName(e.target.value)}
                  className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm"
                />
              </div>
              <div className="flex gap-2">
                <div className="flex-1">
                  <label className="block text-sm mb-1">Width (units)</label>
                  <input
                    type="number"
                    value={pedalWidth}
                    onChange={(e) => setPedalWidth(e.target.value)}
                    className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm"
                  />
                </div>
                <div className="flex-1">
                  <label className="block text-sm mb-1">Height (units)</label>
                  <input
                    type="number"
                    value={pedalHeight}
                    onChange={(e) => setPedalHeight(e.target.value)}
                    className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm"
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm mb-1">Color</label>
                <input
                  type="color"
                  value={pedalColor}
                  onChange={(e) => setPedalColor(e.target.value)}
                  className="h-8 w-16 rounded border border-slate-600 bg-slate-900"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  onClick={() => setShowPedalForm(false)}
                  className="px-3 py-1.5 text-sm rounded border border-slate-600"
                >
                  Cancel
                </button>
                <button
                  onClick={handleAddPedal}
                  className="px-3 py-1.5 text-sm rounded bg-emerald-500 hover:bg-emerald-400"
                >
                  Save
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default App;

