import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface ConnectorsState {
  connectors: any[] | null;
  current: any;
  previewData: string;
  loading: boolean;
  error: string | null;
}

const initialState: ConnectorsState = {
  connectors: null,
  current: null,
  previewData: '{}',
  loading: false,
  error: null,
};

const connectorsSlice = createSlice({
  name: 'connectors',
  initialState,
  reducers: {
    loadStart: (state: ConnectorsState) => {
      state.loading = true;
    },
    fail: (state: ConnectorsState, action: PayloadAction<string>) => {
      state.loading = false;
      state.error = action.payload;
    },
    silentfail: (state: ConnectorsState) => {
      state.loading = false;
    },
    loadSuccess: (state: ConnectorsState, action: PayloadAction<any[]>) => {
      state.loading = false;
      state.connectors = action.payload;
    },
    setCurrent: (state: ConnectorsState, action: PayloadAction<any>) => {
      state.loading = false;
      state.current = action.payload;
      if (state.current != null) {
        state.previewData = action.payload.previewData;
      }
    }
  },
});

export const {
  loadStart,
  loadSuccess,
  setCurrent,
  fail,
  silentfail
} = connectorsSlice.actions;

export default connectorsSlice.reducer;
