import React, { useState, useEffect } from 'react';
import { Link } from "react-router-dom";
import monitoringService from '../service/MonitoringService';
import Table from 'react-bootstrap/Table';
import Tab from 'react-bootstrap/Tab';
import Tabs from 'react-bootstrap/Tabs';

import { useTranslation } from "react-i18next";

function Monitoring() {
  const { t } = useTranslation();

  const [dashboard, setDashboard] = useState<any>(null);

  const loadDashboard = async () => {
    setDashboard(await monitoringService.dashboard());
  }

  useEffect(() => {
    loadDashboard();
  }, []);

  return (
    dashboard ?
      <Tabs className="mb-3">
        <Tab eventKey="home" title={t("Home")}>
          Connector executions : {dashboard.totalExecution}<br/>
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
                      <Link to={"/admin/connectorErrors/"+connector}>{dashboard.successFailures[connector].failure}</Link>
                    </div>
                  </div>
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
      : <></>
  );
}

export default Monitoring;
