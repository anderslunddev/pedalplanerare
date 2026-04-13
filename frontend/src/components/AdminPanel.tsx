import React, { useState, useEffect, useCallback } from "react";
import { authHeaders } from "../api";

type AdminUser = {
  id: string;
  username: string;
  email: string;
  role: string;
};

type AdminPanelProps = {
  authToken: string | null;
  currentUserId: string;
  onBack: () => void;
  onOpenChangePassword?: () => void;
};

const AdminPanel: React.FC<AdminPanelProps> = ({ authToken, currentUserId, onBack, onOpenChangePassword }) => {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newUsername, setNewUsername] = useState("");
  const [newEmail, setNewEmail] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [newRole, setNewRole] = useState("USER");
  const [creating, setCreating] = useState(false);

  const [resetPasswordUserId, setResetPasswordUserId] = useState<string | null>(null);
  const [resetPasswordValue, setResetPasswordValue] = useState("");

  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);

  const flash = (msg: string) => {
    setSuccessMsg(msg);
    setTimeout(() => setSuccessMsg(null), 3000);
  };

  const fetchUsers = useCallback(async () => {
    setError(null);
    try {
      const res = await fetch("/api/admin/users", { headers: authHeaders(authToken) });
      if (!res.ok) throw new Error("Failed to load users");
      setUsers(await res.json());
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to load users");
    } finally {
      setLoading(false);
    }
  }, [authToken]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleCreate = async () => {
    setError(null);
    if (!newUsername || !newEmail || !newPassword) {
      setError("All fields are required.");
      return;
    }
    setCreating(true);
    try {
      const res = await fetch("/api/admin/users", {
        method: "POST",
        headers: authHeaders(authToken),
        body: JSON.stringify({ username: newUsername.trim(), email: newEmail.trim(), password: newPassword, role: newRole }),
      });
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "Failed to create user");
      }
      setShowCreateForm(false);
      setNewUsername("");
      setNewEmail("");
      setNewPassword("");
      setNewRole("USER");
      flash("User created.");
      await fetchUsers();
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to create user");
    } finally {
      setCreating(false);
    }
  };

  const handleToggleRole = async (user: AdminUser) => {
    setError(null);
    const newUserRole = user.role === "ADMIN" ? "USER" : "ADMIN";
    try {
      const res = await fetch(`/api/admin/users/${user.id}/role`, {
        method: "PUT",
        headers: authHeaders(authToken),
        body: JSON.stringify({ role: newUserRole }),
      });
      if (!res.ok) throw new Error("Failed to update role");
      flash(`${user.username} is now ${newUserRole}.`);
      await fetchUsers();
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to update role");
    }
  };

  const handleResetPassword = async (userId: string) => {
    setError(null);
    if (!resetPasswordValue || resetPasswordValue.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }
    try {
      const res = await fetch(`/api/admin/users/${userId}/password`, {
        method: "PUT",
        headers: authHeaders(authToken),
        body: JSON.stringify({ password: resetPasswordValue }),
      });
      if (!res.ok) throw new Error("Failed to reset password");
      setResetPasswordUserId(null);
      setResetPasswordValue("");
      flash("Password reset.");
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to reset password");
    }
  };

  const handleDelete = async (userId: string) => {
    setError(null);
    try {
      const res = await fetch(`/api/admin/users/${userId}`, {
        method: "DELETE",
        headers: authHeaders(authToken),
      });
      if (!res.ok) throw new Error("Failed to delete user");
      setConfirmDeleteId(null);
      flash("User deleted.");
      await fetchUsers();
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to delete user");
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 p-6">
      <div className="max-w-4xl mx-auto space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-semibold text-slate-100">User Management</h1>
          <div className="flex gap-2">
            <button
              onClick={() => setShowCreateForm(!showCreateForm)}
              className="inline-flex items-center justify-center rounded bg-emerald-500 px-4 py-2 text-sm font-medium hover:bg-emerald-400"
            >
              {showCreateForm ? "Cancel" : "Create User"}
            </button>
            {onOpenChangePassword && (
              <button
                type="button"
                onClick={onOpenChangePassword}
                className="inline-flex items-center justify-center rounded border border-slate-600 px-4 py-2 text-sm font-medium text-slate-200 hover:bg-slate-700"
              >
                Change password
              </button>
            )}
            <button
              onClick={onBack}
              className="inline-flex items-center justify-center rounded border border-slate-600 px-4 py-2 text-sm font-medium text-slate-200 hover:bg-slate-700"
            >
              Back to Boards
            </button>
          </div>
        </div>

        {error && (
          <div className="p-3 bg-red-900/30 border border-red-700 rounded text-red-200 text-sm">{error}</div>
        )}
        {successMsg && (
          <div className="p-3 bg-emerald-900/30 border border-emerald-700 rounded text-emerald-200 text-sm">{successMsg}</div>
        )}

        {showCreateForm && (
          <div className="bg-slate-800 border border-slate-700 rounded-lg p-6 space-y-4">
            <h2 className="text-lg font-medium text-slate-200">Create New User</h2>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1">
                <label className="block text-sm text-slate-300">Username</label>
                <input
                  type="text" value={newUsername} onChange={(e) => setNewUsername(e.target.value)}
                  className="w-full rounded-md bg-slate-900 border border-slate-700 px-3 py-2 text-sm text-slate-100 focus:outline-none focus:ring-1 focus:ring-indigo-400"
                />
              </div>
              <div className="space-y-1">
                <label className="block text-sm text-slate-300">Email</label>
                <input
                  type="email" value={newEmail} onChange={(e) => setNewEmail(e.target.value)}
                  className="w-full rounded-md bg-slate-900 border border-slate-700 px-3 py-2 text-sm text-slate-100 focus:outline-none focus:ring-1 focus:ring-indigo-400"
                />
              </div>
              <div className="space-y-1">
                <label className="block text-sm text-slate-300">Password</label>
                <input
                  type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full rounded-md bg-slate-900 border border-slate-700 px-3 py-2 text-sm text-slate-100 focus:outline-none focus:ring-1 focus:ring-indigo-400"
                />
              </div>
              <div className="space-y-1">
                <label className="block text-sm text-slate-300">Role</label>
                <select
                  value={newRole} onChange={(e) => setNewRole(e.target.value)}
                  className="w-full rounded-md bg-slate-900 border border-slate-700 px-3 py-2 text-sm text-slate-100 focus:outline-none focus:ring-1 focus:ring-indigo-400"
                >
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>
            </div>
            <button
              onClick={handleCreate} disabled={creating}
              className="inline-flex items-center justify-center rounded bg-indigo-500 px-4 py-2 text-sm font-medium hover:bg-indigo-400 disabled:opacity-60"
            >
              {creating ? "Creating..." : "Create"}
            </button>
          </div>
        )}

        {loading ? (
          <p className="text-slate-400">Loading users...</p>
        ) : (
          <div className="bg-slate-800 border border-slate-700 rounded-lg overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-xs text-slate-400 border-b border-slate-700 bg-slate-800/80">
                  <th className="text-left px-4 py-3 font-medium">Username</th>
                  <th className="text-left px-4 py-3 font-medium">Email</th>
                  <th className="text-left px-4 py-3 font-medium">Role</th>
                  <th className="text-right px-4 py-3 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <React.Fragment key={user.id}>
                    <tr className="border-b border-slate-700/50 last:border-0 hover:bg-slate-700/30">
                      <td className="px-4 py-3 text-slate-200 font-medium">{user.username}</td>
                      <td className="px-4 py-3 text-slate-300">{user.email}</td>
                      <td className="px-4 py-3">
                        <span className={`inline-block px-2 py-0.5 rounded text-xs font-semibold ${user.role === "ADMIN" ? "bg-amber-500/20 text-amber-300" : "bg-slate-600/40 text-slate-300"}`}>
                          {user.role}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right space-x-2">
                        {user.id !== currentUserId && (
                          <>
                            <button
                              onClick={() => handleToggleRole(user)}
                              className="text-xs px-2 py-1 rounded border border-slate-600 text-slate-300 hover:bg-slate-700"
                            >
                              {user.role === "ADMIN" ? "Demote" : "Promote"}
                            </button>
                            <button
                              onClick={() => { setResetPasswordUserId(user.id); setResetPasswordValue(""); }}
                              className="text-xs px-2 py-1 rounded border border-slate-600 text-slate-300 hover:bg-slate-700"
                            >
                              Reset pw
                            </button>
                            {confirmDeleteId === user.id ? (
                              <>
                                <button
                                  onClick={() => handleDelete(user.id)}
                                  className="text-xs px-2 py-1 rounded bg-red-600 text-white hover:bg-red-500"
                                >
                                  Confirm
                                </button>
                                <button
                                  onClick={() => setConfirmDeleteId(null)}
                                  className="text-xs px-2 py-1 rounded border border-slate-600 text-slate-300 hover:bg-slate-700"
                                >
                                  Cancel
                                </button>
                              </>
                            ) : (
                              <button
                                onClick={() => setConfirmDeleteId(user.id)}
                                className="text-xs px-2 py-1 rounded border border-red-700 text-red-300 hover:bg-red-900/40"
                              >
                                Delete
                              </button>
                            )}
                          </>
                        )}
                        {user.id === currentUserId && (
                          <span className="text-xs text-slate-500 italic">You</span>
                        )}
                      </td>
                    </tr>
                    {resetPasswordUserId === user.id && (
                      <tr className="bg-slate-800/60">
                        <td colSpan={4} className="px-4 py-3">
                          <div className="flex items-center gap-3">
                            <span className="text-sm text-slate-300">New password for {user.username}:</span>
                            <input
                              type="password" value={resetPasswordValue}
                              onChange={(e) => setResetPasswordValue(e.target.value)}
                              placeholder="Min 8 characters"
                              className="rounded-md bg-slate-900 border border-slate-700 px-3 py-1.5 text-sm text-slate-100 focus:outline-none focus:ring-1 focus:ring-indigo-400 w-56"
                            />
                            <button
                              onClick={() => handleResetPassword(user.id)}
                              className="text-xs px-3 py-1.5 rounded bg-indigo-500 text-white hover:bg-indigo-400"
                            >
                              Reset
                            </button>
                            <button
                              onClick={() => setResetPasswordUserId(null)}
                              className="text-xs px-3 py-1.5 rounded border border-slate-600 text-slate-300 hover:bg-slate-700"
                            >
                              Cancel
                            </button>
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminPanel;
