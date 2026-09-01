import axiosClient from './axiosClient';

export const taskApi = {
  getTasksByProject: async (projectId, page = 0, size = 50) => {
    const response = await axiosClient.get(`/projects/${projectId}/tasks?page=${page}&size=${size}`);
    return response.data;
  },
  createTask: async (projectId, taskData) => {
    const response = await axiosClient.post(`/projects/${projectId}/tasks`, taskData);
    return response.data;
  },
  updateTask: async (id, taskData) => {
    const response = await axiosClient.put(`/tasks/${id}`, taskData);
    return response.data;
  },
  deleteTask: async (id) => {
    const response = await axiosClient.delete(`/tasks/${id}`);
    return response.data;
  },
};
