import React, { useEffect, useState } from 'react';
import MainLayout from '../components/layout/MainLayout';
import { projectApi } from '../api/projectApi';
import { clientApi } from '../api/clientApi';
import { Link } from 'react-router-dom';

const ProjectsPage = () => {
  const [projects, setProjects] = useState([]);
  const [clients, setClients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingProject, setEditingProject] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    status: 'IN_PROGRESS',
    clientId: '',
    budget: '',
    startDate: '',
    targetEndDate: '',
  });
  const [error, setError] = useState('');

  const fetchData = async () => {
    setLoading(true);
    try {
      const [projRes, clientRes] = await Promise.all([
        projectApi.getProjects(0, 50),
        clientApi.getClients(0, 100),
      ]);
      setProjects(projRes.data?.content || []);
      setClients(clientRes.data?.content || []);
    } catch (err) {
      setError('Failed to fetch projects');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleOpenModal = (project = null) => {
    if (project) {
      setEditingProject(project);
      setFormData({
        name: project.name || '',
        description: project.description || '',
        status: project.status || 'IN_PROGRESS',
        clientId: project.clientId || '',
        budget: project.budget || '',
        startDate: project.startDate || '',
        targetEndDate: project.targetEndDate || '',
      });
    } else {
      setEditingProject(null);
      setFormData({
        name: '',
        description: '',
        status: 'IN_PROGRESS',
        clientId: '',
        budget: '',
        startDate: '',
        targetEndDate: '',
      });
    }
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        ...formData,
        clientId: formData.clientId ? formData.clientId : null,
        budget: formData.budget ? parseFloat(formData.budget) : null,
      };

      if (editingProject) {
        await projectApi.updateProject(editingProject.id, payload);
      } else {
        await projectApi.createProject(payload);
      }
      setShowModal(false);
      fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Operation failed');
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this project?')) {
      try {
        await projectApi.deleteProject(id);
        fetchData();
      } catch (err) {
        setError('Failed to delete project');
      }
    }
  };

  return (
    <MainLayout>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 700 }}>Projects</h1>
          <p style={{ color: '#94a3b8', fontSize: '0.875rem' }}>Manage software project lifecycles & client assignments</p>
        </div>
        <button onClick={() => handleOpenModal()} className="btn btn-primary" style={{ width: 'auto' }}>
          + New Project
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="card" style={{ padding: 0 }}>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Project Name</th>
                <th>Client</th>
                <th>Status</th>
                <th>Budget</th>
                <th>Target Deadline</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', color: '#94a3b8' }}>Loading projects...</td>
                </tr>
              ) : projects.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', color: '#94a3b8' }}>No projects found. Click "+ New Project" to create one.</td>
                </tr>
              ) : (
                projects.map((proj) => (
                  <tr key={proj.id}>
                    <td style={{ fontWeight: 600 }}>
                      <Link to={`/tasks?projectId=${proj.id}`}>{proj.name}</Link>
                    </td>
                    <td style={{ color: '#94a3b8' }}>{proj.clientName || 'Internal / Unassigned'}</td>
                    <td>
                      <span className={`badge badge-${proj.status.toLowerCase()}`}>{proj.status}</span>
                    </td>
                    <td style={{ color: '#94a3b8' }}>
                      {proj.budget ? `$${proj.budget.toLocaleString()}` : '-'}
                    </td>
                    <td style={{ color: '#94a3b8', fontSize: '0.85rem' }}>
                      {proj.targetEndDate ? new Date(proj.targetEndDate).toLocaleDateString() : '-'}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <Link
                        to={`/tasks?projectId=${proj.id}`}
                        className="btn btn-secondary"
                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem', marginRight: '0.5rem' }}
                      >
                        Tasks
                      </Link>
                      <button
                        onClick={() => handleOpenModal(proj)}
                        className="btn btn-secondary"
                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.8rem', marginRight: '0.5rem' }}
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDelete(proj.id)}
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

      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>{editingProject ? 'Edit Project' : 'Create New Project'}</h3>
              <button className="modal-close" onClick={() => setShowModal(false)}>&times;</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Project Name</label>
                <input
                  type="text"
                  className="form-control"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Assigned Client</label>
                <select
                  className="form-control"
                  value={formData.clientId}
                  onChange={(e) => setFormData({ ...formData, clientId: e.target.value })}
                >
                  <option value="">-- None / Internal Project --</option>
                  {clients.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.companyName}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Status</label>
                <select
                  className="form-control"
                  value={formData.status}
                  onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                >
                  <option value="DRAFT">DRAFT</option>
                  <option value="PLANNING">PLANNING</option>
                  <option value="IN_PROGRESS">IN_PROGRESS</option>
                  <option value="ON_HOLD">ON_HOLD</option>
                  <option value="COMPLETED">COMPLETED</option>
                  <option value="CANCELLED">CANCELLED</option>
                </select>
              </div>
              <div className="form-group">
                <label>Budget ($)</label>
                <input
                  type="number"
                  className="form-control"
                  value={formData.budget}
                  onChange={(e) => setFormData({ ...formData, budget: e.target.value })}
                />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label>Start Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={formData.startDate}
                    onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>Target End Date</label>
                  <input
                    type="date"
                    className="form-control"
                    value={formData.targetEndDate}
                    onChange={(e) => setFormData({ ...formData, targetEndDate: e.target.value })}
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
                  {editingProject ? 'Save Changes' : 'Create Project'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </MainLayout>
  );
};

export default ProjectsPage;
