import React, { createContext, useState, useEffect, useContext } from 'react';
import { authApi } from '../api/authApi';
import { setColdStartListener } from '../api/axiosClient';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [loading, setLoading] = useState(true);
  const [isColdStarting, setIsColdStarting] = useState(false);

  useEffect(() => {
    setColdStartListener((wakingUp) => {
      setIsColdStarting(wakingUp);
    });

    const token = localStorage.getItem('accessToken');
    if (!token) {
      setUser(null);
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const response = await authApi.login({ email, password });
    if (response.success && response.data) {
      const { accessToken, refreshToken, user: userData } = response.data;
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
    }
    return response;
  };

  const register = async (email, password, fullName, role) => {
    const response = await authApi.register({ email, password, fullName, role });
    if (response.success && response.data) {
      const { accessToken, refreshToken, user: userData } = response.data;
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
    }
    return response;
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, isColdStarting, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
