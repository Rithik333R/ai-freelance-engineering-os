import React, { useEffect, useState } from 'react';
import MainLayout from '../components/layout/MainLayout';
import { clientApi } from '../api/clientApi';
import { projectApi } from '../api/projectApi';
import { conversationApi } from '../api/conversationApi';
import { Link, useNavigate } from 'react-router-dom';

const DashboardPage = () => {
  const [clientCount, setClientCount] = useState(0);
  const [projectCount, setProjectCount] = useState(0);
  const [convCount, setConvCount] = useState(0);
  const [recentProjects, setRecentProjects] = useState([]);
  const [quickPrompt, setQuickPrompt] = useState('');
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const [clientsRes, projectsRes, convsRes] = await Promise.all([
          clientApi.getClients(0, 1),
          projectApi.getProjects(0, 5),
          conversationApi.getConversations(),
        ]);

        setClientCount(clientsRes.data?.totalElements || 0);
        setProjectCount(projectsRes.data?.totalElements || 0);
        setRecentProjects(projectsRes.data?.content || []);
        if (convsRes.success && convsRes.data) {
          setConvCount(convsRes.data.length);
        }
      } catch (err) {
        console.error('Failed to load dashboard metrics', err);
      } finally {
        setLoading(false);
      }
    };
    fetchDashboardData();
  }, []);

  const handleQuickAiSubmit = (e) => {
    e.preventDefault();
    if (!quickPrompt.trim()) return;
    navigate('/ai-chat');
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'IN_PROGRESS': return 'badge badge-primary';
      case 'COMPLETED': return 'badge badge-success';
      case 'ON_HOLD': return 'badge badge-warning';
      default: return 'badge badge-secondary';
    }
  };

  return (
    <MainLayout>
      <div style={{ marginBottom: '1.75rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700 }}>AI Workspace Control Center</h1>
        <p style={{ color: 'var(--text-secondary)' }}>
          Real-time overview of active freelance client projects, tasks, and AI intelligence
        </p>
      </div>

      {/* 1. Metric Cards Grid */}
      <div className="grid-4" style={{ marginBottom: '1.5rem' }}>
        <div className="card">
          <div className="card-title">Total Active Clients</div>
          <div className="card-value">{loading ? '...' : clientCount}</div>
          <div style={{ marginTop: '0.75rem' }}>
            <Link to="/clients" style={{ fontSize: '0.85rem' }}>Manage Clients →</Link>
          </div>
        </div>

        <div className="card">
          <div className="card-title">Active Projects</div>
          <div className="card-value">{loading ? '...' : projectCount}</div>
          <div style={{ marginTop: '0.75rem' }}>
            <Link to="/projects" style={{ fontSize: '0.85rem' }}>View Projects →</Link>
          </div>
        </div>

        <div className="card">
          <div className="card-title">Technical Tasks</div>
          <div className="card-value">{loading ? '...' : 'Tasks'}</div>
          <div style={{ marginTop: '0.75rem' }}>
            <Link to="/tasks" style={{ fontSize: '0.85rem' }}>View Tasks →</Link>
          </div>
        </div>

        <div className="card">
          <div className="card-title">AI Conversations</div>
          <div className="card-value">{loading ? '...' : convCount}</div>
          <div style={{ marginTop: '0.75rem' }}>
            <Link to="/ai-chat" style={{ fontSize: '0.85rem' }}>Open AI Chat →</Link>
          </div>
        </div>
      </div>

      {/* 2. Quick AI Command Shortcut Banner */}
      <div className="card" style={{ marginBottom: '1.75rem', borderColor: 'var(--accent-blue)', backgroundColor: 'var(--bg-card)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.75rem' }}>
          <span style={{ fontSize: '1.25rem' }}>⚡</span>
          <h3 style={{ fontSize: '1.05rem', fontWeight: 600, margin: 0 }}>Quick AI Command Bar</h3>
        </div>
        <form onSubmit={handleQuickAiSubmit} style={{ display: 'flex', gap: '0.5rem' }}>
          <input
            type="text"
            className="form-control"
            placeholder="e.g. 'Create client Acme Corp', 'Which projects are at risk because of deadlines?'..."
            value={quickPrompt}
            onChange={(e) => setQuickPrompt(e.target.value)}
          />
          <button type="submit" className="btn btn-primary" style={{ width: 'auto', whiteSpace: 'nowrap' }}>
            Ask AI Assistant →
          </button>
        </form>
      </div>

      <div className="grid-2">
        {/* 3. Recent Projects List */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Active Projects</h3>
            <Link to="/projects" style={{ fontSize: '0.85rem' }}>See All</Link>
          </div>

          {loading ? (
            <div className="loading-spinner">Loading projects...</div>
          ) : recentProjects.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {recentProjects.map((proj) => (
                <div
                  key={proj.id}
                  style={{
                    padding: '0.75rem',
                    backgroundColor: 'var(--bg-hover)',
                    borderRadius: '0.375rem',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '0.95rem' }}>{proj.name}</div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                      {proj.clientName ? `Client: ${proj.clientName}` : 'No Client Assigned'}
                      {proj.budget && ` • Budget: $${proj.budget}`}
                    </div>
                  </div>
                  <span className={getStatusBadgeClass(proj.status)}>{proj.status}</span>
                </div>
              ))}
            </div>
          ) : (
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
              No active projects created yet. Use the <strong>Projects</strong> tab or <strong>AI Chat</strong> to create your first project!
            </p>
          )}
        </div>

        {/* 4. AI Engine Status & Architecture Panel */}
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem' }}>
            AI Engine Capabilities
          </h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem', fontSize: '0.9rem' }}>
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.65rem' }}>
              <span style={{ color: '#22c55e', fontWeight: 700 }}>🟢</span>
              <div>
                <strong>Semantic Vector Search (RAG)</strong>
                <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '0.825rem' }}>
                  Converts workspace entities into vector embeddings using <code>pgvector</code> &amp; Cosine Similarity.
                </p>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.65rem' }}>
              <span style={{ color: '#22c55e', fontWeight: 700 }}>🟢</span>
              <div>
                <strong>AI Action Execution Engine</strong>
                <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '0.825rem' }}>
                  Extracts &amp; executes <code>CREATE_CLIENT</code>, <code>CREATE_PROJECT</code>, and <code>CREATE_TASK</code> with explicit user confirmation cards.
                </p>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.65rem' }}>
              <span style={{ color: '#22c55e', fontWeight: 700 }}>🟢</span>
              <div>
                <strong>Persistent Multi-Turn Chat</strong>
                <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '0.825rem' }}>
                  Full conversation thread storage backed by PostgreSQL/H2 with anti-hallucination prompt boundaries.
                </p>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.65rem' }}>
              <span style={{ color: '#22c55e', fontWeight: 700 }}>🟢</span>
              <div>
                <strong>Multi-Tenant User Isolation</strong>
                <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '0.825rem' }}>
                  Enforces strict per-user authorization. User A can never access or modify User B's resources.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
};

export default DashboardPage;
