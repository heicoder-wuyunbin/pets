import { useState, useRef, type Dispatch, type SetStateAction } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Form, Input, Button, Checkbox, Typography, Tabs, App, Space, Modal,
} from 'antd';
import { LockOutlined, MobileOutlined, SafetyOutlined } from '@ant-design/icons';
import { login, register, sendCode, resetPassword } from '@/api/auth';
import { useAuthStore } from '@/store/authStore';
import type { LoginParams, RegisterParams, ResetPasswordParams } from '@/types/api';

const { Title, Text, Link } = Typography;

const REMEMBER_PHONE_KEY = 'pets-remembered-phone';

export default function Login() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('login');
  const { login: saveAuth, setPreAuth } = useAuthStore();

  // 验证码倒计时
  const [countdown, setCountdown] = useState(0);
  const [sendCodeLoading, setSendCodeLoading] = useState(false);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // 忘记密码弹窗
  const [resetModalVisible, setResetModalVisible] = useState(false);
  const [resetLoading, setResetLoading] = useState(false);
  const [resetCountdown, setResetCountdown] = useState(0);
  const [resetSendLoading, setResetSendLoading] = useState(false);
  const resetTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // 记住手机号
  const rememberedPhone = localStorage.getItem(REMEMBER_PHONE_KEY) ?? '';

  const startCountdown = (setter: Dispatch<SetStateAction<number>>, ref: React.MutableRefObject<ReturnType<typeof setInterval> | null>) => {
    setter(60);
    if (ref.current) clearInterval(ref.current);
    ref.current = setInterval(() => {
      setter((prev) => {
        if (prev <= 1) {
          if (ref.current) clearInterval(ref.current);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const onFinish = async (values: LoginParams) => {
    setLoading(true);
    try {
      const res = await login({
        phone: values.phone,
        password: values.password,
        remember: values.remember ?? false,
        lang: 'zh-cn',
      });
      const data = res.data;

      if (values.remember) {
        localStorage.setItem(REMEMBER_PHONE_KEY, values.phone);
      } else {
        localStorage.removeItem(REMEMBER_PHONE_KEY);
      }

      if (data.needSelect && data.shops) {
        setPreAuth(data.token, data.shops);
        navigate('/select-shop', { replace: true });
      } else {
        saveAuth(data.token, {
          shopId: data.shopId,
          shopName: data.shopName,
          roleCode: data.roleCode,
          roleName: data.roleName,
        });
        message.success(`欢迎回来${data.shopName ? '·' + data.shopName : ''}`);
        navigate('/', { replace: true });
      }
    } catch {
      // 错误已在 request 拦截器统一处理
    } finally {
      setLoading(false);
    }
  };

  const onRegister = async (values: RegisterParams) => {
    setLoading(true);
    try {
      await register(values);
      message.success('注册成功，请登录');
      setActiveTab('login');
    } catch {
      // 错误已在拦截器处理
    } finally {
      setLoading(false);
    }
  };

  const handleSendCode = async (phone: string, bizType: 'register' | 'reset_password') => {
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
      message.warning('请先输入正确的手机号');
      return;
    }
    setSendCodeLoading(true);
    try {
      await sendCode({ phone, bizType });
      message.success('验证码已发送');
      startCountdown(setCountdown, timerRef);
    } catch {
      // 错误已在拦截器处理
    } finally {
      setSendCodeLoading(false);
    }
  };

  const handleResetPassword = async (values: ResetPasswordParams) => {
    setResetLoading(true);
    try {
      await resetPassword(values);
      message.success('密码重置成功，请使用新密码登录');
      setResetModalVisible(false);
    } catch {
      // 错误已在拦截器处理
    } finally {
      setResetLoading(false);
    }
  };

  const handleResetSendCode = async (phone: string) => {
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
      message.warning('请先输入正确的手机号');
      return;
    }
    setResetSendLoading(true);
    try {
      await sendCode({ phone, bizType: 'reset_password' });
      message.success('验证码已发送');
      startCountdown(setResetCountdown, resetTimerRef);
    } catch {
      // 错误已在拦截器处理
    } finally {
      setResetSendLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex' }}>
      {/* 左侧品牌区 */}
      <div
        style={{
          flex: '1 1 0',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          padding: '64px 48px',
          background: 'linear-gradient(135deg, #0B5EAB 0%, #1677FF 50%, #0958D0 100%)',
          color: '#fff',
        }}
      >
        <div style={{ maxWidth: 480 }}>
          <div style={{ fontSize: 48, marginBottom: 24 }}>🐾</div>
          <Title level={2} style={{ color: '#fff', marginBottom: 16 }}>
            宠老板管理后台
          </Title>
          <Text style={{ color: 'rgba(255,255,255,0.85)', fontSize: 16, lineHeight: 1.8 }}>
            宠物店一体化 SaaS 管理平台
            <br />
            涵盖收银、库存、会员、营销、数据全链路
          </Text>
          <div style={{ marginTop: 48, display: 'flex', gap: 32 }}>
            {[
              { label: '收银台', desc: '秒级结算' },
              { label: '会员管理', desc: '储值积分' },
              { label: '数据报表', desc: '实时洞察' },
            ].map((item) => (
              <div key={item.label}>
                <div style={{ fontSize: 18, fontWeight: 600 }}>{item.label}</div>
                <div style={{ opacity: 0.7, fontSize: 13 }}>{item.desc}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 右侧表单区 */}
      <div
        style={{
          flex: '0 0 480px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: '#fff',
          padding: '0 56px',
        }}
      >
        <div style={{ width: '100%', maxWidth: 360 }}>
          <Tabs
            activeKey={activeTab}
            onChange={setActiveTab}
            centered
            items={[
              {
                key: 'login',
                label: '登录',
                children: (
                  <Form<LoginParams>
                    name="login"
                    layout="vertical"
                    initialValues={{
                      phone: rememberedPhone,
                      remember: !!rememberedPhone,
                    }}
                    onFinish={onFinish}
                    autoComplete="off"
                    size="large"
                  >
                    <Form.Item
                      name="phone"
                      rules={[
                        { required: true, message: '请输入手机号' },
                        { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
                      ]}
                    >
                      <Input
                        prefix={
                          <Space split={<span style={{ color: '#d9d9d9' }}>|</span>}>
                            <span style={{ color: '#333' }}>+86</span>
                            <MobileOutlined style={{ color: '#bfbfbf' }} />
                          </Space>
                        }
                        placeholder="请输入手机号"
                        maxLength={11}
                      />
                    </Form.Item>

                    <Form.Item
                      name="password"
                      rules={[
                        { required: true, message: '请输入密码' },
                        { min: 6, message: '密码至少6位' },
                      ]}
                    >
                      <Input.Password
                        prefix={<LockOutlined style={{ color: '#bfbfbf' }} />}
                        placeholder="请输入密码"
                      />
                    </Form.Item>

                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
                      <Form.Item name="remember" valuePropName="checked" style={{ marginBottom: 0 }}>
                        <Checkbox>记住密码</Checkbox>
                      </Form.Item>
                      <Link onClick={() => setResetModalVisible(true)}>忘记密码</Link>
                    </div>

                    <Form.Item>
                      <Button type="primary" htmlType="submit" block size="large" loading={loading}>
                        登录
                      </Button>
                    </Form.Item>
                  </Form>
                ),
              },
              {
                key: 'register',
                label: '注册',
                children: (
                  <Form<RegisterParams>
                    name="register"
                    layout="vertical"
                    onFinish={onRegister}
                    autoComplete="off"
                    size="large"
                  >
                    <Form.Item
                      name="phone"
                      rules={[
                        { required: true, message: '请输入手机号' },
                        { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
                      ]}
                    >
                      <Input
                        prefix={
                          <Space split={<span style={{ color: '#d9d9d9' }}>|</span>}>
                            <span style={{ color: '#333' }}>+86</span>
                            <MobileOutlined style={{ color: '#bfbfbf' }} />
                          </Space>
                        }
                        placeholder="请输入手机号"
                        maxLength={11}
                      />
                    </Form.Item>

                    <Form.Item name="code" rules={[{ required: true, message: '请输入验证码' }]}>
                      <Space.Compact style={{ width: '100%' }}>
                        <Input
                          prefix={<SafetyOutlined style={{ color: '#bfbfbf' }} />}
                          placeholder="短信验证码"
                          maxLength={6}
                        />
                        <Button
                          disabled={countdown > 0}
                          loading={sendCodeLoading}
                          onClick={(e) => {
                            const phone = (e.currentTarget.closest('form')?.querySelector('input[name=phone]') as HTMLInputElement)?.value;
                            handleSendCode(phone, 'register');
                          }}
                          style={{ width: 120 }}
                        >
                          {countdown > 0 ? `${countdown}s` : '获取验证码'}
                        </Button>
                      </Space.Compact>
                    </Form.Item>

                    <Form.Item
                      name="password"
                      rules={[
                        { required: true, message: '请输入密码' },
                        { min: 6, message: '密码至少6位' },
                      ]}
                    >
                      <Input.Password
                        prefix={<LockOutlined style={{ color: '#bfbfbf' }} />}
                        placeholder="设置密码（至少6位）"
                      />
                    </Form.Item>

                    <Form.Item>
                      <Button type="primary" htmlType="submit" block size="large" loading={loading}>
                        注册
                      </Button>
                    </Form.Item>
                  </Form>
                ),
              },
            ]}
          />

          <div style={{ textAlign: 'center', color: '#999', fontSize: 12, marginTop: 8 }}>
            宠老板 SaaS · 教学演示版本
          </div>
        </div>
      </div>

      {/* 忘记密码弹窗 */}
      <Modal
        title="重置密码"
        open={resetModalVisible}
        footer={null}
        onCancel={() => setResetModalVisible(false)}
        destroyOnClose
        width={420}
      >
        <Form<ResetPasswordParams>
          layout="vertical"
          onFinish={handleResetPassword}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="phone"
            rules={[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
            ]}
          >
            <Input
              prefix={
                <Space split={<span style={{ color: '#d9d9d9' }}>|</span>}>
                  <span style={{ color: '#333' }}>+86</span>
                  <MobileOutlined style={{ color: '#bfbfbf' }} />
                </Space>
              }
              placeholder="请输入手机号"
              maxLength={11}
            />
          </Form.Item>

          <Form.Item name="code" rules={[{ required: true, message: '请输入验证码' }]}>
            <Space.Compact style={{ width: '100%' }}>
              <Input
                prefix={<SafetyOutlined style={{ color: '#bfbfbf' }} />}
                placeholder="短信验证码"
                maxLength={6}
              />
              <Button
                disabled={resetCountdown > 0}
                loading={resetSendLoading}
                onClick={(e) => {
                  const phone = (e.currentTarget.closest('form')?.querySelector('input[name=phone]') as HTMLInputElement)?.value;
                  handleResetSendCode(phone);
                }}
                style={{ width: 120 }}
              >
                {resetCountdown > 0 ? `${resetCountdown}s` : '获取验证码'}
              </Button>
            </Space.Compact>
          </Form.Item>

          <Form.Item
            name="newPassword"
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 6, message: '密码至少6位' },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: '#bfbfbf' }} />}
              placeholder="设置新密码（至少6位）"
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block size="large" loading={resetLoading}>
              确认重置
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
