import { useState, useCallback } from "react";
import { User } from "./types";

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

  return { authToken, currentUser, login, logout };
}
