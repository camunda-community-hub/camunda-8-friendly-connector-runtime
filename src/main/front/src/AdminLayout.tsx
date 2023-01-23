import React from "react";
import { useDispatch, useSelector } from 'react-redux';
import { Outlet } from "react-router-dom";
import AdminNavbar from "./components/AdminNavbar";

const AdminLayout = () => {
  return (
    <>
      <AdminNavbar />
      <div className="container-fluid bg-light main">
        <Outlet />
      </div>
    </>
  );
};

export default AdminLayout;
