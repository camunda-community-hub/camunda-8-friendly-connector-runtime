import React, { useState, useEffect } from "react";
import { BrowserRouter, Route, Link, NavLink } from "react-router-dom";
import { useDispatch, useSelector } from 'react-redux';
import authService from '../service/AuthService';
import adminOrgService from '../service/AdminOrgService';
import adminTranslationService from '../service/AdminTranslationService';
import logo from '../assets/img/logo.svg'
import InputGroup from 'react-bootstrap/InputGroup';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import LanguageSelector from './LanguageSelector';
import { useTranslation } from "react-i18next";

function AdminNavbar() {
  const { t } = useTranslation();
  const user = useSelector((state: any) => state.auth.data)
  const connector = useSelector((state: any) => state.connectors.current)
  const language = useSelector((state: any) => state.translations.currentLanguage)
  const orgEnabled = useSelector((state: any) => state.adminOrg.enabled)

  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(adminOrgService.checkIfEnabled());
  }, []);
  const logout = (event: any) => {
    dispatch(authService.signOut());
  };
  return (
    <>
      <nav className={connector || language ? "navbar reduced navbar-light bg-dark" : "navbar navbar-light bg-light"} >
        <div className="container-fluid">
          <Link to="/home"><img width="120" src={logo} className="custom-logo" alt="Camunda" /></Link>
          <div>
            {language ?
              <InputGroup className="mb-3">
                <Form.Control aria-label="Language name" placeholder="Language name" value={language.name} disabled />
                <Button variant="secondary" onClick={() => dispatch(adminTranslationService.setLanguage(null))}><i className="bi bi-arrow-return-left"></i> {t("Back")}</Button>
              </InputGroup>
              :
              <div className="input-group mb-3 ">
                <LanguageSelector></LanguageSelector>
                <a className="btn btn-outline-secondary" onClick={logout}>{authService.getUser()!.username} <i className="bi bi-box-arrow-left"></i></a>
              </div>
            }
          </div>
        </div>
        {!connector && !language ?
          <div className="bg-primary menu">
            <NavLink className={({ isActive }) =>
              isActive ? "text-light menu-item selected" : "text-light menu-item"
            } to="/admin/connectors">{t("Connectors")}</NavLink>
            <NavLink className={({ isActive }) =>
              isActive ? "text-light menu-item selected" : "text-light menu-item"
            } to="/admin/secrets">{t("Secrets")}</NavLink>
           
            {orgEnabled && user!.profile === 'Admin' ?
              <>
                <NavLink className={({ isActive }) =>
                  isActive ? "text-light menu-item selected" : "text-light menu-item"
                } to="/admin/users">{t("Users")}</NavLink>
                <NavLink className={({ isActive }) =>
                  isActive ? "text-light menu-item selected" : "text-light menu-item"
                } to="/admin/translations">{t("Internationalization")}</NavLink>
              </>
              : <></>
            }
          </div>
          : <></>}
      </nav>
    </>
  );
}

export default AdminNavbar;
