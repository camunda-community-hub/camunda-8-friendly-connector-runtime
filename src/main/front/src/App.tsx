import ReactDOM from "react-dom";
import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Connectors from "./pages/Connectors";
import ConnectorErrors from "./pages/ConnectorErrors";
import Secrets from "./pages/Secrets";
import SimpleLayout from "./SimpleLayout"
import AdminLayout from "./AdminLayout"
import Welcome from "./pages/Welcome";
import Monitoring from "./pages/Monitoring";
import Login from "./pages/Login";
import Undefined from "./pages/Undefined";
import AdminUsers from "./pages/AdminUsers";
import AdminTranslations from "./pages/AdminTranslations";
import './assets/css/bootstrap.min.css';
import './assets/css/bootstrap-icons-1.7.2.css';
import './assets/css/custom.css';
import './assets/css/login.css';
import './assets/css/customBootstrap.css';
import authService from './service/AuthService'

function App() {

  const dispatch = useDispatch();
  const user = useSelector((state: any) => state.auth.data)

  useEffect(() => {
    dispatch(authService.retrieveConnectedUser());
  });

  return (
    user ?
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<SimpleLayout />}>
            <Route index element={<Welcome />} />
            <Route path="home" element={<Welcome />} />
            <Route path="*" element={<Undefined />} />
          </Route>
          <Route path="admin" element={<AdminLayout />}>
            <Route index element={<Monitoring />} />
            <Route path="monitoring" element={<Monitoring />} />
            <Route path="connectors" element={<Connectors />} />
            <Route path="secrets" element={<Secrets />} />
            <Route path="users" element={<AdminUsers />} />
            <Route path="translations" element={<AdminTranslations />} />
            <Route path="connectorErrors/*" element={<ConnectorErrors />} />
            <Route path="*" element={<Undefined />} />
          </Route>
        </Routes>
      </BrowserRouter>
      :
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<SimpleLayout />}>
            <Route index element={<Welcome />} />
            <Route path="home" element={<Welcome />} />
            <Route path="*" element={<Undefined />} />
          </Route>
          <Route path="admin" element={<SimpleLayout />}>
            <Route index element={<Login />} />
            <Route path="*" element={<Login />} />
          </Route>
        </Routes>
      </BrowserRouter>
  );
}

export default App;
