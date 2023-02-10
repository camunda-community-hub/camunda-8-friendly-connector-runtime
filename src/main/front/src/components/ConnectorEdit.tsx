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

  const user = useSelector((state: any) => state.auth.data)
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
            <InputGroup.Text>Jar file</InputGroup.Text>
            {user.profile != 'Admin' ?
              <Form.Control aria-label="Job type" readOnly={true} placeholder="Job type" value={connector.jarFile} />
              :
              <Form.Control aria-label="file" type="file" id="uploadFormFileControl" onChange={loadJar} />
            }
          </InputGroup>
          <InputGroup className="mb-3">
            <InputGroup.Text>Name</InputGroup.Text>
            <Form.Control aria-label="Name" readOnly={user.profile != 'Admin' || !connector.jarFile} placeholder="Job type" value={connector.name} onChange={(evt) => updateConnector('name', evt.target.value)} />
          </InputGroup>
          <InputGroup className="mb-3">
            <InputGroup.Text>Service</InputGroup.Text>
            <Form.Control aria-label="Name" readOnly={user.profile != 'Admin' || !connector.jarFile} placeholder="Job type" value={connector.service} onChange={(evt) => updateConnector('service', evt.target.value)} />
          </InputGroup>
          <InputGroup className="mb-3">
            <InputGroup.Text>Job type</InputGroup.Text>
            <Form.Control aria-label="Job type" readOnly={user.profile != 'Admin' || !connector.jarFile} placeholder="Job type" value={connector.jobType} onChange={(evt) => updateConnector('jobType', evt.target.value)} />
          </InputGroup>

          <Table variant="secondary" striped bordered hover>
            <thead>
              <tr>
                <th>{t("Variables")}</th>
                {user.profile == 'Admin' && connector.jarFile ?
                  <th><Button variant="success" onClick={addVariable}><i className="bi bi-plus-circle"></i></Button></th>
                  : <></>
                }
              </tr>
            </thead>
            <tbody>
              {connector.fetchVariables ? connector.fetchVariables.map((variable: string, index: number) =>
                <tr key={index}>
                  <td>
                    <InputGroup className="mb-3">
                      <InputGroup.Text>variable name</InputGroup.Text>
                      <Form.Control readOnly={user.profile != 'Admin' || !connector.jarFile} aria-label="Varname" value={variable} onChange={(evt) => updateVar(index, evt.target.value)} />
                    </InputGroup></td>
                  {user.profile == 'Admin' && connector.jarFile ?
                    <td><Button variant="danger" onClick={() => deleteVar(index)}><i className="bi bi-trash"></i></Button></td>
                    : <></>
                  }
                </tr>
              ) : <></>}
            </tbody>
          </Table>
         
            <Row>
            <Col>
              {user.profile == 'Admin' ?
                <Button variant="secondary" disabled={!connector.jarFile || !connector.jobType || !connector.name} onClick={(evt) => save()}><i className="bi bi-send"></i> {t("Save")}</Button>
                : <></>
              }
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
