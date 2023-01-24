import React, { useState, useEffect } from 'react';
import secretsService from '../service/SecretsService';
import InputGroup from 'react-bootstrap/InputGroup';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import Table from 'react-bootstrap/Table';
import Card from 'react-bootstrap/Card';
import Modal from 'react-bootstrap/Modal';
import Alert from 'react-bootstrap/Alert';
import { env } from '../env';
import { useTranslation } from "react-i18next";

function Secrets() {
  const { t } = useTranslation();

  const [secrets, setSecrets] = useState<any>(null);
  const [editSecret, setEditSecret] = useState<number>(-1);
  const [privateKey, setPrivateKey] = useState<string | null>(null);
  const [restorePrivateKey, setRestorePrivateKey] = useState<string>("");
  const [status, setStatus] = useState<string | null>(null);

  const loadSecrets = async () => {
    if (!secrets) {
      setStatus(await secretsService.loadStatus());
      await secretsService.loadSecrets();
      setSecrets(secretsService.secrets);
    }
  }

  const restoreSecrets = async () => {
    setStatus(await secretsService.restoreSecrets(restorePrivateKey));
  }

  useEffect(() => {
    loadSecrets();
  }, []);

  const setPersisted = (value: boolean) => {
    let clone = Object.assign({}, secrets);
    clone.persistedOnDisk = value;
    setSecrets(clone);
    secretsService.save(clone);
  }
  const setEncrypted = async (value: boolean) => {
    let clone = Object.assign({}, secrets);
    clone.encrypted = value;
    setSecrets(clone);
    let response = await secretsService.save(clone);
    if (response != null) {
      setPrivateKey(response);
    }
  }
  const newSecret = () => {
    let clone = Object.assign({}, secrets);
    clone.secrets.push({ "key": "secretKey", "value": "secretValue" });
    setSecrets(clone);
  }
  const deleteSecret = (index: number) => {
    secretsService.deleteSecret(index);
    let clone = Object.assign({}, secrets);
    clone.secrets.splice(index, 1);
    setSecrets(clone);
    setEditSecret(-1);
  }
  const changeSecretKey = (index: number, value: string) => {
    let clone = Object.assign({}, secrets);
    clone.secrets[index].key = value;
    setSecrets(clone);
  }
  const changeSecretValue = (index: number, value: string) => {
    let clone = Object.assign({}, secrets);
    clone.secrets[index].value = value;
    setSecrets(clone);
  }
  const saveSecret = (index: number) => {
    secretsService.saveSecret(index);
    setEditSecret(-1);
  }

  return (
    secrets ?
      <>
        <br />
        <Card className="bg-dark text-light">
          <Card.Body>
            <Card.Title> {t("Secrets")}</Card.Title>

            <Form.Check
              type="switch" checked={secrets.persistedOnDisk} onChange={(evt) => setPersisted(evt.target.checked)}
              label="Should be persisted on disk" />
            {secrets.persistedOnDisk ?
              <Form.Check
                type="switch" checked={secrets.encrypted} onChange={(evt) => setEncrypted(evt.target.checked)}
                label="Should be encrypted on disk" />
              : <></>

            }
            <Table variant="secondary" striped bordered hover>
              <thead>
                <tr>
                  <th>{t("Secret key")}</th>
                  <th>{t("Secret value")}</th>
                  <th><Button variant="success" onClick={() => newSecret()}><i className="bi bi-plus-circle"></i></Button></th>
                </tr>
              </thead>
              <tbody>
                {secrets.secrets ? secrets.secrets.map((secret: {}, index: number) =>
                  <tr key={index}>
                    <td>
                      {editSecret != index ?
                        secrets.secrets[index].key
                        :
                        <InputGroup className="mb-3">
                          <InputGroup.Text><i className="bi bi-key"></i></InputGroup.Text>
                          <Form.Control aria-label="Varname" value={secrets.secrets[index].key} onChange={(evt) => changeSecretKey(index, evt.target.value)} />
                        </InputGroup>
                      }</td>
                    <td>
                      {editSecret != index ?
                        Array(secrets.secrets[index].value.length+1).join('*') 
                        :
                        <InputGroup className="mb-3">
                          <InputGroup.Text><i className="bi bi-keyboard"></i></InputGroup.Text>
                          <Form.Control type="password" aria-label="Varname" value={secrets.secrets[index].value} onChange={(evt) => changeSecretValue(index, evt.target.value)} />
                        </InputGroup>
                      }</td>
                    <td>{editSecret != index ?
                      <Button variant="success" onClick={() => setEditSecret(index)}><i className="bi bi-pencil"></i></Button>
                      : <>
                        <Button variant="success" onClick={() => saveSecret(index)}><i className="bi bi-check"></i></Button>
                        <Button variant="danger" onClick={() => deleteSecret(index)}><i className="bi bi-trash"></i></Button>
                        <Button variant="outline-success" onClick={() => setEditSecret(-1)}><i className="bi bi-box-arrow-left"></i></Button>
                      </>
                    }</td>
                  </tr>
                ) : <></>}
              </tbody>
            </Table>
          </Card.Body>
        </Card>
        <Modal show={privateKey != null && privateKey != ''} onHide={() => setPrivateKey(null)} animation={false} size="lg">
            <Modal.Header closeButton>
              <Modal.Title>{t("Private Key")}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
            <div className="row">
              <Alert variant="info" style={{"wordWrap":"break-word"}}>
                {privateKey}
              </Alert>
              <Alert variant="warning">
                Keep this private key in a secure location. If the applications shut downs, you will be required to provide this key to restore the secrets.
              </Alert>
              </div>
            </Modal.Body>
            <Modal.Footer>
            <Button variant="primary" onClick={() => setPrivateKey(null)}>
                {t("Close")}
              </Button>
            </Modal.Footer>
        </Modal>
        <Modal show={status != null && status.startsWith("WARNING")} animation={false} size="lg">
          <Modal.Header>
            <Modal.Title>{t("Private Key")}</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <div className="row">
              <Alert variant="danger">
                {status}
              </Alert>
              <InputGroup className="mb-3">
                <InputGroup.Text>Private key</InputGroup.Text>
                <Form.Control aria-label="privateKey" value={restorePrivateKey} onChange={(evt) => setRestorePrivateKey(evt.target.value)} />
                <Button variant="primary" onClick={() => restoreSecrets()}>
                  {t("Restore secrets")}
                </Button>
              </InputGroup>
            </div>
          </Modal.Body>
        </Modal>
      </>
      : <></>
  );
}

export default Secrets;
