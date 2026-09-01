import React from 'react';
import Navbar from '../common/Navbar';
import ColdStartNotice from '../common/ColdStartNotice';

const MainLayout = ({ children }) => {
  return (
    <div>
      <ColdStartNotice />
      <Navbar />
      <main className="container">{children}</main>
    </div>
  );
};

export default MainLayout;
