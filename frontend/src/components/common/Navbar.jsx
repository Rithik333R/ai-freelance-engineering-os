import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const handleLogout = () => {
    setIsMenuOpen(false);
    logout();
    navigate('/login');
  };

  const closeMenu = () => {
    setIsMenuOpen(false);
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand">
          AI Freelance <span>Engineering OS</span>
        </div>

        {user && (
          <>
            <button
              className="navbar-toggle"
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              aria-label="Toggle navigation menu"
            >
              {isMenuOpen ? '✕' : '☰'}
            </button>

            <div className={`navbar-menu ${isMenuOpen ? 'open' : ''}`}>
              <ul className="navbar-links">
                <li>
                  <NavLink to="/dashboard" onClick={closeMenu} className={({ isActive }) => (isActive ? 'active' : '')}>
                    Dashboard
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/clients" onClick={closeMenu} className={({ isActive }) => (isActive ? 'active' : '')}>
                    Clients
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/projects" onClick={closeMenu} className={({ isActive }) => (isActive ? 'active' : '')}>
                    Projects
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/tasks" onClick={closeMenu} className={({ isActive }) => (isActive ? 'active' : '')}>
                    Tasks
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/ai-chat" onClick={closeMenu} className={({ isActive }) => (isActive ? 'active' : '')}>
                    AI Chat
                  </NavLink>
                </li>
              </ul>
              <div className="navbar-user">
                <span className="user-badge">{user.fullName || user.email}</span>
                <button onClick={handleLogout} className="btn btn-secondary" style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}>
                  Logout
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
