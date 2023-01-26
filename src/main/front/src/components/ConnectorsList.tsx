import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import connectorService from '../service/ConnectorService';
import Button from 'react-bootstrap/Button';
import Table from 'react-bootstrap/Table';
import InputGroup from 'react-bootstrap/InputGroup';
import Form from 'react-bootstrap/Form';
import Card from 'react-bootstrap/Card';
import Modal from 'react-bootstrap/Modal';
import Alert from 'react-bootstrap/Alert';
import { useTranslation } from "react-i18next";

function ConnectorsList() {
  const { t } = useTranslation();
  const dispatch = useDispatch();

  const connectors = useSelector((state: any) => state.connectors.connectors)
  const [ootbModal, setOotbModal] = useState<boolean>(false);
  const [release, setRelease] = useState<string>('');
  const [ootbConnectors, setOotbConnectors] = useState<any[]>([]);
  useEffect(() => {
    connectorService.loadRelease();
  }, []);

  const openOotbModal = () => {
    if (release === '') {
      setRelease(connectorService.release);
      setOotbConnectors(connectorService.ootbConnectors);
    }
    setOotbModal(true);
  }

  const updateOotbConnectors = async (release: string) => {
    setOotbConnectors(await connectorService.loadOotbConnectors(release));
  }

  const install = async (index: number) => {
    await connectorService.installOotb(ootbConnectors[index].name, release);
    dispatch(connectorService.getConnectors());
    let clone:any[] = Object.assign([], ootbConnectors);
    clone[index].installed = true;
    setOotbConnectors(clone);
  }

  return (
    <div>
      <br />
      <Button variant="primary" onClick={() => dispatch(connectorService.new())}><i className="bi bi-plus-square"></i> {t("New connector")}</Button>
      <Button variant="secondary" onClick={() => openOotbModal()}><i className="bi bi-download"></i> {t("Connectors Camunda")}</Button>

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
      <Modal show={ootbModal} animation={false} size="lg" onHide={() => setOotbModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>{t("Out of the box Connectors")}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className="row">

            <InputGroup className="mb-3">
              <InputGroup.Text>Private key</InputGroup.Text>
              <Form.Control aria-label="privateKey" value={release} onChange={(evt) => setRelease(evt.target.value)} />
              <Button variant="primary" onClick={() => updateOotbConnectors(release)}>
                {t("Update list")}
              </Button>
            </InputGroup>
            {t("Connector list")}
            <Table variant="secondary" striped bordered hover>
              <thead>
                <tr>
                  <th>{t("Connector name")}</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {ootbConnectors ? ootbConnectors.map((connector: any, index: number) =>
                  <tr key={index}>
                    <td>{connector.name}</td>
                    <td>{connector.installed
                      ? 'installed' :
                      <Button variant="primary" onClick={() => install(index)}><i className="bi bi-download"></i> install</Button>
                    }
                    </td>
                  </tr>
                ) : <></>}
              </tbody>
            </Table>
          </div>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="primary" onClick={() => setOotbModal(false)}>
            {t("Close")}
          </Button>
        </Modal.Footer>
      </Modal>
  </div >
  );
}

export default ConnectorsList
