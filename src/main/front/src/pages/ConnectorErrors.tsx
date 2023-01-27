import React, { useState, useEffect } from 'react';
import monitoringService from '../service/MonitoringService';
import Table from 'react-bootstrap/Table';
import Accordion from 'react-bootstrap/Accordion';
import { useTranslation } from "react-i18next";

function ConnectorErrors() {
  const { t } = useTranslation();

  const [errors, setErrors] = useState<any[]|null>(null);
  const [connector, setConnector] = useState<string|null>(null);
  
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
              <Table variant="secondary" striped bordered hover>
                <tbody>
                  <tr><td>Exception</td><td>{error.exception}</td></tr>
                  <tr><td>Connector</td><td dangerouslySetInnerHTML={{ __html: JSON.stringify(error.connector, null, 2).replaceAll('\n', '<br/>').replaceAll(' ', '&nbsp;&nbsp;') }}></td></tr>
                  <tr><td>Variables</td><td dangerouslySetInnerHTML={{ __html: JSON.stringify(error.context, null, 2).replaceAll('\n', '<br/>').replaceAll(' ', '&nbsp;&nbsp;') }}></td></tr>
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
