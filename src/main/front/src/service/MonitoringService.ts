import api from './api';

export class MonitoringService {
  dashboard = async (): Promise<any> => {
    let { data } = await api.get('/monitoring');
    return data;
  }
  errors = async (connector:string): Promise<any> => {
    let { data } = await api.get('/monitoring/errors/'+connector);
    return data;
  }
}

const monitoringService = new MonitoringService();

export default monitoringService;
