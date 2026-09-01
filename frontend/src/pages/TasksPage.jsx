import React, { useEffect, useState, useCallback } from 'react';
import MainLayout from '../components/layout/MainLayout';
import { taskApi } from '../api/taskApi';
import { projectApi } from '../api/projectApi';
import { useSearchParams } from 'react-router-dom';

const TasksPage = () => {
  const [searchParams] = useSearchParams();
  const initialProjectId = searchParams.get('projectId') || '';

  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState(initialProjectId);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    status: 'TODO',
    priority: 'MEDIUM',
    estimatedHours: '',
    dueDate: '',
  });
  const [error, setError] = useState('');

  const fetchProjects = async () => {
    try {
      const res = await projectApi.getProjects(0, 100);
      const projList = res.data?.content || [];
      setProjects(projList);
      if (!selectedProjectId && projList.length > 0) {
        setSelectedProjectId(projList[0].id);
      }
    } catch (err) {
      setError('Failed to fetch projects');
    }
  };

  const fetchTasks = useCallback(async () => {
    if (!selectedProjectId) return;
    setLoading(true);
    try {
      const res = await taskApi.getTasksByProject(selectedProjectId, 0, 100);
      setTasks(res.data?.content || []);
    } catch (err) {
      setError('Failed to fetch tasks for selected project');
    } finally {
      setLoading(false);
    }
  }, [selectedProjectId]);

  useEffect(() => {
    fetchProjects();
  }, []);

  useEffect(() => {
    if (selectedProjectId) {
      fetchTasks();
    }
  }, [selectedProjectId, fetchTasks]);

  const handleOpenModal = (task = null) => {
    if (task) {
      setEditingTask(task);
      setFormData({
        title: task.title || '',
        description: task.description || '',
        status: task.status || 'TODO',
        priority: task.priority || 'MEDIUM',
        estimatedHours: task.estimatedHours || '',
        dueDate: task.dueDate || '',
      });
    } else {
      setEditingTask(null);
      setFormData({
        title: '',
        description: '',
        status: 'TODO',
        priority: 'MEDIUM',
        estimatedHours: '',
        dueDate: '',
      });
    }
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedProjectId) {
      setError('Please select a project first');
      return;
    }

    try {
      const payload = {
        ...formData,
        estimatedHours: formData.estimatedHours ? parseInt(formData.estimatedHours, 10) : null,
      };

      if (editingTask) {
        await taskApi.updateTask(editingTask.id, payload);
      } else {
        await taskApi.createTask(selectedProjectId, payload);
      }
      setShowModal(false);
      fetchTasks();
    } catch (err) {
      setError(err.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this task?')) {
      try {
        await taskApi.deleteTask(id);
        fetchTasks();
      } catch (err) {
        setError('Failed to delete task');
      }
    }
  };

  return (
    <MainLayout>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 700 }}>Project Tasks</h1>
          <p style={{ color: '#94a3b8', fontSize: '0.875rem' }}>Track task execution, priorities, and project milestones</p>
        </div>
        {selectedProjectId && (
          <button onClick={() => handleOpenModal()} className="btn btn-primary" style={{ width: 'auto' }}>
            + Add Task
          </button>
        )}
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem 1.5rem' }}>
        <div className="form-group" style={{ margin: 0 }}>
          <label style={{ marginBottom: '0.25rem' }}>Select Active Project</label>
          <select
            className="form-control"
            value={selectedProjectId}
            onChange={(e) => setSelectedProjectId(e.target.value)}
          >
            <option value="">-- Choose Project --</option>
            {projects.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name} ({p.status})
              </option>
            ))}
          </select>
        </div>
      </div>

      {selectedProjectId ? (
        <div className="card" style={{ padding: 0 }}>
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Task Title</th>
                  <th>Status</th>
                  <th>Priority</th>
                  <th>Est. Hours</th>
                  <th>Due Date</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="6" style={{ textAlign: 'center', color: '#94a3b8' }}>Loading tasks...</td>
                  </tr>
                ) : tasks.length === 0 ? (
                  <tr>
                    <td colSpan="6" style={{ textAlign: 'center', color: '#94a3b8' }}>No tasks found in this project. Click "+ Add Task" to create one.</td>
                  </tr>
                ) : (
                  tasks.map((task) => (
                    <tr key={task.id}>
                      <td style={{ fontWeight: 600 }}>{task.title}</td>
                      <td>
                        <span className={`badge badge-${task.status.toLowerCase()}`}>{task.status}</span>
                      </td>
                      <td>
                        <span className={`badge badge-${task.priority.toLowerCase()}`}>{task.priority}</span>
                      </td>
                      <td style={{ color: '#94a3b8' }}>{task.estimatedHours ? `${task.estimatedHours} hrs` : '-'}</td>
                      <td style={{ color: '#94a3b8', fontSize: '0.85rem' }}>
                        {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : '-'}
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <button
                          onClick={() => handleOpenModal(task)}
                          className="btn btn-secondary"
                          style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem', marginRight: '0.5rem' }}
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => handleDelete(task.id)}
                          className="btn btn-danger"
                          style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem' }}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="card" style={{ textAlign: 'center', color: '#94a3b8', padding: '3rem' }}>
          Please select a project above to view and manage tasks.
        </div>
      )}

      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>{editingTask ? 'Edit Task' : 'Add New Task'}</h3>
              <button className="modal-close" onClick={() => setShowModal(false)}>&times;</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Task Title</label>
                <input
                  type="text"
                  className="form-control"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  required
                />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label>Status</label>
                  <select
                    className="form-control"
                    value={formData.status}
                    onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                  >
                    <option value="TODO">TODO</option>
                    <option value="IN_PROGRESS">IN_PROGRESS</option>
                    <option value="IN_REVIEW">IN_REVIEW</option>
                    <option value="DONE">DONE</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Priority</label>
                  <select
                    className="form-control"
                    value={formData.priority}
                    onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                  >
                    <option value="LOW">LOW</option>
                    <option value="MEDIUM">MEDIUM</option>
                    <option value="HIGH">HIGH</option>
                    <option value="CRITICAL">CRITICAL</option>
                  </select>
                </div>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label>Estimated Hours</label>
                  <input
                    type="number"
                    className="form-control"
                    value={formData.estimatedHours}
                    onChange={(e) => setFormData({ ...formData, estimatedHours: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>Due Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={formData.dueDate}
                    onChange={(e) => setFormData({ ...formData, dueDate: e.target.value })}
                  />
                </div>
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea
                  className="form-control"
                  rows="3"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                ></textarea>
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.5rem' }}>
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" style={{ width: 'auto' }}>
                  {editingTask ? 'Save Changes' : 'Create Task'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </MainLayout>
  );
};

export default TasksPage;
