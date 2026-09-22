import axios from 'axios';
import { message } from 'antd';
import type { ApiResult } from '@/types/api';
import { useAuthStore } from '@/store/authStore';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  timeout: 15000,
});

request.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  config.headers['lang'] = 'zh-cn';
  return config;
});

request.interceptors.response.use(
  (res): any => {
    const body = res.data as ApiResult<unknown>;
    if (body.code !== 0) {
      message.error(body.msg || '请求失败');
      return Promise.reject(body);
    }
    return body;
  },
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
      window.location.href = '/login';
      return Promise.reject(error);
    }
    const msg = error.response?.data?.msg || error.message || '网络异常';
    message.error(msg);
    return Promise.reject(error);
  },
);

export default request;
