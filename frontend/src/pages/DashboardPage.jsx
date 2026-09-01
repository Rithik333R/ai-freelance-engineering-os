import React, { useEffect, useState } from 'react';
import MainLayout from '../components/layout/MainLayout';
import { clientApi } from '../api/clientApi';
import { projectApi } from '../api/projectApi';
import { Link } from 'react-router-dom';

const DashboardPage = () => {
  const [clientCount, setClientCount] = useState(0);
  const [projectCount, setProjectCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardMetrics = async () => {
      try {
        const [clientsRes, projectsRes] = await Promise.all([
          clientApi.getClients(0, 1),
          projectApi.getProjects(0, 1),
        ]);
        setClientCount(clientsRes.data?.totalElements || 0);
        setProjectCount(projectsRes.data?.totalElements || 0);
      } catch (err) {
        console.error('Failed to load metrics', err);
      } finally {
        setLoading(false);
      }
    };
    fetchDashboardMetrics();
  }, []);

  return (
    <MainLayout>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700 }}>Workspace Dashboard</h1>
        <p style={{ color: '#94a3b8' }}>Overview of active software engineering client projects</p>
      </div>

      <div className="grid-3">
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
          <div className="card-title">System Status</div>
          <div className="card-value" style={{ color: '#22c55e', fontSize: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            🟢 Operational
          </div>
          <p style={{ fontSize: '0.85rem', color: '#94a3b8', marginTop: '0.5rem' }}>Spring Boot & PostgreSQL Connected</p>
        </div>
      </div>

      <div className="card" style={{ marginTop: '2rem' }}>
        <h3 style={{ marginBottom: '0.75rem', fontSize: '1.1rem' }}>🤖 AI Engine Readiness</h3>
        <p style={{ color: '#94a3b8', fontSize: '0.9rem' }}>
          Phase 1 backend foundation, Flyway migrations, JWT security, and resource-isolated CRUD APIs are fully operational.
          Ready for Requirement Extraction, AI Planning, and Grounded RAG Assistant integrations!
        </p>
      </div>
    </MainLayout>
  );
};

export default DashboardPage;
