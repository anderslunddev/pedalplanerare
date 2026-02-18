import React, { useEffect, useState } from "react";

type BoardSummary = {
  id: string;
  name: string;
};

type SidebarProps = {
  activeBoardId: string | null;
  onBoardSelect: (boardId: string) => void;
  userId: string | null;
  authToken: string | null;
  refreshKey: number;
};

const Sidebar: React.FC<SidebarProps> = ({ activeBoardId, onBoardSelect, userId, authToken, refreshKey }) => {
  const [boards, setBoards] = useState<BoardSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchBoards = async () => {
      if (!userId) {
        setBoards([]);
        setLoading(false);
        setError(null);
        return;
      }
      setLoading(true);
      setError(null);
      try {
        const headers: HeadersInit = {};
        if (authToken) {
          headers["Authorization"] = `Bearer ${authToken}`;
        }
        const response = await fetch(`/api/boards/user/${userId}`, {
          headers,
        });
        if (!response.ok) {
          throw new Error("Failed to fetch boards");
        }
        const data: BoardSummary[] = await response.json();
        setBoards(data);
      } catch (e: unknown) {
        setError(e instanceof Error ? e.message : "Failed to load boards");
      } finally {
        setLoading(false);
      }
    };

    fetchBoards();
  }, [userId, authToken, refreshKey]);

  return (
    <div className="w-1/5 h-screen bg-slate-800 border-r border-slate-700 flex flex-col">
      <div className="p-4 border-b border-slate-700">
        <h2 className="text-lg font-semibold text-slate-100">Boards</h2>
      </div>
      <div className="flex-1 overflow-y-auto">
        {loading ? (
          <div className="p-4 text-sm text-slate-400">Loading...</div>
        ) : error ? (
          <div className="p-4 text-sm text-red-400">{error}</div>
        ) : boards.length === 0 ? (
          <div className="p-4 text-sm text-slate-400">No boards yet</div>
        ) : (
          <ul className="py-2">
            {boards.map((board) => (
              <li key={board.id}>
                <button
                  onClick={() => onBoardSelect(board.id)}
                  className={`w-full text-left px-4 py-2 text-sm hover:bg-slate-700 transition-colors ${
                    activeBoardId === board.id
                      ? "bg-slate-700 text-indigo-400 font-medium"
                      : "text-slate-300"
                  }`}
                >
                  {board.name}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default Sidebar;
