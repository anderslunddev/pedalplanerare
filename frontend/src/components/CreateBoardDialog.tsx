import React, { useState } from "react";
import { Board } from "../types";
import { authHeaders } from "../api";

type CreateBoardDialogProps = {
  userId: string;
  authToken: string | null;
  onCreated: (board: Board) => void;
  onClose: () => void;
};

const CreateBoardDialog: React.FC<CreateBoardDialogProps> = ({
  userId,
  authToken,
  onCreated,
  onClose,
}) => {
  const [boardName, setBoardName] = useState("");
  const [boardWidth, setBoardWidth] = useState("80");
  const [boardHeight, setBoardHeight] = useState("60");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCreate = async () => {
    setError(null);
    if (!boardName.trim()) {
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
        headers: authHeaders(authToken),
        body: JSON.stringify({ name: boardName.trim(), width: w, height: h, userId }),
      });
      if (!response.ok) {
        const errorText = await response.text();
        let message = `Request failed (${response.status})`;
        try {
          const err = JSON.parse(errorText);
          if (err.message) message = err.message;
          else if (err.fieldErrors && typeof err.fieldErrors === "object") {
            message = Object.entries(err.fieldErrors)
              .map(([k, v]) => `${k}: ${v}`)
              .join(". ");
          }
        } catch {
          if (errorText) message = errorText;
        }
        throw new Error(message);
      }
      const data: Board = await response.json();
      onCreated(data);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to create board.");
    } finally {
      setLoading(false);
    }
  };

  const submitOnEnter = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") handleCreate();
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-slate-800 rounded-lg p-6 w-full max-w-sm space-y-4">
        <h2 className="text-lg font-semibold text-slate-100">Create New Board</h2>
        {error && (
          <div className="p-3 bg-red-900/30 border border-red-700 rounded text-red-200 text-sm">
            {error}
          </div>
        )}
        <div>
          <label className="block text-sm mb-1 text-slate-300">Board Name</label>
          <input
            type="text"
            value={boardName}
            onChange={(e) => setBoardName(e.target.value)}
            onKeyDown={submitOnEnter}
            className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm text-slate-100"
            placeholder="e.g. My Pedalboard"
            autoFocus
          />
        </div>
        <div>
          <label className="block text-sm mb-1 text-slate-300">Board Width (cm)</label>
          <input
            type="number"
            value={boardWidth}
            onChange={(e) => setBoardWidth(e.target.value)}
            onKeyDown={submitOnEnter}
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
            onKeyDown={submitOnEnter}
            className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm text-slate-100"
            placeholder="60"
          />
        </div>
        <div className="flex justify-end gap-2 pt-2">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm rounded border border-slate-600 text-slate-300 hover:bg-slate-700"
          >
            Cancel
          </button>
          <button
            onClick={handleCreate}
            disabled={loading}
            className="px-4 py-2 text-sm rounded bg-indigo-500 hover:bg-indigo-400 disabled:opacity-60 text-white"
          >
            {loading ? "Creating..." : "Create"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default CreateBoardDialog;
