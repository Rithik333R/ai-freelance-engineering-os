import axiosClient from './axiosClient';

export const clientApi = {
  getClients: async (page = 0, size = 20) => {
    const response = await axiosClient.get(`/clients?page=${page}&size=${size}`);
    return response.data;
  },
  getClientById: async (id) => {
    const response = await axiosClient.get(`/clients/${id}`);
    return response.data;
  },
  createClient: async (clientData) => {
    const response = await axiosClient.post('/clients', clientData);
    return response.data;
  },
  updateClient: async (id, clientData) => {
    const response = await axiosClient.put(`/clients/${id}`, clientData);
    return response.data;
  },
  deleteClient: async (id) => {
    const response = await axiosClient.delete(`/clients/${id}`);
    return response.data;
  },
};
