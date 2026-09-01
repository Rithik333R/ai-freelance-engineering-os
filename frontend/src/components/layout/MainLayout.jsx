import React from 'react';
import Navbar from '../common/Navbar';
import ColdStartNotice from '../common/ColdStartNotice';

const MainLayout = ({ children, fluid }) => {
  return (
    <div className="layout-wrapper">
      <ColdStartNotice />
      <Navbar />
      <main className={fluid ? 'container-fluid' : 'container'}>{children}</main>
    </div>
  );
};

export default MainLayout;

