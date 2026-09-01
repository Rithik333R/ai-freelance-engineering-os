import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        🚀 AI Freelance <span>Engineering OS</span>
      </div>
      {user && (
        <>
          <ul className="navbar-links">
            <li>
              <NavLink to="/dashboard" className={({ isActive }) => (isActive ? 'active' : '')}>
                Dashboard
              </NavLink>
            </li>
            <li>
              <NavLink to="/clients" className={({ isActive }) => (isActive ? 'active' : '')}>
                Clients
              </NavLink>
            </li>
            <li>
              <NavLink to="/projects" className={({ isActive }) => (isActive ? 'active' : '')}>
                Projects
              </NavLink>
            </li>
            <li>
              <NavLink to="/tasks" className={({ isActive }) => (isActive ? 'active' : '')}>
                Tasks
              </NavLink>
            </li>
          </ul>
          <div className="navbar-user">
            <span className="user-badge">{user.fullName || user.email}</span>
            <button onClick={handleLogout} className="btn btn-secondary" style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}>
              Logout
            </button>
          </div>
        </>
      )}
    </nav>
  );
};

export default Navbar;
