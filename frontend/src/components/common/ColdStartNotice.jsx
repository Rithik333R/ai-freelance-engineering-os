import React from 'react';
import { useAuth } from '../../context/AuthContext';

const ColdStartNotice = () => {
  const { isColdStarting } = useAuth();

  if (!isColdStarting) return null;

  return (
    <div className="cold-start-banner">
      <span>⚡</span> Waking up the server... this may take a few seconds on the free hosting tier.
    </div>
  );
};

export default ColdStartNotice;
