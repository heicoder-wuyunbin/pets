import { defineConfig, type ProxyOptions } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

const gatewayTarget = 'http://localhost:9000';

/**
 * 前端路径前缀 -> 网关服务名。
 * 网关开启 discovery locator, 路由为 /{service}/**, 去掉服务名前缀后转发;
 * 各服务 Controller 均挂在 /api/v1 下, 因此统一重写为 /{service}/api/v1{原路径}。
 * 例: /auth/login -> /auth-service/api/v1/auth/login
 */
const serviceOf: Record<string, string> = {
  '/auth': 'auth-service',
  '/member': 'member-service',
  '/trade': 'trade-service',
  '/goods': 'goods-service',
  '/service': 'goods-service',
  '/stock': 'stock-service',
  '/marketing': 'market-service',
  '/report': 'report-service',
  '/shop': 'shop-service',
  '/notice': 'shop-service',
  '/clerk': 'system-service',
  '/sys-param': 'system-service',
  '/operation-log': 'system-service',
};

/** API 请求全部为 POST; GET 视为 SPA 页面导航, 放行给本地 dev server */
const apiProxy = (service: string): ProxyOptions => ({
  target: gatewayTarget,
  changeOrigin: true,
  rewrite: (path) => `/${service}/api/v1${path}`,
  bypass: (req) => (req.method !== 'POST' ? req.url : undefined),
});

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/auth': apiProxy('auth-service'),
      '/member': apiProxy('member-service'),
      '/trade': apiProxy('trade-service'),
      '/goods': apiProxy('goods-service'),
      '/service': apiProxy('goods-service'),
      '/stock': apiProxy('stock-service'),
      '/marketing': apiProxy('market-service'),
      '/report': apiProxy('report-service'),
      '/shop': apiProxy('shop-service'),
      '/notice': apiProxy('shop-service'),
      '/clerk': apiProxy('system-service'),
      '/sys-param': apiProxy('system-service'),
      '/operation-log': apiProxy('system-service'),
    },
  },
});
