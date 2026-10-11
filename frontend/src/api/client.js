const API_BASE = '/api';

export const getAuthToken = () => localStorage.getItem('devflow_token');
export const setAuthToken = (token) => localStorage.setItem('devflow_token', token);
export const removeAuthToken = () => {
  localStorage.removeItem('devflow_token');
  localStorage.removeItem('devflow_user');
};

export const getStoredUser = () => {
  const user = localStorage.getItem('devflow_user');
  return user ? JSON.parse(user) : null;
};
export const setStoredUser = (user) => localStorage.setItem('devflow_user', JSON.stringify(user));

async function request(endpoint, options = {}) {
  const token = getAuthToken();
  const headers = {
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    removeAuthToken();
    window.dispatchEvent(new CustomEvent('devflow_auth_expired'));
    throw new Error('Session expired or unauthorized. Please log in again.');
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || data.error || 'An unexpected error occurred');
    }
    return data;
  }

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || 'Request failed');
  }

  return response;
}

export const api = {
  // Authentication
  auth: {
    login: async (email, password) => {
      const data = await request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      });
      if (data.token) {
        setAuthToken(data.token);
        setStoredUser({
          id: data.userId,
          email: data.email,
          role: data.role,
        });
      }
      return data;
    },
    register: async (email, password, role = 'DEVELOPER') => {
      return request('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ email, password, role }),
      });
    },
  },

  // Organizations
  organizations: {
    list: () => request('/organizations'),
    get: (orgId) => request(`/organizations/${orgId}`),
    create: (name, description) =>
      request('/organizations', {
        method: 'POST',
        body: JSON.stringify({ name, description }),
      }),
    addMember: (orgId, userEmail, role = 'DEVELOPER') =>
      request(`/organizations/${orgId}/members`, {
        method: 'POST',
        body: JSON.stringify({ userEmail, role }),
      }),
    updateMemberRole: (orgId, userId, role) =>
      request(`/organizations/${orgId}/members/${userId}`, {
        method: 'PATCH',
        body: JSON.stringify({ role }),
      }),
    removeMember: (orgId, userId) =>
      request(`/organizations/${orgId}/members/${userId}`, {
        method: 'DELETE',
      }),
  },

  // Projects
  projects: {
    list: (orgId) => request(`/organizations/${orgId}/projects`),
    get: (orgId, projectId) => request(`/organizations/${orgId}/projects/${projectId}`),
    create: (orgId, name, description, status = 'ACTIVE') =>
      request(`/organizations/${orgId}/projects`, {
        method: 'POST',
        body: JSON.stringify({ name, description, status }),
      }),
    addMember: (orgId, projectId, userEmail, projectRole = 'CONTRIBUTOR') =>
      request(`/organizations/${orgId}/projects/${projectId}/members`, {
        method: 'POST',
        body: JSON.stringify({ userEmail, projectRole }),
      }),
    removeMember: (orgId, projectId, userId) =>
      request(`/organizations/${orgId}/projects/${projectId}/members/${userId}`, {
        method: 'DELETE',
      }),
  },

  // Tasks
  tasks: {
    list: (orgId, projectId, status, assigneeId) => {
      const params = new URLSearchParams();
      if (status) params.append('status', status);
      if (assigneeId) params.append('assigneeId', assigneeId);
      const query = params.toString() ? `?${params.toString()}` : '';
      return request(`/organizations/${orgId}/projects/${projectId}/tasks${query}`);
    },
    get: (orgId, projectId, taskId) =>
      request(`/organizations/${orgId}/projects/${projectId}/tasks/${taskId}`),
    create: (orgId, projectId, data) =>
      request(`/organizations/${orgId}/projects/${projectId}/tasks`, {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    update: (orgId, projectId, taskId, data) =>
      request(`/organizations/${orgId}/projects/${projectId}/tasks/${taskId}`, {
        method: 'PATCH',
        body: JSON.stringify(data),
      }),
    delete: (orgId, projectId, taskId) =>
      request(`/organizations/${orgId}/projects/${projectId}/tasks/${taskId}`, {
        method: 'DELETE',
      }),
  },

  // Comments
  comments: {
    list: (orgId, projectId, taskId) =>
      request(`/organizations/${orgId}/projects/${projectId}/tasks/${taskId}/comments`),
    create: (orgId, projectId, taskId, content) =>
      request(`/organizations/${orgId}/projects/${projectId}/tasks/${taskId}/comments`, {
        method: 'POST',
        body: JSON.stringify({ content }),
      }),
  },

  // Attachments
  attachments: {
    list: (orgId, projectId, taskId) =>
      request(`/organizations/${orgId}/projects/${projectId}/tasks/${taskId}/attachments`),
    upload: (orgId, projectId, taskId, file) => {
      const formData = new FormData();
      formData.append('file', file);
      return request(`/organizations/${orgId}/projects/${projectId}/tasks/${taskId}/attachments`, {
        method: 'POST',
        body: formData,
      });
    },
    downloadUrl: (orgId, projectId, taskId, attachmentId) =>
      `${API_BASE}/organizations/${orgId}/projects/${projectId}/tasks/${taskId}/attachments/${attachmentId}/download`,
  },

  // Notifications
  notifications: {
    list: (unreadOnly = false, page = 0, size = 20) =>
      request(`/notifications?unreadOnly=${unreadOnly}&page=${page}&size=${size}`),
    markRead: (id) =>
      request(`/notifications/${id}/read`, {
        method: 'PATCH',
      }),
    markAllRead: () =>
      request('/notifications/read-all', {
        method: 'PATCH',
      }),
  },
};
