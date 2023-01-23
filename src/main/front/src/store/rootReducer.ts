import {combineReducers} from '@reduxjs/toolkit';

import authReducer from './features/auth/slice';
import adminOrgReducer from './features/adminOrgs/slice';
import connectorsReducer from './features/connectors/slice';
import translationsReducer from './features/translations/slice';

const rootReducer = combineReducers({
  auth: authReducer,
  adminOrg: adminOrgReducer,
  connectors: connectorsReducer,
  translations: translationsReducer
});

export type RootState = ReturnType<typeof rootReducer>;

export default rootReducer;
