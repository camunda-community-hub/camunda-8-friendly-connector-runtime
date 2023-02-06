import React, { useState, useEffect } from 'react';
import { Link } from "react-router-dom";
import monitoringService from '../service/MonitoringService';
import Table from 'react-bootstrap/Table';
import Tab from 'react-bootstrap/Tab';
import Tabs from 'react-bootstrap/Tabs';
import Modal from 'react-bootstrap/Modal';
import InputGroup from 'react-bootstrap/InputGroup';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';

import { useTranslation } from "react-i18next";

function Monitoring() {
  const { t } = useTranslation();

  const [dashboard, setDashboard] = useState<any>(null);
  const [timeStats, setTimeStats] = useState<any | null>(null);
  const [showTimeModal, setShowTimeModal] = useState<any>(null);
  const loadDashboard = async () => {
    setDashboard(await monitoringService.dashboard());
  }

  useEffect(() => {
    loadDashboard();
  }, []);

  const showTimeStats = async (connector: string) => {
    let stat = await monitoringService.timeStats(connector)
    stat.connector = connector;
    setTimeStats(stat);
    setShowTimeModal(true);
  }

  return (
    dashboard ?
      <>
        <Tabs className="mb-3">
          <Tab eventKey="home" title={t("Home")}>
            Connector executions : {dashboard.totalExecution}<br />
            <table>
              <tbody>
                {dashboard.successFailures && Object.keys(dashboard.successFailures).map((connector: string, index: number) =>
                  <tr key={index}>
                    <td>{connector}</td>
                    <td>
                      <div className="execChart">
                        <div className="success" style={{ width: (dashboard.successFailures[connector].success * 100 / (dashboard.successFailures[connector].success + dashboard.successFailures[connector].failure)) + "%" }}>
                          {dashboard.successFailures[connector].success}
                        </div>
                        <div className="failure" style={{ width: (dashboard.successFailures[connector].failure * 100 / (dashboard.successFailures[connector].success + dashboard.successFailures[connector].failure)) + "%" }}>
                          {dashboard.successFailures[connector].failure}
                        </div>
                      </div>
                    </td>
                    <td>
                      {dashboard.successFailures[connector].success > 0 ?
                        <Button variant="primary" onClick={() => showTimeStats(connector)}><i className="bi bi-hourglass-split"></i> {t("Durations")}</Button>
                        : <></>}
                      {dashboard.successFailures[connector].failure > 0 ?
                        <Link to={"/admin/connectorErrors/" + connector}><i className="bi bi-bug"></i> {t("Errors")}</Link>
                        : <></>}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </Tab>
          <Tab eventKey="audit" title={t("Audit logs")}>
            <Table variant="secondary" striped bordered hover>
              <thead>
                <tr>
                  <th>{t("Connector")}</th>
                  <th>{t("Actions")}</th>
                  <th>{t("Author")}</th>
                  <th>{t("Date")}</th>
                </tr>
              </thead>
              <tbody>
                {dashboard.auditLogs ? dashboard.auditLogs.map((log: any, index: number) =>
                  <tr key={index}>
                    <td>{log.connector}</td>
                    <td>{log.action}</td>
                    <td>{log.author}</td>
                    <td>{log.date}</td>
                  </tr>
                ) : <></>}
              </tbody>
            </Table>
          </Tab>
        </Tabs>
        {timeStats ?
          <Modal show={showTimeModal} animation={false} size="lg" onHide={() => setShowTimeModal(false)}>
            <Modal.Header closeButton>
              <Modal.Title>{timeStats.connector} : {t("Duration statistics")}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              <InputGroup className="mb-3">
                <InputGroup.Text>Average duration</InputGroup.Text>
                <Form.Control aria-label="Average" value={timeStats.avg + " ms"} disabled={true} />
              </InputGroup>
              <InputGroup className="mb-3">
                <InputGroup.Text>Slowest duration</InputGroup.Text>
                <Form.Control aria-label="Slowest" value={timeStats.slowest + " ms"} disabled={true} />
              </InputGroup>
              <InputGroup className="mb-3">
                <InputGroup.Text>Fastest duration</InputGroup.Text>
                <Form.Control aria-label="Fastest" value={timeStats.fastest + " ms"} disabled={true} />
              </InputGroup>
              <InputGroup className="mb-3">
                <InputGroup.Text>Nb executions</InputGroup.Text>
                <Form.Control aria-label="Count" value={timeStats.count} disabled={true} />
              </InputGroup>
            </Modal.Body>
            <Modal.Footer>
              <Button variant="primary" onClick={() => setShowTimeModal(false)}>
                {t("Close")}
              </Button>
            </Modal.Footer>
          </Modal>
          :
          <></>
        }
      </>
      : <></>
  );
}

export default Monitoring;
