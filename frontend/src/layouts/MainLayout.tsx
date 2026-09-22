import { useState } from 'react';
import { Layout, Menu, Avatar, Dropdown, Space } from 'antd';
import {
  DashboardOutlined,
  CalendarOutlined,
  ShoppingCartOutlined,
  OrderedListOutlined,
  ScheduleOutlined,
  FileTextOutlined,
  HomeOutlined,
  BellOutlined,
  AccountBookOutlined,
  WalletOutlined,
  ShareAltOutlined,
  AppstoreOutlined,
  ToolOutlined,
  DatabaseOutlined,
  AuditOutlined,
  TeamOutlined,
  UserOutlined,
  CreditCardOutlined,
  GiftOutlined,
  StarOutlined,
  ThunderboltOutlined,
  BarChartOutlined,
  FundOutlined,
  TrophyOutlined,
  SettingOutlined,
  PrinterOutlined,
  NotificationOutlined,
  DeleteOutlined,
  LogoutOutlined,
  ShopOutlined,
  GlobalOutlined,
  MobileOutlined,
} from '@ant-design/icons';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

const { Header, Sider, Content } = Layout;

const menuItems = [
  { key: '/survey', icon: <DashboardOutlined />, label: '店铺概况' },
  {
    key: 'g-reception', icon: <CalendarOutlined />, label: '接待管理',
    children: [
      { key: '/work-calendar', icon: <CalendarOutlined />, label: '工作日历' },
    ],
  },
  {
    key: 'g-business', icon: <ShoppingCartOutlined />, label: '店面业务',
    children: [
      { key: '/cashier', icon: <ShoppingCartOutlined />, label: '收银台' },
      { key: '/order', icon: <OrderedListOutlined />, label: '订单管理' },
      { key: '/reservation', icon: <ScheduleOutlined />, label: '预约服务' },
      { key: '/washreport', icon: <FileTextOutlined />, label: '洗护报告' },
      { key: '/foster', icon: <HomeOutlined />, label: '寄养管理' },
      { key: '/reminder', icon: <BellOutlined />, label: '提醒' },
      { key: '/accounting', icon: <AccountBookOutlined />, label: '记账本' },
      { key: '/petpay', icon: <WalletOutlined />, label: '宠付宝' },
      { key: '/referral', icon: <ShareAltOutlined />, label: '转诊管理' },
    ],
  },
  {
    key: 'g-commodity', icon: <AppstoreOutlined />, label: '商品服务',
    children: [
      { key: '/goods', icon: <AppstoreOutlined />, label: '商品管理' },
      { key: '/service', icon: <ToolOutlined />, label: '服务管理' },
      { key: '/live-pet', icon: <HomeOutlined />, label: '活体管理' },
    ],
  },
  {
    key: 'g-stock', icon: <DatabaseOutlined />, label: '库存管理',
    children: [
      { key: '/stock', icon: <DatabaseOutlined />, label: '商品库存' },
      { key: '/stock/in', icon: <FundOutlined />, label: '入库管理' },
      { key: '/stock/out', icon: <ShareAltOutlined />, label: '出库管理' },
      { key: '/stock/inventory', icon: <AuditOutlined />, label: '盘点管理' },
    ],
  },
  {
    key: 'g-member', icon: <UserOutlined />, label: '客户中心',
    children: [
      { key: '/member', icon: <TeamOutlined />, label: '客户管理' },
      { key: '/member/pet', icon: <HomeOutlined />, label: '宠物管理' },
      { key: '/member/card', icon: <CreditCardOutlined />, label: '会员卡模板' },
      { key: '/member/timescard', icon: <GiftOutlined />, label: '次卡模板' },
      { key: '/member/points', icon: <StarOutlined />, label: '积分' },
      { key: '/member/marketing', icon: <ThunderboltOutlined />, label: '会员营销' },
    ],
  },
  {
    key: 'g-market', icon: <ThunderboltOutlined />, label: '营销中心',
    children: [
      { key: '/marketing/tool', icon: <ThunderboltOutlined />, label: '营销工具' },
      { key: '/marketing', icon: <SettingOutlined />, label: '营销管理' },
    ],
  },
  {
    key: 'g-report', icon: <BarChartOutlined />, label: '数据统计',
    children: [
      { key: '/report/overview', icon: <DashboardOutlined />, label: '数据总览' },
      { key: '/report/daily', icon: <FundOutlined />, label: '营业报表' },
      { key: '/report/rank', icon: <TrophyOutlined />, label: '销售报表' },
      { key: '/report/stat', icon: <TeamOutlined />, label: '用户报表' },
      { key: '/report/clerk', icon: <TeamOutlined />, label: '店员报表' },
    ],
  },
  {
    key: 'g-mall', icon: <MobileOutlined />, label: '微信商城',
    children: [
      { key: '/mall/account', icon: <WalletOutlined />, label: '我的账户' },
      { key: '/mall/goods', icon: <AppstoreOutlined />, label: '商城商品' },
      { key: '/mall/order', icon: <OrderedListOutlined />, label: '线上订单' },
      { key: '/mall/distributor', icon: <TeamOutlined />, label: '分销员' },
      { key: '/mall/supply', icon: <ShopOutlined />, label: '一件代发' },
      { key: '/mall/decorate', icon: <SettingOutlined />, label: '店铺装修' },
      { key: '/mall/config', icon: <SettingOutlined />, label: '商城配置' },
      { key: '/mall/wechat', icon: <NotificationOutlined />, label: '公众号' },
    ],
  },
  {
    key: 'g-retail', icon: <GlobalOutlined />, label: '新零售',
    children: [
      { key: '/retail/meituan-waimai', icon: <ShoppingCartOutlined />, label: '美团外卖' },
      { key: '/retail/meituan-tuangou', icon: <ShopOutlined />, label: '美团团购' },
      { key: '/retail/taobao', icon: <ShopOutlined />, label: '淘宝闪购' },
      { key: '/retail/jingdong', icon: <ShoppingCartOutlined />, label: '京东秒送' },
      { key: '/retail/douyin', icon: <MobileOutlined />, label: '抖音小时达' },
      { key: '/retail/anxin', icon: <HomeOutlined />, label: '安心生活' },
    ],
  },
  {
    key: 'g-config', icon: <SettingOutlined />, label: '基础配置',
    children: [
      { key: '/shop/setting', icon: <ShopOutlined />, label: '店铺管理' },
      { key: '/system/clerk', icon: <TeamOutlined />, label: '店员管理' },
      { key: '/system/param', icon: <SettingOutlined />, label: '参数设置' },
      { key: '/system/setting', icon: <SettingOutlined />, label: '系统设置' },
      { key: '/system/print', icon: <PrinterOutlined />, label: '打印设置' },
      { key: '/system/push', icon: <NotificationOutlined />, label: '消息推送' },
      { key: '/system/recycle', icon: <DeleteOutlined />, label: '回收站' },
      { key: '/system/operation-log', icon: <FileTextOutlined />, label: '操作日志' },
    ],
  },
];

export default function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuthStore();
  const [collapsed, setCollapsed] = useState(false);

  const selectedKey = location.pathname;
  const siderWidth = collapsed ? 80 : 208;

  // 默认展开包含当前路由的子菜单组
  const openKeys = menuItems
    .filter((item) => 'children' in item && item.children?.some((c) => c.key === selectedKey))
    .map((item) => item.key);

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        breakpoint="lg"
        width={208}
        style={{ overflow: 'auto', height: '100vh', position: 'fixed', left: 0, top: 0, bottom: 0 }}
      >
        <div
          style={{
            height: 56,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontSize: collapsed ? 14 : 16,
            fontWeight: 600,
          }}
        >
          {collapsed ? '🐾' : '🐾 宠老板'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[selectedKey]}
          defaultOpenKeys={openKeys}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          style={{ borderRight: 0 }}
        />
      </Sider>
      <Layout style={{ marginLeft: siderWidth, transition: 'margin-left 0.2s' }}>
        <Header
          style={{
            position: 'sticky',
            top: 0,
            zIndex: 9,
            width: '100%',
            padding: '0 24px',
            background: '#fff',
            display: 'flex',
            justifyContent: 'flex-end',
            alignItems: 'center',
            boxShadow: '0 1px 4px rgba(0,21,41,0.08)',
          }}
        >
          <Dropdown
            menu={{
              items: [{ key: 'logout', icon: <LogoutOutlined />, label: '退出登录' }],
              onClick: ({ key }) => {
                if (key === 'logout') {
                  logout();
                  navigate('/login');
                }
              },
            }}
          >
            <Space style={{ cursor: 'pointer' }}>
              <Avatar style={{ backgroundColor: '#1677ff' }} icon={<UserOutlined />} />
              <span>{user?.shopName || '未登录'}</span>
            </Space>
          </Dropdown>
        </Header>
        <Content style={{ margin: '16px 24px 24px' }}>
          <div style={{ background: '#fff', padding: 24, borderRadius: 8, minHeight: 360 }}>
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}
