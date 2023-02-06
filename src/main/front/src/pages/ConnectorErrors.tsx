import React, { useState, useEffect } from 'react';
import monitoringService from '../service/MonitoringService';
import Table from 'react-bootstrap/Table';
import Accordion from 'react-bootstrap/Accordion';
import { useTranslation } from "react-i18next";

function ConnectorErrors() {
  const { t } = useTranslation();

  const [errors, setErrors] = useState<any[]|null>(null);
  const [connector, setConnector] = useState<string|null>(null);
  const [showException, setShowException] = useState<boolean>(false);
  const [showVariables, setShowVariables] = useState<boolean>(false);
  const [showConnector, setShowConnector] = useState<boolean>(false);
  
  const loadDashboard = async () => {
    let url = window.location.href;
    let lastElt = url.substring(url.lastIndexOf("/") + 1, url.length);
    setConnector(lastElt);
    setErrors(await monitoringService.errors(lastElt));
  }

  useEffect(() => {
    if (errors == null && connector==null) {
      loadDashboard();
    }
  }, []);

  return (
    errors ?
      <>
        <h2>Connector <b>{connector}</b></h2>
        <Accordion defaultActiveKey="0">
        {errors.map((error: any, index: number) =>
          <Accordion.Item key={index} eventKey={'' + index} >
            <Accordion.Header>Instance&nbsp;<b>{error.processInstance}</b>&nbsp;on&nbsp;<i>{error.date}</i></Accordion.Header>
            <Accordion.Body>
              <Table variant="secondary" striped bordered>
                <tbody>
                  <tr className={showException ? '' : 'collapsed'} ><td>Exception</td><td><div>{error.exception}</div></td><td onClick={() => setShowException(!showException)}><i className={showException ? "bi bi-arrows-collapse" : "bi bi-arrows-expand"}></i></td></tr>
                  <tr className={showConnector ? '' : 'collapsed'}><td>Connector</td><td dangerouslySetInnerHTML={{ __html: showConnector ? JSON.stringify(error.connector, null, 2).replaceAll('\n', '<br/>').replaceAll(' ', '&nbsp;&nbsp;') : '<div>' + JSON.stringify(error.connector) + '</div>'}}></td>
                    <td onClick={() => setShowConnector(!showConnector)}><i className={showConnector ? "bi bi-arrows-collapse" : "bi bi-arrows-expand"}></i></td></tr>
                  <tr className={showVariables ? '' : 'collapsed'}><td>Variables</td><td dangerouslySetInnerHTML={{ __html: showVariables ? JSON.stringify(error.context, null, 2).replaceAll('\n', '<br/>').replaceAll(' ', '&nbsp;&nbsp;') : '<div>' + JSON.stringify(error.context) + '</div>'}}></td>
                    <td onClick={() => setShowVariables(!showVariables)}><i className={showVariables ? "bi bi-arrows-collapse" : "bi bi-arrows-expand"}></i></td></tr>
                  <tr><td>Duration</td><td>{ error.duration } ms</td><td></td></tr>
                </tbody>
               </Table>
            </Accordion.Body>
          </Accordion.Item>
        )}
        </Accordion>
        </>
      : <></>
  );
}

export default ConnectorErrors;
