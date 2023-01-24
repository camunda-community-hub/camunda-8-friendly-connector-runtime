import api from './api';

export class SecretsService {
  loading: boolean = false;
  secrets: any = { "persistedOnDisk": true, "secrets": [] };
  errors: any | null = null;
  loadStatus = async ():Promise<string> => {
    let { data } = await api.get('/secrets/status');
    console.log(data);
    return data;
  }
  loadSecrets = async () => {
    this.loading = true;
    try {
      const { data } = await api.get('/secrets');

      this.secrets.persistedOnDisk = data.persistedOnDisk;
      this.secrets.encrypted = data.encrypted;
      this.secrets.secrets = [];
      for (const key in data.secretsKeyValues) {
        this.secrets.secrets.push({ "key": key, "value": data.secretsKeyValues[key] });
      }
      this.loading = false;
    } catch (error: any) {
      if (error.response) {
        // The request was made. server responded out of range of 2xx
        this.errors = error.response.data.message;
      } else if (error.request) {
        // The request was made but no response was received
        this.errors = 'ERROR_NETWORK';
      } else {
        // Something happened in setting up the request that triggered an Error
        console.warn('Error', error.message);
        this.errors = error.message;
      }
      this.loading = false;
    }
  }
  restoreSecrets = async (privateKey: string): Promise<string> => {
    const { data } = await api.post('/secrets/restore', { "privateKey": privateKey });
    return data;
  }
  save = async (secrets: any): Promise<string> => {
    let secretConf = { "persistedOnDisk": secrets.persistedOnDisk, "encrypted": secrets.encrypted };
    let { data } = await api.post('/secrets', secretConf);
    return data.privateKey;
  }
  saveSecret = (index: number) => {
    api.post('/secrets/add', this.secrets.secrets[index]).then(response => {
    }).catch(error => {
      alert(error.message);
    })
  }
  deleteSecret = (index: number) => {
    api.delete('/secrets/' + this.secrets.secrets[index].key).then(response => {
    }).catch(error => {
      alert(error.message);
    })
  }
}

const secretsService = new SecretsService();

export default secretsService;
