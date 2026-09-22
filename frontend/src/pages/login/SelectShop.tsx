import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Typography, Card, List, Tag, App, Spin } from 'antd';
import { ShopOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { selectShop } from '@/api/auth';
import { useAuthStore } from '@/store/authStore';
import type { ShopOption } from '@/types/api';

const { Title, Text } = Typography;

export default function SelectShop() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const { pendingShops, preAuthToken, token, setToken, login, clearPreAuth } = useAuthStore();
  const [loading, setLoading] = useState(false);

  // 无预授权信息则回登录页
  useEffect(() => {
    if (!pendingShops || !preAuthToken) {
      navigate('/login', { replace: true });
      return;
    }
    // 确保请求拦截器能带上预授权 token
    if (token !== preAuthToken) {
      setToken(preAuthToken);
    }
  }, [pendingShops, preAuthToken, token, setToken, navigate]);

  const handleSelect = async (shop: ShopOption) => {
    setLoading(true);
    try {
      const res = await selectShop({ clerkId: shop.clerkId });
      const data = res.data;
      login(data.token, {
        shopId: data.shopId,
        shopName: data.shopName,
        roleCode: data.roleCode,
        roleName: data.roleName,
      });
      clearPreAuth();
      message.success(`已进入「${data.shopName ?? '店铺'}」`);
      navigate('/', { replace: true });
    } catch {
      // 错误已在拦截器统一处理
    } finally {
      setLoading(false);
    }
  };

  if (!pendingShops) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <Spin />
      </div>
    );
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #0B5EAB 0%, #1677FF 50%, #0958D0 100%)',
        padding: 24,
      }}
    >
      <Card style={{ width: 480, boxShadow: '0 8px 32px rgba(0,0,0,0.12)' }} bordered={false}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={3} style={{ marginBottom: 4 }}>
            <ShopOutlined style={{ marginRight: 8 }} />
            选择门店
          </Title>
          <Text type="secondary">您有多个岗位，请选择要进入的门店</Text>
        </div>

        <Spin spinning={loading}>
          <List
            dataSource={pendingShops}
            renderItem={(shop) => (
              <List.Item
                style={{ padding: 0, marginBottom: 12, border: 'none' }}
              >
                <Card
                  hoverable
                  size="small"
                  onClick={() => !loading && handleSelect(shop)}
                  style={{ width: '100%', cursor: 'pointer', transition: 'all 0.2s' }}
                  styles={{ body: { padding: '16px 20px' } }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <Text strong style={{ fontSize: 16 }}>{shop.shopName}</Text>
                      <div style={{ marginTop: 6 }}>
                        <Tag color="blue">{shop.roleName}</Tag>
                      </div>
                    </div>
                    <CheckCircleOutlined style={{ fontSize: 20, color: '#d9d9d9' }} />
                  </div>
                </Card>
              </List.Item>
            )}
          />
        </Spin>
      </Card>
    </div>
  );
}
