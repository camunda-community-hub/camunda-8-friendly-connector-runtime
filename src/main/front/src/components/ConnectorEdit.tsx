import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import connectorService from '../service/ConnectorService';
import InputGroup from 'react-bootstrap/InputGroup';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import Table from 'react-bootstrap/Table';
import Card from 'react-bootstrap/Card';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';
import { useTranslation } from "react-i18next";

function ConnectorEdit() {
  const { t } = useTranslation();
  const dispatch = useDispatch();

  const connector = useSelector((state: any) => state.connectors.current);

  const save = () => {
    dispatch(connectorService.save());
  };

  const refresh = (clone: any) => {
    dispatch(connectorService.setCurrent(clone));
  }

  const updateConnector = (property: string, value: any) => {
    let copy = connectorService.clone();
    copy[property] = value;
    refresh(copy);
  }

  const addVariable = () => {
    let copy = connectorService.clone();
    copy.fetchVariables.push("");
    refresh(copy);
  }
  const deleteVar = (index: number) => {
    let copy = connectorService.clone();
    copy.fetchVariables.splice(index, 1);
    refresh(copy);
  }
  const updateVar = (index: number, name: string) => {
    let copy = connectorService.clone();
    copy.fetchVariables[index] = name;
    refresh(copy);
  }

  const loadJar = (evt: any) => {
    dispatch(connectorService.uploadJar(evt.target.files[0]));
  }

  const close = () => {
    dispatch(connectorService.setCurrent(null));
  }

  return (
    <>
      <br />
      <Card className="bg-dark text-light">
        <Card.Body>
          <Card.Title>{connector.name}</Card.Title>
          <InputGroup className="mb-3">
            <InputGroup.Text>Name</InputGroup.Text>
            <Form.Control aria-label="Name" placeholder="Job type" value={connector.name} onChange={(evt) => updateConnector('name', evt.target.value)} />
          </InputGroup>
          <InputGroup className="mb-3">
            <InputGroup.Text>Job type</InputGroup.Text>
            <Form.Control aria-label="Job type" placeholder="Job type" value={connector.jobType} onChange={(evt) => updateConnector('jobType', evt.target.value)} />
          </InputGroup>

          <InputGroup className="mb-3">
            <InputGroup.Text>Jar file</InputGroup.Text>
            <Form.Control aria-label="file" type="file" id="uploadFormFileControl" onChange={loadJar} />
          </InputGroup>

          <Table variant="secondary" striped bordered hover>
            <thead>
              <tr>
                <th>{t("Variables")}</th>
                <th><Button variant="success" onClick={addVariable}><i className="bi bi-plus-circle"></i></Button></th>
              </tr>
            </thead>
            <tbody>
              {connector.fetchVariables ? connector.fetchVariables.map((variable: string, index: number) =>
                <tr key={index}>
                  <td>
                    <InputGroup className="mb-3">
                      <InputGroup.Text>variable name</InputGroup.Text>
                      <Form.Control aria-label="Varname" value={variable} onChange={(evt) => updateVar(index, evt.target.value)} />
                    </InputGroup></td>
                  <td><Button variant="danger" onClick={() => deleteVar(index)}><i className="bi bi-trash"></i></Button></td>
                </tr>
              ) : <></>}
            </tbody>
          </Table>
          <Row>
            <Col>
              <Button variant="secondary" onClick={(evt) => save()}><i className="bi bi-send"></i> {t("Save")}</Button>
            </Col>
            <Col>
              <Button variant="light" onClick={(evt) => close()}><i className="bi bi-close"></i> {t("Close")}</Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>
    </>
  );
}

export default ConnectorEdit
