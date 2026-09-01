import axiosClient from './axiosClient';

export const authApi = {
  login: async (credentials) => {
    const response = await axiosClient.post('/auth/login', credentials);
    return response.data;
  },
  register: async (userData) => {
    const response = await axiosClient.post('/auth/register', userData);
    return response.data;
  },
  refreshToken: async (token) => {
    const response = await axiosClient.post('/auth/refresh', { refreshToken: token });
    return response.data;
  },
  checkHealth: async () => {
    const response = await axiosClient.get('/auth/health');
    return response.data;
  },
};
