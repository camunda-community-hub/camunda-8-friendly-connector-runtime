import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import ConnectorService from '../service/ConnectorService';
import ConnectorsList from '../components/ConnectorsList';
import ConnectorEdit from '../components/ConnectorEdit';

function Connectors() {

  const connector = useSelector((state: any) => state.connectors.current)
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(ConnectorService.getConnectors());
  });

  return (
    connector ? <ConnectorEdit /> : <ConnectorsList />
  );
}

export default Connectors;
