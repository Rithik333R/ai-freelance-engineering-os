import axiosClient from './axiosClient';

export const projectApi = {
  getProjects: async (page = 0, size = 20) => {
    const response = await axiosClient.get(`/projects?page=${page}&size=${size}`);
    return response.data;
  },
  getProjectById: async (id) => {
    const response = await axiosClient.get(`/projects/${id}`);
    return response.data;
  },
  createProject: async (projectData) => {
    const response = await axiosClient.post('/projects', projectData);
    return response.data;
  },
  updateProject: async (id, projectData) => {
    const response = await axiosClient.put(`/projects/${id}`, projectData);
    return response.data;
  },
  deleteProject: async (id) => {
    const response = await axiosClient.delete(`/projects/${id}`);
    return response.data;
  },
};
