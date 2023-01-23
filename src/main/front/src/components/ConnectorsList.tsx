import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import connectorService from '../service/ConnectorService';
import Button from 'react-bootstrap/Button';
import Table from 'react-bootstrap/Table';
import { useTranslation } from "react-i18next";

function ConnectorsList() {
  const { t } = useTranslation();
  const dispatch = useDispatch();

  const connectors = useSelector((state: any) => state.connectors.connectors)


  return (
    <div>
      <br />
      <Button variant="primary" onClick={() => dispatch(connectorService.new())}><i className="bi bi-plus-square"></i> {t("New connector")}</Button>
      
      <Table striped bordered hover>
		<thead>
		  <tr>
            <th scope="col">{t("Name")}</th>
            <th scope="col">{t("Status")}</th>
            <th scope="col">{t("Actions")}</th>
          </tr>
        </thead>
        <tbody>
          {connectors ? connectors.map((connector: any, index: number) =>
            <tr key={connector.name}>
              <td>{connector.name}</td>
              <td>{connector.started ? t("Running") : t("Paused")}</td>
              <td>
                <Button variant="primary" onClick={() => dispatch(connectorService.open(connector.name))}><i className="bi bi-pencil"></i> {t("Open")}</Button>
                {connector.started ?
                  <Button variant="warning" onClick={() => dispatch(connectorService.stop(connector.name))}><i className="bi bi-stop"></i> {t("Stop")}</Button>
                  :
                  <Button variant="success" onClick={() => dispatch(connectorService.start(connector.name))}><i className="bi bi-play"></i> {t("Execute")}</Button>
                }
                <Button variant="danger" onClick={() => dispatch(connectorService.delete(connector.name))}><i className="bi bi-trash"></i> {t("Delete")}</Button>
              </td>
            </tr>)
          : <></>}
		</tbody>
      </Table>
  </div >
  );
}

export default ConnectorsList
