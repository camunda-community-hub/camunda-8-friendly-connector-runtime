import React, { useState, useEffect } from 'react';
import monitoringService from '../service/MonitoringService';
import InputGroup from 'react-bootstrap/InputGroup';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import Table from 'react-bootstrap/Table';
import Card from 'react-bootstrap/Card';
import Modal from 'react-bootstrap/Modal';
import Alert from 'react-bootstrap/Alert';
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
            {dashboard.successFailures && Object.keys(dashboard.successFailures).map((connector: string, index: number) =>
              <tr>
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
              </tr>
         )}
            </table>
        </Tab>
        <Tab eventKey="audit" title={t("Audit logs")}>

        </Tab>
      </Tabs>
      : <></>
  );
}

export default Monitoring;
