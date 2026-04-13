import { useState, useCallback, useEffect } from "react";
import { User } from "./types";
import { authHeaders } from "./api";

const TOKEN_KEY = "pedalboard_token";
const USER_KEY = "pedalboard_user";

export function useAuth() {
  const [authToken, setAuthTokenState] = useState<string | null>(() =>
    localStorage.getItem(TOKEN_KEY)
  );

  const [currentUser, setCurrentUserState] = useState<User | null>(() => {
    try {
      const stored = localStorage.getItem(USER_KEY);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });

  const login = useCallback((token: string, user: User) => {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    setAuthTokenState(token);
    setCurrentUserState(user);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setAuthTokenState(null);
    setCurrentUserState(null);
  }, []);

  useEffect(() => {
    if (!authToken) return;
    let cancelled = false;
    (async () => {
      try {
        const res = await fetch("/api/users/me", { headers: authHeaders(authToken) });
        if (cancelled) return;
        if (res.ok) {
          const user = (await res.json()) as User;
          localStorage.setItem(USER_KEY, JSON.stringify(user));
          setCurrentUserState(user);
        } else if (res.status === 401) {
          logout();
        }
      } catch {
        // Keep cached user on transient errors
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [authToken, logout]);

  return { authToken, currentUser, login, logout };
}
