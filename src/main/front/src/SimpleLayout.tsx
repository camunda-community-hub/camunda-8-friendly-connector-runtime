import React from "react";
import { useDispatch, useSelector } from 'react-redux';
import { Outlet } from "react-router-dom";

const SimpleLayout = () => {
  return (
    <>
      <Outlet />
    </>
  );
};

export default SimpleLayout;
