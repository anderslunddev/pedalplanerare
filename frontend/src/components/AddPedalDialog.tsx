import React, { useState } from "react";
import { Pedal } from "../types";
import { authHeaders } from "../api";
import { findDefaultPosition } from "../utils";

type AddPedalDialogProps = {
  boardId: string;
  boardWidth: number;
  boardHeight: number;
  existingPedals: Pedal[];
  authToken: string | null;
  defaultPlacement: number;
  onCreated: (pedal: Pedal) => void;
  onClose: () => void;
};

const AddPedalDialog: React.FC<AddPedalDialogProps> = ({
  boardId,
  boardWidth,
  boardHeight,
  existingPedals,
  authToken,
  defaultPlacement,
  onCreated,
  onClose,
}) => {
  const [pedalName, setPedalName] = useState("");
  const [pedalWidth, setPedalWidth] = useState("10");
  const [pedalHeight, setPedalHeight] = useState("10");
  const [pedalColor, setPedalColor] = useState("#4f46e5");
  const [pedalPlacement, setPedalPlacement] = useState(String(defaultPlacement));
  const [error, setError] = useState<string | null>(null);

  const handleSave = async () => {
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
    if (existingPedals.some((p) => (p.placement ?? 0) === placementNum)) {
      setError(`Placement number ${placementNum} is already in use.`);
      return;
    }

    const { x, y } = findDefaultPosition(boardWidth, boardHeight, existingPedals, w, h);

    try {
      const response = await fetch(`/api/boards/${boardId}/pedals`, {
        method: "POST",
        headers: authHeaders(authToken),
        body: JSON.stringify({
          name: pedalName,
          width: w,
          height: h,
          color: pedalColor,
          x,
          y,
          placement: placementNum,
        }),
      });
      if (!response.ok) {
        throw new Error(`Failed to add pedal (${response.status})`);
      }
      const created: Pedal = await response.json();
      onCreated(created);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to add pedal.");
    }
  };

  const submitOnEnter = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") handleSave();
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-slate-800 rounded-lg p-4 w-full max-w-sm space-y-3">
        <h2 className="text-lg font-semibold text-slate-100">Add Pedal</h2>
        {error && (
          <div className="p-3 bg-red-900/30 border border-red-700 rounded text-red-200 text-sm">
            {error}
          </div>
        )}
        <div>
          <label className="block text-sm mb-1 text-slate-300">Name</label>
          <input
            type="text"
            value={pedalName}
            onChange={(e) => setPedalName(e.target.value)}
            onKeyDown={submitOnEnter}
            className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm text-slate-100"
            autoFocus
          />
        </div>
        <div className="flex gap-2">
          <div className="flex-1">
            <label className="block text-sm mb-1 text-slate-300">Width (units)</label>
            <input
              type="number"
              value={pedalWidth}
              onChange={(e) => setPedalWidth(e.target.value)}
              onKeyDown={submitOnEnter}
              className="w-full rounded border border-slate-600 bg-slate-900 px-2 py-1 text-sm text-slate-100"
            />
          </div>
          <div className="flex-1">
            <label className="block text-sm mb-1 text-slate-300">Height (units)</label>
            <input
              type="number"
              value={pedalHeight}
              onChange={(e) => setPedalHeight(e.target.value)}
              onKeyDown={submitOnEnter}
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
            onKeyDown={submitOnEnter}
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
            onClick={onClose}
            className="px-3 py-1.5 text-sm rounded border border-slate-600 text-slate-300 hover:bg-slate-700"
          >
            Cancel
          </button>
          <button
            onClick={handleSave}
            className="px-3 py-1.5 text-sm rounded bg-emerald-500 hover:bg-emerald-400 text-white"
          >
            Save
          </button>
        </div>
      </div>
    </div>
  );
};

export default AddPedalDialog;
