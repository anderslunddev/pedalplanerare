import React, { useState } from "react";
import { authHeaders } from "../api";

type ChangePasswordDialogProps = {
  authToken: string;
  onClose: () => void;
};

const ChangePasswordDialog: React.FC<ChangePasswordDialogProps> = ({ authToken, onClose }) => {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async () => {
    setError(null);
    if (!currentPassword || !newPassword) {
      setError("Fill in all fields.");
      return;
    }
    if (newPassword.length < 8) {
      setError("New password must be at least 8 characters.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("New passwords do not match.");
      return;
    }
    setLoading(true);
    try {
      const res = await fetch("/api/users/me/password", {
        method: "PUT",
        headers: authHeaders(authToken),
        body: JSON.stringify({ currentPassword, newPassword }),
      });
      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        const msg =
          typeof data.message === "string" ? data.message : "Could not change password.";
        throw new Error(msg);
      }
      setSuccess(true);
      setTimeout(() => onClose(), 1200);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Could not change password.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
      <div className="bg-slate-800 border border-slate-600 rounded-lg p-6 w-full max-w-md shadow-xl space-y-4">
        <h2 className="text-lg font-semibold text-slate-100">Change password</h2>
        {error && (
          <div className="p-2 rounded bg-red-900/30 border border-red-700 text-red-200 text-sm">
            {error}
          </div>
        )}
        {success && (
          <div className="p-2 rounded bg-emerald-900/30 border border-emerald-700 text-emerald-200 text-sm">
            Password updated.
          </div>
        )}
        <div className="space-y-3">
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1">Current password</label>
            <input
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              className="w-full rounded bg-slate-900 border border-slate-600 px-3 py-2 text-sm text-slate-100"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1">New password (min 8)</label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="w-full rounded bg-slate-900 border border-slate-600 px-3 py-2 text-sm text-slate-100"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1">Confirm new password</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="w-full rounded bg-slate-900 border border-slate-600 px-3 py-2 text-sm text-slate-100"
            />
          </div>
        </div>
        <div className="flex justify-end gap-2 pt-2">
          <button
            type="button"
            onClick={onClose}
            className="px-3 py-1.5 text-sm rounded border border-slate-600 text-slate-300 hover:bg-slate-700"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={loading || success}
            className="px-3 py-1.5 text-sm rounded bg-indigo-500 text-white hover:bg-indigo-400 disabled:opacity-50"
          >
            {loading ? "Saving..." : "Save"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ChangePasswordDialog;
