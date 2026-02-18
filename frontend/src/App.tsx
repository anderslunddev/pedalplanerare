import React, { useState, useEffect, useCallback } from "react";
import Sidebar from "./Sidebar";
import LoginPage from "./components/LoginPage";
import CreateBoardDialog from "./components/CreateBoardDialog";
import AddPedalDialog from "./components/AddPedalDialog";
import { useAuth } from "./useAuth";
import { authHeaders } from "./api";
import { isOverlapping } from "./utils";
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
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [showCreateBoardDialog, setShowCreateBoardDialog] = useState(false);
  const [showPedalForm, setShowPedalForm] = useState(false);

  const [draggingId, setDraggingId] = useState<string | null>(null);
  const [dragOffset, setDragOffset] = useState<{ dx: number; dy: number } | null>(null);

  const [sidebarRefreshKey, setSidebarRefreshKey] = useState(0);

  useEffect(() => {
    if (activeBoardId) {
      loadBoard(activeBoardId);
    } else {
      setBoard(null);
      setPedals([]);
      setCables([]);
    }
  }, [activeBoardId]);

  const loadBoard = async (boardId: string) => {
    setLoading(true);
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
    } finally {
      setLoading(false);
    }
  };

  const generateSequence = useCallback(async () => {
    if (!board) return;
    setError(null);
    setCables([]);
    try {
      const response = await fetch(`/api/boards/${board.id}/generate-sequence`, {
        method: "POST",
        headers: authHeaders(authToken),
      });
      if (!response.ok) {
        const msg = await response.text();
        throw new Error(msg || `Failed to generate sequence (${response.status})`);
      }
      setCables(await response.json());
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to generate sequence.");
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
      if (board?.id) {
        try {
          const cablesRes = await fetch(`/api/boards/${board.id}/cables`, {
            headers: authHeaders(authToken),
          });
          if (cablesRes.ok) {
            setCables(await cablesRes.json());
            return;
          }
        } catch {
          /* fall through to local filter */
        }
      }
      setCables((prev) =>
        prev.filter((c) => c.sourcePedalId !== pedalId && c.destinationPedalId !== pedalId)
      );
    } catch {
      setError("Could not remove pedal. Try again.");
    }
  };

  const handleMouseDownPedal = (e: React.MouseEvent<HTMLDivElement>, pedal: Pedal) => {
    if (!board) return;
    const boardRect = (e.currentTarget.parentElement as HTMLDivElement).getBoundingClientRect();
    setDraggingId(pedal.id);
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
    setDraggingId(null);
    setDragOffset(null);
    if (!pedal || !board) return;

    const otherPedals = pedals.filter((p) => p.id !== pedal.id);
    const overlappedWith = otherPedals.find((p) => isOverlapping(pedal, p));

    let finalX = pedal.x;
    let finalY = pedal.y;

    if (overlappedWith) {
      const adjacent = findAdjacentPosition(pedal, overlappedWith, otherPedals, board);
      if (!adjacent) {
        try {
          const res = await fetch(`/api/pedals/${pedal.id}`, {
            method: "DELETE",
            headers: authHeaders(authToken),
          });
          if (!res.ok) {
            setError("Could not remove pedal. Try again.");
            return;
          }
        } catch {
          setError("Could not remove pedal. Try again.");
          return;
        }
        setPedals((prev) => prev.filter((p) => p.id !== pedal.id));
        setCables((prev) =>
          prev.filter((c) => c.sourcePedalId !== pedal.id && c.destinationPedalId !== pedal.id)
        );
        await generateSequence();
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

  const handleLogout = () => {
    logout();
    setActiveBoardId(null);
  };

  if (!currentUser) {
    return <LoginPage onLogin={login} />;
  }

  const rectWidth = board ? board.width * PIXELS_PER_UNIT : 0;
  const rectHeight = board ? board.height * PIXELS_PER_UNIT : 0;

  return (
    <div className="min-h-screen flex bg-slate-900">
      <Sidebar
        activeBoardId={activeBoardId}
        onBoardSelect={setActiveBoardId}
        userId={currentUser.id}
        authToken={authToken}
        refreshKey={sidebarRefreshKey}
      />
      <div className="flex-1 flex flex-col">
        <div className="h-16 border-b border-slate-800 flex items-center justify-between px-6 bg-slate-900/80 backdrop-blur">
          <h1 className="text-2xl font-semibold text-slate-100">Pedalboard Planner</h1>
          <div className="flex items-center gap-4">
            <span className="text-sm text-slate-300">
              Logged in as <span className="font-medium">{currentUser.username}</span>
            </span>
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

        <div className="p-6">
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
              <div className="flex items-center justify-between">
                <h2 className="text-xl text-slate-200">{board.name}</h2>
                <div className="flex gap-2">
                  <button
                    onClick={() => setShowPedalForm(true)}
                    className="inline-flex items-center justify-center rounded bg-emerald-500 px-3 py-1.5 text-sm font-medium hover:bg-emerald-400"
                  >
                    Add Pedal
                  </button>
                  <button
                    onClick={generateSequence}
                    className="inline-flex items-center justify-center rounded bg-sky-500 px-3 py-1.5 text-sm font-medium hover:bg-sky-400"
                  >
                    Connect Pedals
                  </button>
                  <button
                    onClick={handleDeleteBoard}
                    className="inline-flex items-center justify-center rounded bg-red-600 px-3 py-1.5 text-sm font-medium hover:bg-red-500"
                  >
                    Delete Board
                  </button>
                </div>
              </div>

              {error && (
                <div className="p-3 bg-red-900/30 border border-red-700 rounded text-red-200 text-sm">
                  {error}
                </div>
              )}

              <div className="flex flex-col items-center space-y-3">
                <div className="text-sm text-slate-300">
                  {board.width} &times; {board.height} cm
                </div>
                <div className="flex flex-col">
                  <div className="flex items-end">
                    <div className="mr-2 flex flex-col items-end text-[10px] text-slate-400">
                      {[0, board.height / 2, board.height].map((v) => (
                        <span key={v.toFixed(1)}>{v.toFixed(1)} cm</span>
                      ))}
                    </div>
                    <div
                      className="relative bg-slate-700"
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
                              strokeWidth={2}
                              fill="none"
                              strokeLinecap="round"
                              strokeLinejoin="round"
                            />
                          );
                        })}
                      </svg>

                      {pedals.map((pedal) => (
                        <div
                          key={pedal.id}
                          className="absolute rounded shadow cursor-move flex items-center justify-center text-[10px] font-medium group"
                          style={{
                            width: pedal.width * PIXELS_PER_UNIT,
                            height: pedal.height * PIXELS_PER_UNIT,
                            left: pedal.x * PIXELS_PER_UNIT,
                            top: pedal.y * PIXELS_PER_UNIT,
                            backgroundColor: pedal.color,
                          }}
                          onMouseDown={(e) => handleMouseDownPedal(e, pedal)}
                        >
                          <div className="absolute -top-1 -left-1 h-4 w-4 rounded-full bg-slate-900/80 text-[8px] text-slate-100 flex items-center justify-center border border-slate-500">
                            {pedal.placement ?? ""}
                          </div>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleDeletePedal(pedal.id);
                            }}
                            className="absolute -top-1 -right-1 h-4 w-4 rounded-full bg-red-600 text-[8px] text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                          >
                            &times;
                          </button>
                          <span className="px-1 text-slate-900">{pedal.name}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                  <div className="mt-1 text-[10px] text-slate-400 text-center">
                    0 cm
                    <span className="mx-2">&rarr;</span>
                    {board.width.toFixed(1)} cm
                  </div>
                </div>
              </div>

              {cables.length > 0 && (
                <div className="mt-6">
                  <h2 className="text-sm font-semibold mb-2 text-slate-300">Cables</h2>
                  <ul className="text-xs text-slate-200 space-y-1">
                    {cables.map((cable) => (
                      <li key={cable.id}>{cable.totalLength.toFixed(1)} cm</li>
                    ))}
                  </ul>
                </div>
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
          onCreated={(newPedal) => {
            setPedals((prev) => [...prev, newPedal]);
            setShowPedalForm(false);
          }}
          onClose={() => setShowPedalForm(false)}
        />
      )}
    </div>
  );
};

export default App;
