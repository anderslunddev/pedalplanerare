import React, { useState, useEffect } from "react";
import Sidebar from "./Sidebar";

type Board = {
  id: string;
  name: string;
  width: number;
  height: number;
  pedals?: Pedal[];
};

type Pedal = {
  id: string;
  name: string;
  width: number;
  height: number;
  color: string;
  x: number;
  y: number;
  placement?: number;
};

type PathPoint = {
  x: number;
  y: number;
};

type Cable = {
  id: string;
  boardId: string;
  sourcePedalId: string;
  destinationPedalId: string;
  pathPoints: PathPoint[];
  totalLength: number;
};

type User = {
  id: string;
  username: string;
  email: string;
};

const PIXELS_PER_UNIT = 5;

function isOverlapping(
  a: { x: number; y: number; width: number; height: number },
  b: { x: number; y: number; width: number; height: number }
): boolean {
  return !(
    a.x + a.width <= b.x ||
    b.x + b.width <= a.x ||
    a.y + a.height <= b.y ||
    b.y + b.height <= a.y
  );
}

/** Find a default (x, y) for a new pedal of size (w, h) that does not overlap existing pedals. */
function findDefaultPosition(
  boardWidth: number,
  boardHeight: number,
  existingPedals: { x: number; y: number; width: number; height: number }[],
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
    existingPedals.some((p) =>
      isOverlapping({ ...candidate, x, y }, p)
    );

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

const App: React.FC = () => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [authToken, setAuthToken] = useState<string | null>(null);
  const [loginUsername, setLoginUsername] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [showSignUp, setShowSignUp] = useState(false);
  const [signUpUsername, setSignUpUsername] = useState("");
  const [signUpEmail, setSignUpEmail] = useState("");
  const [signUpPassword, setSignUpPassword] = useState("");
  const [signUpPasswordConfirm, setSignUpPasswordConfirm] = useState("");

  const [activeBoardId, setActiveBoardId] = useState<string | null>(null);
  const [board, setBoard] = useState<Board | null>(null);
  const [pedals, setPedals] = useState<Pedal[]>([]);
  const [cables, setCables] = useState<Cable[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [showCreateBoardDialog, setShowCreateBoardDialog] = useState(false);
  const [boardName, setBoardName] = useState("");
  const [boardWidth, setBoardWidth] = useState<string>("80");
  const [boardHeight, setBoardHeight] = useState<string>("60");

  const [showPedalForm, setShowPedalForm] = useState(false);
  const [pedalName, setPedalName] = useState("");
  const [pedalWidth, setPedalWidth] = useState("10");
  const [pedalHeight, setPedalHeight] = useState("10");
  const [pedalColor, setPedalColor] = useState("#4f46e5");
  const [pedalPlacement, setPedalPlacement] = useState("");

  const [draggingId, setDraggingId] = useState<string | null>(null);
  const [dragOffset, setDragOffset] = useState<{ dx: number; dy: number } | null>(null);

  // Helper function to get headers with auth token
  const getAuthHeaders = (): HeadersInit => {
    const headers: HeadersInit = {
      "Content-Type": "application/json",
    };
    if (authToken) {
      headers["Authorization"] = `Bearer ${authToken}`;
    }
    return headers;
  };

  // Load board data when activeBoardId changes
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
        headers: getAuthHeaders(),
      });
      if (!response.ok) {
        throw new Error("Failed to load board");
      }
      const boardData: Board = await response.json();
      setBoard(boardData);
      setPedals(boardData.pedals || []);

      // Load cables
      const cablesResponse = await fetch(`/api/boards/${boardId}/cables`, {
        headers: getAuthHeaders(),
      });
      if (cablesResponse.ok) {
        const cablesData: Cable[] = await cablesResponse.json();
        setCables(cablesData);
      }
    } catch (e: any) {
      setError(e.message || "Failed to load board");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateBoard = async () => {
    if (!currentUser) {
      setError("You must be logged in to create a board.");
      return;
    }
    setError(null);
    if (!boardName || boardName.trim() === "") {
      setError("Board name is required.");
      return;
    }
    const w = Number(boardWidth);
    const h = Number(boardHeight);
    if (!w || !h || w <= 0 || h <= 0) {
      setError("Width and height must be positive numbers.");
      return;
    }

    setLoading(true);
    try {
      const response = await fetch("/api/boards", {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({
          name: boardName.trim(),
          width: w,
          height: h,
          userId: currentUser.id
        })
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Request failed with status ${response.status}`);
      }

      const data: Board = await response.json();
      setActiveBoardId(data.id);
      setShowCreateBoardDialog(false);
      setBoardName("");
      setBoardWidth("80");
      setBoardHeight("60");
    } catch (e: any) {
      setError(e.message || "Failed to create board.");
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async () => {
    setError(null);
    if (!loginUsername || !loginPassword) {
      setError("Username and password are required.");
      return;
    }
    setLoading(true);
    try {
      const response = await fetch("/api/users/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          username: loginUsername,
          password: loginPassword
        })
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Login failed");
      }

      const loginResponse: { token: string; user: User } = await response.json();
      setCurrentUser(loginResponse.user);
      setAuthToken(loginResponse.token);
      setLoginPassword("");
    } catch (e: any) {
      setError(e.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    setCurrentUser(null);
    setAuthToken(null);
    setActiveBoardId(null);
  };

  const handleAddPedal = async () => {
    if (!board) return;
    setError(null);
    const w = Number(pedalWidth);
    const h = Number(pedalHeight);
    const placementNum = Number(pedalPlacement);
    if (!pedalName || !w || !h || w <= 0 || h <= 0) {
      setError("Please provide pedal name, positive width and height.");
      return;
    }
    if (!placementNum || placementNum <= 0) {
      setError("Placement must be a positive number.");
      return;
    }
    if (
      pedals.some((p: any) => (p.placement ?? 0) === placementNum)
    ) {
      setError(`Placement number ${placementNum} is already in use.`);
      return;
    }

    const { x, y } = findDefaultPosition(board.width, board.height, pedals, w, h);

    try {
      const response = await fetch(`/api/boards/${board.id}/pedals`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({
          name: pedalName,
          width: w,
          height: h,
          color: pedalColor,
          x,
          y,
          placement: placementNum
        })
      });
      if (!response.ok) {
        throw new Error(`Failed to add pedal (${response.status})`);
      }
      const created: Pedal = await response.json();
      setPedals((prev) => [...prev, created]);

      setShowPedalForm(false);
      setPedalName("");
      setPedalWidth("10");
      setPedalHeight("10");
      setPedalPlacement("");
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
    if (!pedal || !board) return;

    // Prevent overlapping pedals: try to move next to any collided pedal; delete if no space.
    const otherPedals = pedals.filter((p) => p.id !== pedal.id);

    const findNonOverlappingAdjacentPosition = (
      moving: Pedal,
      collidedWith: Pedal,
      others: Pedal[]
    ): { x: number; y: number } | null => {
      const margin = 1; // units
      const candidates = [
        { // right
          x: collidedWith.x + collidedWith.width + margin,
          y: collidedWith.y
        },
        { // left
          x: collidedWith.x - moving.width - margin,
          y: collidedWith.y
        },
        { // below
          x: collidedWith.x,
          y: collidedWith.y + collidedWith.height + margin
        },
        { // above
          x: collidedWith.x,
          y: collidedWith.y - moving.height - margin
        }
      ];

      const withinBoard = (x: number, y: number) =>
        x >= 0 &&
        y >= 0 &&
        x + moving.width <= board.width &&
        y + moving.height <= board.height;

      for (const c of candidates) {
        if (!withinBoard(c.x, c.y)) continue;
        const test: Pedal = { ...moving, x: c.x, y: c.y };
        const collides = others.some((op) => isOverlapping(test, op));
        if (!collides) {
          return { x: c.x, y: c.y };
        }
      }
      return null;
    };

    const overlappedWith = otherPedals.find((p) => isOverlapping(pedal, p));

    let finalX = pedal.x;
    let finalY = pedal.y;

    if (overlappedWith) {
      const adjacent = findNonOverlappingAdjacentPosition(pedal, overlappedWith, otherPedals);
      if (!adjacent) {
        // No place to move: delete this pedal and refresh cables
        try {
          const res = await fetch(`/api/pedals/${pedal.id}`, {
            method: "DELETE",
            headers: getAuthHeaders(),
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
          prev.filter(
            (c) =>
              c.sourcePedalId !== pedal.id &&
              c.destinationPedalId !== pedal.id
          )
        );
        await generateSequence();
        return;
      }
      finalX = adjacent.x;
      finalY = adjacent.y;
      // Update pedal position locally to the resolved coordinates
      setPedals((prev) =>
        prev.map((p) =>
          p.id === pedal.id ? { ...p, x: finalX, y: finalY } : p
        )
      );
    }
    try {
      const res = await fetch(`/api/pedals/${pedal.id}`, {
        method: "PUT",
        headers: getAuthHeaders(),
        body: JSON.stringify({
          x: finalX,
          y: finalY
        })
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

  const generateSequence = async () => {
    if (!board) return;
    setError(null);
    // Always clear current routes before (re)generating
    setCables([]);
    try {
      const response = await fetch(`/api/boards/${board.id}/generate-sequence`, {
        method: "POST",
        headers: getAuthHeaders(),
      });
      if (!response.ok) {
        const msg = await response.text();
        throw new Error(msg || `Failed to generate sequence (${response.status})`);
      }
      const data: Cable[] = await response.json();
      setCables(data);
    } catch (e: any) {
      setError(e.message || "Failed to generate sequence.");
    }
  };

  const handleBoardSelect = (boardId: string) => {
    setActiveBoardId(boardId);
  };

  const handleDeleteBoard = async () => {
    if (!board) return;
    setError(null);
    try {
      const res = await fetch(`/api/boards/${board.id}`, {
        method: "DELETE",
        headers: getAuthHeaders(),
      });
      if (!res.ok) {
        setError("Could not delete board. Try again.");
        return;
      }
      setActiveBoardId(null);
      setBoard(null);
      setPedals([]);
      setCables([]);
    } catch {
      setError("Could not delete board. Try again.");
    }
  };

  // If not logged in, show a dedicated login screen and nothing else.
  if (!currentUser) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-900">
        <div className="bg-slate-800 rounded-lg p-8 w-full max-w-md space-y-6 shadow-xl border border-slate-700">
          <div className="space-y-2 text-center">
            <h1 className="text-2xl font-semibold text-slate-100">
              Pedalboard Planner
            </h1>
            <p className="text-sm text-slate-400">
              {showSignUp
                ? "Create an account to save and manage your boards."
                : "Log in to see and manage your boards."}
            </p>
          </div>
          {error && (
            <div className="p-3 bg-red-900/30 border border-red-700 rounded text-red-200 text-sm">
              {error}
            </div>
          )}
          {!showSignUp ? (
            <>
              <div className="space-y-4">
                <div className="space-y-2">
                  <label className="block text-sm font-medium text-slate-200">
                    Username
                  </label>
                  <input
                    type="text"
                    value={loginUsername}
                    onChange={(e) => setLoginUsername(e.target.value)}
                    className="w-full rounded-md bg-slate-900 border border-slate-700 px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-indigo-400"
                    placeholder="Username"
                  />
                </div>
                <div className="space-y-2">
                  <label className="block text-sm font-medium text-slate-200">
                    Password
                  </label>
                  <input
                    type="password"
                    value={loginPassword}
                    onChange={(e) => setLoginPassword(e.target.value)}
                    className="w-full rounded-md bg-slate-900 border border-slate-700 px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-indigo-400"
                    placeholder="Password"
                  />
                </div>
                <button
                  onClick={handleLogin}
                  disabled={loading}
                  className="w-full inline-flex items-center justify-center rounded-md bg-indigo-500 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-400 disabled:opacity-60"
                >
                  {loading ? "Logging in..." : "Log in"}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setError(null);
                    setShowSignUp(true);
                  }}
                  className="w-full inline-flex items-center justify-center rounded-md border border-slate-600 px-4 py-2 text-sm font-medium text-slate-200 hover:bg-slate-700"
                >
                  Create an account
                </button>
              </div>
            </>
          ) : (
            <>
              <div className="space-y-4">
                <div className="space-y-2">
                  <label className="block text-sm font-medium text-slate-200">
                    Username
                  </label>
                  <input
                    type="text"
                    value={signUpUsername}
                    onChange={(e) => setSignUpUsername(e.target.value)}
                    className="w-full rounded-md bg-slate-900 border border-slate-700 px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-indigo-400"
                    placeholder="your-username"
                  />
                </div>
                <div className="space-y-2">
                  <label className="block text-sm font-medium text-slate-200">
                    Email
                  </label>
                  <input
                    type="email"
                    value={signUpEmail}
                    onChange={(e) => setSignUpEmail(e.target.value)}
                    className="w-full rounded-md bg-slate-900 border border-slate-700 px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-indigo-400"
                    placeholder="you@example.com"
                  />
                </div>
                <div className="space-y-2">
                  <label className="block text-sm font-medium text-slate-200">
                    Password
                  </label>
                  <input
                    type="password"
                    value={signUpPassword}
                    onChange={(e) => setSignUpPassword(e.target.value)}
                    className="w-full rounded-md bg-slate-900 border border-slate-700 px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-indigo-400"
                    placeholder="Choose a strong password"
                  />
                </div>
                <div className="space-y-2">
                  <label className="block text-sm font-medium text-slate-200">
                    Confirm password
                  </label>
                  <input
                    type="password"
                    value={signUpPasswordConfirm}
                    onChange={(e) => setSignUpPasswordConfirm(e.target.value)}
                    className="w-full rounded-md bg-slate-900 border border-slate-700 px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-indigo-400"
                    placeholder="Repeat password"
                  />
                </div>
                <button
                  onClick={async () => {
                    setError(null);
                    if (!signUpUsername || !signUpEmail || !signUpPassword) {
                      setError("Username, email and password are required.");
                      return;
                    }
                    if (signUpPassword !== signUpPasswordConfirm) {
                      setError("Passwords do not match.");
                      return;
                    }
                    try {
                      const response = await fetch("/api/users", {
                        method: "POST",
                        headers: {
                          "Content-Type": "application/json"
                        },
                        body: JSON.stringify({
                          username: signUpUsername.trim(),
                          email: signUpEmail.trim(),
                          password: signUpPassword
                        })
                      });
                      if (!response.ok) {
                        const text = await response.text();
                        throw new Error(text || "Sign up failed");
                      }
                      // On success, switch back to login screen and prefill username.
                      setLoginUsername(signUpUsername.trim());
                      setShowSignUp(false);
                      setSignUpUsername("");
                      setSignUpEmail("");
                      setSignUpPassword("");
                      setSignUpPasswordConfirm("");
                      setError("Account created. You can now log in.");
                    } catch (e: any) {
                      setError(e.message || "Sign up failed");
                    }
                  }}
                  disabled={loading}
                  className="w-full inline-flex items-center justify-center rounded-md bg-indigo-500 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-400 disabled:opacity-60"
                >
                  {loading ? "Creating account..." : "Create account"}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowSignUp(false);
                    setError(null);
                  }}
                  className="w-full inline-flex items-center justify-center rounded-md border border-slate-600 px-4 py-2 text-sm font-medium text-slate-200 hover:bg-slate-700"
                >
                  Back to login
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    );
  }

  let rectWidth = 0;
  let rectHeight = 0;

  if (board) {
    rectWidth = board.width * PIXELS_PER_UNIT;
    rectHeight = board.height * PIXELS_PER_UNIT;
  }

  return (
    <div className="min-h-screen flex bg-slate-900">
      <Sidebar
        activeBoardId={activeBoardId}
        onBoardSelect={handleBoardSelect}
        userId={currentUser?.id ?? null}
        authToken={authToken}
      />
      <div className="flex-1 flex flex-col">
        <div className="h-16 border-b border-slate-800 flex items-center justify-between px-6 bg-slate-900/80 backdrop-blur">
          <h1 className="text-2xl font-semibold text-slate-100">Pedalboard Planner</h1>
          <div className="flex items-center gap-4">
            {currentUser ? (
              <>
                <span className="text-sm text-slate-300">
                  Logged in as{" "}
                  <span className="font-medium">{currentUser.username}</span>
                </span>
                <button
                  onClick={handleLogout}
                  className="text-sm text-slate-300 hover:text-red-300"
                >
                  Logout
                </button>
                <button
                  onClick={() => {
                    setBoardName("");
                    setBoardWidth("80");
                    setBoardHeight("60");
                    setShowCreateBoardDialog(true);
                  }}
                  className="inline-flex items-center justify-center rounded bg-indigo-500 px-4 py-2 text-sm font-medium hover:bg-indigo-400"
                >
                  New board
                </button>
              </>
            ) : (
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  placeholder="Username"
                  value={loginUsername}
                  onChange={(e) => setLoginUsername(e.target.value)}
                  className="rounded-md bg-slate-800 border border-slate-700 px-2 py-1 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-indigo-400"
                />
                <input
                  type="password"
                  placeholder="Password"
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                  className="rounded-md bg-slate-800 border border-slate-700 px-2 py-1 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-indigo-400"
                />
                <button
                  onClick={handleLogin}
                  className="inline-flex items-center justify-center rounded bg-indigo-500 px-4 py-2 text-sm font-medium hover:bg-indigo-400"
                >
                  Login
                </button>
              </div>
            )}
          </div>
        </div>
        <div className="p-6">
          {!board ? (
            // Dashboard view
            <div className="flex flex-col items-center justify-center min-h-[60vh]">
              <div className="text-center space-y-4">
                <h2 className="text-xl text-slate-300 mb-4">Welcome to Pedalboard Planner</h2>
                <p className="text-slate-400 mb-6">Select a board from the sidebar or create a new one to get started.</p>
                <button
                  onClick={() => {
                    setBoardName("");
                    setBoardWidth("80");
                    setBoardHeight("60");
                    setShowCreateBoardDialog(true);
                  }}
                  className="inline-flex items-center justify-center rounded bg-indigo-500 px-6 py-3 text-base font-medium hover:bg-indigo-400"
                >
                  Create New Board
                </button>
              </div>
            </div>
          ) : (
            // Board view
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <h2 className="text-xl text-slate-200">{board.name}</h2>
                <div className="flex gap-2">
                  <button
                    onClick={() => {
                      if (!board) return;
                      const next =
                        pedals.length === 0
                          ? 1
                          : Math.max(
                              ...pedals.map((p) => (p as any).placement ?? 0)
                            ) + 1;
                      setPedalPlacement(String(next));
                      setShowPedalForm(true);
                    }}
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
                    onClick={async () => {
                      if (!board) return;
                      try {
                        await fetch(`/api/boards/${board.id}/cables`, {
                          method: "DELETE",
                          headers: getAuthHeaders(),
                        });
                        setCables([]);
                      } catch (e) {
                        // best-effort, don't crash UI
                      }
                    }}
                    className="inline-flex items-center justify-center rounded bg-slate-600 px-3 py-1.5 text-sm font-medium hover:bg-slate-500"
                  >
                    Delete All Cables
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
                  {board.width} × {board.height} cm
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
                      style={{
                        width: rectWidth,
                        height: rectHeight
                      }}
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
                          if (!cable.pathPoints || cable.pathPoints.length === 0) {
                            return null;
                          }
                          const colors = ["#e5e7eb", "#f97316", "#22c55e", "#3b82f6", "#ec4899", "#a855f7"];
                          const strokeColor = colors[index % colors.length];

                          const pts = cable.pathPoints.map((p) => ({
                            x: p.x * PIXELS_PER_UNIT,
                            y: p.y * PIXELS_PER_UNIT
                          }));
                          if (pts.length < 2) return null;

                          let d = `M ${pts[0].x} ${pts[0].y}`;
                          if (pts.length === 2) {
                            // simple straight segment as quadratic curve
                            const midX = (pts[0].x + pts[1].x) / 2;
                            const midY = (pts[0].y + pts[1].y) / 2;
                            d += ` Q ${midX} ${midY} ${pts[1].x} ${pts[1].y}`;
                          } else {
                            // Catmull-Rom to Bezier approximation
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

                          return (
                            <path
                              key={cable.id}
                              d={d}
                              stroke={strokeColor}
                              strokeWidth={2}
                              fill="none"
                              strokeLinecap="round"
                              strokeLinejoin="round"
                            />
                          );
                        })}
                      </svg>
                      {pedals.map((pedal: any) => (
                        <div
                          key={pedal.id}
                          className="absolute rounded shadow cursor-move flex items-center justify-center text-[10px] font-medium group"
                          style={{
                            width: pedal.width * PIXELS_PER_UNIT,
                            height: pedal.height * PIXELS_PER_UNIT,
                            left: pedal.x * PIXELS_PER_UNIT,
                            top: pedal.y * PIXELS_PER_UNIT,
                            backgroundColor: pedal.color
                          }}
                          onMouseDown={(e) => handleMouseDownPedal(e, pedal)}
                        >
                          <div className="absolute -top-1 -left-1 h-4 w-4 rounded-full bg-slate-900/80 text-[8px] text-slate-100 flex items-center justify-center border border-slate-500">
                            {pedal.placement ?? ""}
                          </div>
                          <button
                            onClick={async (e) => {
                              e.stopPropagation();
                              setError(null);
                              try {
                                const res = await fetch(`/api/pedals/${pedal.id}`, {
                                  method: "DELETE",
                                  headers: getAuthHeaders(),
                                });
                                if (!res.ok) {
                                  setError("Could not remove pedal. Try again.");
                                  return;
                                }
                                setPedals((prev) => prev.filter((p) => p.id !== pedal.id));
                                setCables((prev) =>
                                  prev.filter(
                                    (c) =>
                                      c.sourcePedalId !== pedal.id &&
                                      c.destinationPedalId !== pedal.id
                                  )
                                );
                              } catch {
                                setError("Could not remove pedal. Try again.");
                              }
                            }}
                            className="absolute -top-1 -right-1 h-4 w-4 rounded-full bg-red-600 text-[8px] text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                          >
                            ×
                          </button>
                          <span className="px-1 text-slate-900">{pedal.name}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                  <div className="mt-1 text-[10px] text-slate-400 text-center">
                    0 cm
                    <span className="mx-2">→</span>
                    {board.width.toFixed(1)} cm
                  </div>
                </div>
              </div>

              {cables.length > 0 && (
                <div className="mt-6">
                  <h2 className="text-sm font-semibold mb-2 text-slate-300">Cables</h2>
                  <ul className="text-xs text-slate-200 space-y-1">
                    {cables.map((cable) => (
                      <li key={cable.id}>
                        {cable.totalLength.toFixed(1)} cm
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Create Board Dialog */}
      {showCreateBoardDialog && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-slate-800 rounded-lg p-6 w-full max-w-sm space-y-4">
            <h2 className="text-lg font-semibold text-slate-100">Create New Board</h2>
            <div>
              <label className="block text-sm mb-1 text-slate-300">Board Name</label>
              <input
                type="text"
                value={boardName}
                onChange={(e) => setBoardName(e.target.value)}
                className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm text-slate-100"
                placeholder="e.g. My Pedalboard"
              />
            </div>
            <div>
              <label className="block text-sm mb-1 text-slate-300">Board Width (cm)</label>
              <input
                type="number"
                value={boardWidth}
                onChange={(e) => setBoardWidth(e.target.value)}
                className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm text-slate-100"
                placeholder="80"
              />
            </div>
            <div>
              <label className="block text-sm mb-1 text-slate-300">Board Height (cm)</label>
              <input
                type="number"
                value={boardHeight}
                onChange={(e) => setBoardHeight(e.target.value)}
                className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm text-slate-100"
                placeholder="60"
              />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => {
                  setShowCreateBoardDialog(false);
                  setBoardName("");
                  setBoardWidth("80");
                  setBoardHeight("60");
                  setError(null);
                }}
                className="px-4 py-2 text-sm rounded border border-slate-600 text-slate-300 hover:bg-slate-700"
              >
                Cancel
              </button>
              <button
                onClick={handleCreateBoard}
                disabled={loading}
                className="px-4 py-2 text-sm rounded bg-indigo-500 hover:bg-indigo-400 disabled:opacity-60 text-white"
              >
                {loading ? "Creating..." : "Create"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Add Pedal Dialog */}
      {showPedalForm && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-slate-800 rounded-lg p-4 w-full max-w-sm space-y-3">
            <h2 className="text-lg font-semibold text-slate-100">Add Pedal</h2>
            <div>
              <label className="block text-sm mb-1 text-slate-300">Name</label>
              <input
                type="text"
                value={pedalName}
                onChange={(e) => setPedalName(e.target.value)}
                className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm text-slate-100"
              />
            </div>
            <div className="flex gap-2">
              <div className="flex-1">
                <label className="block text-sm mb-1 text-slate-300">Width (units)</label>
                <input
                  type="number"
                  value={pedalWidth}
                  onChange={(e) => setPedalWidth(e.target.value)}
                  className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm text-slate-100"
                />
              </div>
              <div className="flex-1">
                <label className="block text-sm mb-1 text-slate-300">Height (units)</label>
                <input
                  type="number"
                  value={pedalHeight}
                  onChange={(e) => setPedalHeight(e.target.value)}
                  className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm text-slate-100"
                />
              </div>
            </div>
            <div>
              <label className="block text-sm mb-1 text-slate-300">Placement</label>
              <input
                type="number"
                value={pedalPlacement}
                onChange={(e) => setPedalPlacement(e.target.value)}
                className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm text-slate-100"
              />
            </div>
            <div>
              <label className="block text-sm mb-1 text-slate-300">Color</label>
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
                className="px-3 py-1.5 text-sm rounded border border-slate-600 text-slate-300 hover:bg-slate-700"
              >
                Cancel
              </button>
              <button
                onClick={handleAddPedal}
                className="px-3 py-1.5 text-sm rounded bg-emerald-500 hover:bg-emerald-400 text-white"
              >
                Save
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default App;
