import React, { useState } from "react";
import { User } from "../types";

type LoginPageProps = {
  onLogin: (token: string, user: User) => void;
};

const LoginPage: React.FC<LoginPageProps> = ({ onLogin }) => {
  const [loginUsername, setLoginUsername] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

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
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: loginUsername, password: loginPassword }),
      });
      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Login failed");
      }
      const data: { token: string; user: User } = await response.json();
      onLogin(data.token, data.user);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Login failed");
    } finally {
      setLoading(false);
    }
  };

  const submitOnEnter = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") handleLogin();
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-900">
      <div className="bg-slate-800 rounded-lg p-8 w-full max-w-md space-y-6 shadow-xl border border-slate-700">
        <div className="space-y-2 text-center">
          <h1 className="text-2xl font-semibold text-slate-100">
            Pedalboard Planner
          </h1>
          <p className="text-sm text-slate-400">
            Log in to see and manage your boards. Ask an administrator if you need an account.
          </p>
        </div>
        {error && (
          <div className="p-3 bg-red-900/30 border border-red-700 rounded text-red-200 text-sm">
            {error}
          </div>
        )}
        <div className="space-y-4">
          <div className="space-y-2">
            <label className="block text-sm font-medium text-slate-200">
              Username
            </label>
            <input
              type="text"
              value={loginUsername}
              onChange={(e) => setLoginUsername(e.target.value)}
              onKeyDown={submitOnEnter}
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
              onKeyDown={submitOnEnter}
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
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
