import axiosClient from './axiosClient';

export const conversationApi = {
  createConversation: async (title) => {
    const response = await axiosClient.post('/ai/conversations', { title });
    return response.data;
  },

  getConversations: async () => {
    const response = await axiosClient.get('/ai/conversations');
    return response.data;
  },

  getConversationById: async (id) => {
    const response = await axiosClient.get(`/ai/conversations/${id}`);
    return response.data;
  },

  deleteConversation: async (id) => {
    const response = await axiosClient.delete(`/ai/conversations/${id}`);
    return response.data;
  },

  sendMessage: async (conversationId, message) => {
    const response = await axiosClient.post(`/ai/conversations/${conversationId}/messages`, { message });
    return response.data;
  },
};
