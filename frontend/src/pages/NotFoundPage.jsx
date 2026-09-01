import React from 'react';
import { Link } from 'react-router-dom';

const NotFoundPage = () => {
  return (
    <div style={{ textAlign: 'center', padding: '5rem 1rem', color: '#94a3b8' }}>
      <h1 style={{ fontSize: '3rem', fontWeight: 800, color: '#f8fafc', marginBottom: '1rem' }}>404</h1>
      <p style={{ fontSize: '1.25rem', marginBottom: '1.5rem' }}>Page Not Found</p>
      <Link to="/dashboard" className="btn btn-primary" style={{ display: 'inline-flex', width: 'auto' }}>
        Return to Dashboard
      </Link>
    </div>
  );
};

export default NotFoundPage;
