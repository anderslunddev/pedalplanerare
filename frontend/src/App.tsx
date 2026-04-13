import React, { useState, useEffect, useCallback } from "react";
import Sidebar from "./Sidebar";
import LoginPage from "./components/LoginPage";
import CreateBoardDialog from "./components/CreateBoardDialog";
import AddPedalDialog from "./components/AddPedalDialog";
import AdminPanel from "./components/AdminPanel";
import { useAuth } from "./useAuth";
import { authHeaders } from "./api";
import { isOverlapping, getContrastTextColor } from "./utils";
import { Board, Pedal, Cable, PIXELS_PER_UNIT } from "./types";

const CABLE_COLORS = ["#e5e7eb", "#f97316", "#22c55e", "#3b82f6", "#ec4899", "#a855f7"];

function findAdjacentPosition(
  moving: Pedal,
  collidedWith: Pedal,
  others: Pedal[],
  board: Board
): { x: number; y: number } | null {
  const margin = 1;
  const candidates = [
    { x: collidedWith.x + collidedWith.width + margin, y: collidedWith.y },
    { x: collidedWith.x - moving.width - margin, y: collidedWith.y },
    { x: collidedWith.x, y: collidedWith.y + collidedWith.height + margin },
    { x: collidedWith.x, y: collidedWith.y - moving.height - margin },
  ];
  for (const c of candidates) {
    if (c.x < 0 || c.y < 0 || c.x + moving.width > board.width || c.y + moving.height > board.height)
      continue;
    if (!others.some((op) => isOverlapping({ ...moving, x: c.x, y: c.y }, op))) {
      return c;
    }
  }
  return null;
}

function buildCablePath(pts: { x: number; y: number }[]): string {
  let d = `M ${pts[0].x} ${pts[0].y}`;
  if (pts.length === 2) {
    const midX = (pts[0].x + pts[1].x) / 2;
    const midY = (pts[0].y + pts[1].y) / 2;
    d += ` Q ${midX} ${midY} ${pts[1].x} ${pts[1].y}`;
  } else {
    for (let i = 0; i < pts.length - 1; i++) {
      const p0 = i === 0 ? pts[0] : pts[i - 1];
      const p1 = pts[i];
      const p2 = pts[i + 1];
      const p3 = i + 2 < pts.length ? pts[i + 2] : pts[i + 1];
      const c1x = p1.x + (p2.x - p0.x) / 6;
      const c1y = p1.y + (p2.y - p0.y) / 6;
      const c2x = p2.x - (p3.x - p1.x) / 6;
      const c2y = p2.y - (p3.y - p1.y) / 6;
      d += ` C ${c1x} ${c1y} ${c2x} ${c2y} ${p2.x} ${p2.y}`;
    }
  }
  return d;
}

const App: React.FC = () => {
  const { authToken, currentUser, login, logout } = useAuth();

  const [activeBoardId, setActiveBoardId] = useState<string | null>(null);
  const [board, setBoard] = useState<Board | null>(null);
  const [pedals, setPedals] = useState<Pedal[]>([]);
  const [cables, setCables] = useState<Cable[]>([]);
  const [error, setError] = useState<string | null>(null);

  const [showCreateBoardDialog, setShowCreateBoardDialog] = useState(false);
  const [showPedalForm, setShowPedalForm] = useState(false);

  const [draggingId, setDraggingId] = useState<string | null>(null);
  const [dragOffset, setDragOffset] = useState<{ dx: number; dy: number } | null>(null);
  const [dragOriginal, setDragOriginal] = useState<{ x: number; y: number } | null>(null);

  const [sidebarRefreshKey, setSidebarRefreshKey] = useState(0);

  const [confirmingDeleteBoard, setConfirmingDeleteBoard] = useState(false);

  const [showAdmin, setShowAdmin] = useState(false);

  useEffect(() => {
    setConfirmingDeleteBoard(false);
    setError(null);
    if (activeBoardId) {
      loadBoard(activeBoardId);
    } else {
      setBoard(null);
      setPedals([]);
      setCables([]);
    }
  }, [activeBoardId]);

  const loadBoard = async (boardId: string) => {
    setError(null);
    try {
      const response = await fetch(`/api/boards/${boardId}`, {
        headers: authHeaders(authToken),
      });
      if (!response.ok) throw new Error("Failed to load board");
      const boardData: Board = await response.json();
      setBoard(boardData);
      setPedals(boardData.pedals || []);

      const cablesResponse = await fetch(`/api/boards/${boardId}/cables`, {
        headers: authHeaders(authToken),
      });
      if (cablesResponse.ok) {
        setCables(await cablesResponse.json());
      }
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to load board");
    }
  };

  const generateSequence = useCallback(async () => {
    if (!board) return;
    try {
      const response = await fetch(`/api/boards/${board.id}/generate-sequence`, {
        method: "POST",
        headers: authHeaders(authToken),
      });
      if (!response.ok) return;
      setCables(await response.json());
    } catch {
      // cable generation is best-effort after actions
    }
  }, [board, authToken]);

  const handleDeleteBoard = async () => {
    if (!board) return;
    setError(null);
    try {
      const res = await fetch(`/api/boards/${board.id}`, {
        method: "DELETE",
        headers: authHeaders(authToken),
      });
      if (!res.ok) {
        setError("Could not delete board. Try again.");
        return;
      }
      setActiveBoardId(null);
      setBoard(null);
      setPedals([]);
      setCables([]);
      setSidebarRefreshKey((k) => k + 1);
      setConfirmingDeleteBoard(false);
    } catch {
      setError("Could not delete board. Try again.");
    }
  };

  const handleDeletePedal = async (pedalId: string) => {
    setError(null);
    try {
      const res = await fetch(`/api/pedals/${pedalId}`, {
        method: "DELETE",
        headers: authHeaders(authToken),
      });
      if (!res.ok) {
        setError("Could not remove pedal. Try again.");
        return;
      }
      setPedals((prev) => prev.filter((p) => p.id !== pedalId));
      await generateSequence();
    } catch {
      setError("Could not remove pedal. Try again.");
    }
  };

  const handleMouseDownPedal = (e: React.MouseEvent<HTMLDivElement>, pedal: Pedal) => {
    if (!board) return;
    const boardRect = (e.currentTarget.parentElement as HTMLDivElement).getBoundingClientRect();
    setDraggingId(pedal.id);
    setDragOriginal({ x: pedal.x, y: pedal.y });
    setDragOffset({
      dx: e.clientX - boardRect.left - pedal.x * PIXELS_PER_UNIT,
      dy: e.clientY - boardRect.top - pedal.y * PIXELS_PER_UNIT,
    });
  };

  const handleMouseMoveBoard = (e: React.MouseEvent<HTMLDivElement>) => {
    if (!draggingId || !dragOffset || !board) return;
    const boardRect = e.currentTarget.getBoundingClientRect();
    const px = e.clientX - boardRect.left - dragOffset.dx;
    const py = e.clientY - boardRect.top - dragOffset.dy;

    const dragged = pedals.find((p) => p.id === draggingId);
    const pedalW = dragged?.width ?? 0;
    const pedalH = dragged?.height ?? 0;
    const maxX = (board.width - pedalW) * PIXELS_PER_UNIT;
    const maxY = (board.height - pedalH) * PIXELS_PER_UNIT;

    const newXUnits = Math.max(0, Math.min(px, maxX)) / PIXELS_PER_UNIT;
    const newYUnits = Math.max(0, Math.min(py, maxY)) / PIXELS_PER_UNIT;

    setPedals((prev) =>
      prev.map((p) => (p.id === draggingId ? { ...p, x: newXUnits, y: newYUnits } : p))
    );
  };

  const handleMouseUpBoard = async () => {
    if (!draggingId) return;
    const pedal = pedals.find((p) => p.id === draggingId);
    const origPos = dragOriginal;
    setDraggingId(null);
    setDragOffset(null);
    setDragOriginal(null);
    if (!pedal || !board) return;

    const otherPedals = pedals.filter((p) => p.id !== pedal.id);
    const overlappedWith = otherPedals.find((p) => isOverlapping(pedal, p));

    let finalX = pedal.x;
    let finalY = pedal.y;

    if (overlappedWith) {
      const adjacent = findAdjacentPosition(pedal, overlappedWith, otherPedals, board);
      if (!adjacent) {
        if (origPos) {
          setPedals((prev) =>
            prev.map((p) => (p.id === pedal.id ? { ...p, x: origPos.x, y: origPos.y } : p))
          );
        }
        setError("No room to place pedal there. Snapped back to original position.");
        return;
      }
      finalX = adjacent.x;
      finalY = adjacent.y;
      setPedals((prev) =>
        prev.map((p) => (p.id === pedal.id ? { ...p, x: finalX, y: finalY } : p))
      );
    }

    try {
      const res = await fetch(`/api/pedals/${pedal.id}`, {
        method: "PUT",
        headers: authHeaders(authToken),
        body: JSON.stringify({ x: finalX, y: finalY }),
      });
      if (!res.ok) {
        setError("Could not save pedal position. Try again.");
        return;
      }
      await generateSequence();
    } catch {
      setError("Could not save pedal position. Try again.");
    }
  };

  const handlePedalCreated = async (newPedal: Pedal) => {
    setPedals((prev) => [...prev, newPedal]);
    setShowPedalForm(false);
    await generateSequence();
  };

  const handleLogout = () => {
    logout();
    setActiveBoardId(null);
  };

  if (!currentUser) {
    return <LoginPage onLogin={login} />;
  }

  if (showAdmin && currentUser.role === "ADMIN") {
    return (
      <AdminPanel
        authToken={authToken}
        currentUserId={currentUser.id}
        onBack={() => setShowAdmin(false)}
      />
    );
  }

  const rectWidth = board ? board.width * PIXELS_PER_UNIT : 0;
  const rectHeight = board ? board.height * PIXELS_PER_UNIT : 0;

  const totalCableLength = cables.reduce((sum, c) => sum + c.totalLength, 0);

  const pedalById = (id: string) => pedals.find((p) => p.id === id);

  return (
    <div className="min-h-screen flex bg-slate-900">
      <Sidebar
        activeBoardId={activeBoardId}
        onBoardSelect={setActiveBoardId}
        userId={currentUser.id}
        authToken={authToken}
        refreshKey={sidebarRefreshKey}
        onAuthExpired={handleLogout}
      />
      <div className="flex-1 flex flex-col">
        <div className="h-16 border-b border-slate-800 flex items-center justify-between px-6 bg-slate-900/80 backdrop-blur">
          <h1 className="text-2xl font-semibold text-slate-100">Pedalboard Planner</h1>
          <div className="flex items-center gap-4">
            <span className="text-sm text-slate-300">
              Logged in as <span className="font-medium">{currentUser.username}</span>
            </span>
            {currentUser.role === "ADMIN" && (
              <button
                onClick={() => setShowAdmin(true)}
                className="text-sm text-amber-300 hover:text-amber-200"
              >
                Admin
              </button>
            )}
            <button onClick={handleLogout} className="text-sm text-slate-300 hover:text-red-300">
              Logout
            </button>
            <button
              onClick={() => setShowCreateBoardDialog(true)}
              className="inline-flex items-center justify-center rounded bg-indigo-500 px-4 py-2 text-sm font-medium hover:bg-indigo-400"
            >
              New board
            </button>
          </div>
        </div>

        <div className="p-6 overflow-auto">
          {!board ? (
            <div className="flex flex-col items-center justify-center min-h-[60vh]">
              <div className="text-center space-y-4">
                <h2 className="text-xl text-slate-300 mb-4">Welcome to Pedalboard Planner</h2>
                <p className="text-slate-400 mb-6">
                  Select a board from the sidebar or create a new one to get started.
                </p>
                <button
                  onClick={() => setShowCreateBoardDialog(true)}
                  className="inline-flex items-center justify-center rounded bg-indigo-500 px-6 py-3 text-base font-medium hover:bg-indigo-400"
                >
                  Create New Board
                </button>
              </div>
            </div>
          ) : (
            <div className="space-y-6">
              {/* Board header */}
              <div className="flex items-center justify-between">
                <div className="flex items-baseline gap-3">
                  <h2 className="text-xl text-slate-200">{board.name}</h2>
                  <span className="text-sm text-slate-400">
                    {board.width} &times; {board.height} cm
                  </span>
                </div>
                <div className="flex gap-2 items-center">
                  <button
                    onClick={() => setShowPedalForm(true)}
                    className="inline-flex items-center justify-center rounded bg-emerald-500 px-3 py-1.5 text-sm font-medium hover:bg-emerald-400"
                  >
                    Add Pedal
                  </button>
                  {!confirmingDeleteBoard ? (
                    <button
                      onClick={() => setConfirmingDeleteBoard(true)}
                      className="inline-flex items-center justify-center rounded bg-red-600/70 px-3 py-1.5 text-sm font-medium hover:bg-red-500"
                    >
                      Delete Board
                    </button>
                  ) : (
                    <div className="flex items-center gap-1.5 bg-red-900/40 border border-red-700 rounded px-3 py-1">
                      <span className="text-xs text-red-200">Delete this board?</span>
                      <button
                        onClick={handleDeleteBoard}
                        className="px-2 py-0.5 text-xs rounded bg-red-600 hover:bg-red-500 text-white font-medium"
                      >
                        Yes
                      </button>
                      <button
                        onClick={() => setConfirmingDeleteBoard(false)}
                        className="px-2 py-0.5 text-xs rounded border border-slate-500 text-slate-300 hover:bg-slate-700"
                      >
                        No
                      </button>
                    </div>
                  )}
                </div>
              </div>

              {error && (
                <div className="p-3 bg-red-900/30 border border-red-700 rounded text-red-200 text-sm">
                  {error}
                </div>
              )}

              {pedals.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-16 text-center">
                  <p className="text-slate-400 mb-4">
                    No pedals on this board yet. Add pedals to get started
                    — cable lengths will be calculated automatically.
                  </p>
                  <button
                    onClick={() => setShowPedalForm(true)}
                    className="inline-flex items-center justify-center rounded bg-emerald-500 px-5 py-2 text-sm font-medium hover:bg-emerald-400"
                  >
                    Add Your First Pedal
                  </button>
                </div>
              ) : (
                <>
                  {/* Board canvas */}
                  <div className="flex justify-center">
                    <div
                      className="relative bg-slate-700 rounded"
                      style={{ width: rectWidth, height: rectHeight }}
                      onMouseMove={handleMouseMoveBoard}
                      onMouseUp={handleMouseUpBoard}
                      onMouseLeave={handleMouseUpBoard}
                    >
                      <svg
                        className="absolute inset-0 pointer-events-none"
                        width={rectWidth}
                        height={rectHeight}
                      >
                        {/* Grid lines */}
                        {board && Array.from({ length: Math.floor(board.width) + 1 }, (_, i) => i).map((v) => (
                          <line
                            key={`gx-${v}`}
                            x1={v * PIXELS_PER_UNIT} y1={0}
                            x2={v * PIXELS_PER_UNIT} y2={rectHeight}
                            stroke={v % 5 === 0 ? "rgba(148,163,184,0.18)" : "rgba(148,163,184,0.06)"}
                            strokeWidth={v % 5 === 0 ? 1 : 0.5}
                          />
                        ))}
                        {board && Array.from({ length: Math.floor(board.height) + 1 }, (_, i) => i).map((v) => (
                          <line
                            key={`gy-${v}`}
                            x1={0} y1={v * PIXELS_PER_UNIT}
                            x2={rectWidth} y2={v * PIXELS_PER_UNIT}
                            stroke={v % 5 === 0 ? "rgba(148,163,184,0.18)" : "rgba(148,163,184,0.06)"}
                            strokeWidth={v % 5 === 0 ? 1 : 0.5}
                          />
                        ))}

                        {/* Tick labels along edges */}
                        {board && Array.from({ length: Math.floor(board.width / 10) + 1 }, (_, i) => i * 10).filter(v => v <= board.width).map((v) => (
                          <text
                            key={`tx-${v}`}
                            x={v * PIXELS_PER_UNIT}
                            y={rectHeight - 3}
                            textAnchor="middle"
                            fill="rgba(148,163,184,0.5)"
                            fontSize={9}
                          >
                            {v}
                          </text>
                        ))}
                        {board && Array.from({ length: Math.floor(board.height / 10) + 1 }, (_, i) => i * 10).filter(v => v <= board.height).map((v) => (
                          <text
                            key={`ty-${v}`}
                            x={3}
                            y={v * PIXELS_PER_UNIT + 10}
                            textAnchor="start"
                            fill="rgba(148,163,184,0.5)"
                            fontSize={9}
                          >
                            {v}
                          </text>
                        ))}

                        {/* Cable paths */}
                        {cables.map((cable, index) => {
                          if (!cable.pathPoints?.length) return null;
                          const pts = cable.pathPoints.map((p) => ({
                            x: p.x * PIXELS_PER_UNIT,
                            y: p.y * PIXELS_PER_UNIT,
                          }));
                          if (pts.length < 2) return null;
                          return (
                            <path
                              key={cable.id}
                              d={buildCablePath(pts)}
                              stroke={CABLE_COLORS[index % CABLE_COLORS.length]}
                              strokeWidth={2.5}
                              fill="none"
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              opacity={0.85}
                            />
                          );
                        })}
                      </svg>

                      {pedals.map((pedal) => {
                        const textColor = getContrastTextColor(pedal.color);
                        return (
                          <div
                            key={pedal.id}
                            className="absolute rounded shadow-md cursor-move flex items-center justify-center text-[11px] font-semibold group select-none"
                            style={{
                              width: pedal.width * PIXELS_PER_UNIT,
                              height: pedal.height * PIXELS_PER_UNIT,
                              left: pedal.x * PIXELS_PER_UNIT,
                              top: pedal.y * PIXELS_PER_UNIT,
                              backgroundColor: pedal.color,
                              color: textColor,
                            }}
                            onMouseDown={(e) => handleMouseDownPedal(e, pedal)}
                          >
                            <div className="absolute -top-2 -left-2 h-5 w-5 rounded-full bg-slate-900/90 text-[9px] text-slate-100 flex items-center justify-center border border-slate-500 font-bold">
                              {pedal.placement ?? ""}
                            </div>
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                handleDeletePedal(pedal.id);
                              }}
                              className="absolute -top-2 -right-2 h-5 w-5 rounded-full bg-red-600 text-[10px] text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-500"
                            >
                              &times;
                            </button>
                            <span className="px-1 truncate">{pedal.name}</span>
                          </div>
                        );
                      })}
                    </div>
                  </div>

                  {/* Cable summary */}
                  {cables.length > 0 && (
                    <div className="bg-slate-800 rounded-lg border border-slate-700 p-4">
                      <div className="flex items-center justify-between mb-3">
                        <h3 className="text-sm font-semibold text-slate-200">Cable Summary</h3>
                        <span className="text-sm font-bold text-indigo-400">
                          Total: {totalCableLength.toFixed(1)} cm
                        </span>
                      </div>
                      <table className="w-full text-sm">
                        <thead>
                          <tr className="text-xs text-slate-400 border-b border-slate-700">
                            <th className="text-left pb-2 font-medium w-8"></th>
                            <th className="text-left pb-2 font-medium">From</th>
                            <th className="text-left pb-2 font-medium w-8"></th>
                            <th className="text-left pb-2 font-medium">To</th>
                            <th className="text-right pb-2 font-medium">Length</th>
                          </tr>
                        </thead>
                        <tbody>
                          {cables.map((cable, index) => {
                            const src = pedalById(cable.sourcePedalId);
                            const dst = pedalById(cable.destinationPedalId);
                            const color = CABLE_COLORS[index % CABLE_COLORS.length];
                            return (
                              <tr key={cable.id} className="border-b border-slate-700/50 last:border-0">
                                <td className="py-1.5">
                                  <span
                                    className="inline-block w-3 h-3 rounded-full"
                                    style={{ backgroundColor: color }}
                                  />
                                </td>
                                <td className="py-1.5 text-slate-200">{src?.name ?? "?"}</td>
                                <td className="py-1.5 text-slate-500 text-center">&rarr;</td>
                                <td className="py-1.5 text-slate-200">{dst?.name ?? "?"}</td>
                                <td className="py-1.5 text-right text-slate-300 tabular-nums">
                                  {cable.totalLength.toFixed(1)} cm
                                </td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    </div>
                  )}
                </>
              )}
            </div>
          )}
        </div>
      </div>

      {showCreateBoardDialog && (
        <CreateBoardDialog
          userId={currentUser.id}
          authToken={authToken}
          onCreated={(newBoard) => {
            setActiveBoardId(newBoard.id);
            setShowCreateBoardDialog(false);
            setSidebarRefreshKey((k) => k + 1);
          }}
          onClose={() => setShowCreateBoardDialog(false)}
        />
      )}

      {showPedalForm && board && (
        <AddPedalDialog
          boardId={board.id}
          boardWidth={board.width}
          boardHeight={board.height}
          existingPedals={pedals}
          authToken={authToken}
          defaultPlacement={
            pedals.length === 0 ? 1 : Math.max(...pedals.map((p) => p.placement ?? 0)) + 1
          }
          onCreated={handlePedalCreated}
          onClose={() => setShowPedalForm(false)}
        />
      )}
    </div>
  );
};

export default App;
