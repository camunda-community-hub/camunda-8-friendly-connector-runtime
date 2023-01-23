import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import adminTranslationService from '../service/AdminTranslationService';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import InputGroup from 'react-bootstrap/InputGroup';
import Table from 'react-bootstrap/Table';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';
import Tab from 'react-bootstrap/Tab';
import Tabs from 'react-bootstrap/Tabs';
import { useTranslation } from "react-i18next";

function AdminTranslationEdit() {
  const dispatch = useDispatch();
  const { t } = useTranslation();

  const language = adminTranslationService.getCurrentLanguage();
  const [siteTranslations, setSiteTranslations] = useState<{ key: string; value: string }[]>([]);

  useEffect(() => {
    let transArray: { key: string; value: string }[] = [];
    let i = 0;
    for (let prop in language.siteTranslations) {
      transArray[i++] = { key: prop, value: language.siteTranslations[prop] };
    }
    setSiteTranslations(transArray);
  }, []);

  const setSiteTransValue = (index: number, value: string) => {
    siteTranslations[index].value = value;
  }

  const saveLanguage = () => {
    let objSiteTrans: any = {};
    for (let i = 0; i < siteTranslations.length; i++) {
      objSiteTrans[siteTranslations[i].key] = siteTranslations[i].value;
    }
    dispatch(adminTranslationService.setSiteTranslations(objSiteTrans));
    dispatch(adminTranslationService.saveCurrentLanguage());
  }

  return (
    <div className="container-fluid">
      <br />
      <Row>
        <Col>
          <InputGroup className="mb-3">
            <InputGroup.Text> {t("Language name")}</InputGroup.Text>
            <Form.Control aria-label="name" placeholder="language name" defaultValue={language.name} onChange={(evt) => dispatch(adminTranslationService.setLanguageName(evt.target.value))} />
          </InputGroup>
          <InputGroup className="mb-3">
            <InputGroup.Text> {t("Language code")}</InputGroup.Text>
            <Form.Control aria-label="code" placeholder="language code" defaultValue={language.code} onChange={(evt) => dispatch(adminTranslationService.setLanguageCode(evt.target.value))} />
          </InputGroup>
        </Col>
        <Col>
          <Button variant="primary" onClick={saveLanguage}>{t("Save")}</Button>
        </Col>
      </Row>
      <h2 className="text-primary">{t("Dictionnary")}</h2>

      <Table striped bordered hover>
        <thead className="bg-primary">
          <tr>
            <th className="text-light">
              Key
            </th>
            <th className="text-light">
              Value
            </th>
          </tr>
        </thead>
        <tbody>
          {siteTranslations ? siteTranslations.map((trans: any, index: number) =>
            <tr key={trans.key + index}>
              <td>
                {trans.key}
              </td>
              <td>
                <Form.Control aria-label="Value" placeholder="value" defaultValue={trans.value} onChange={(evt) => setSiteTransValue(index, evt.target.value)} />
              </td>
            </tr>
          ) : <></>}
        </tbody>
        <tfoot className="bg-primary">
          <tr>
            <td></td>
            <td></td>
          </tr>
        </tfoot>
      </Table>
    </div>
  );
}

export default AdminTranslationEdit
