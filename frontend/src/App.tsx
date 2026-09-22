import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider, App as AntdApp, theme } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { AuthGuard } from '@/router';
import MainLayout from '@/layouts/MainLayout';
import Login from '@/pages/login/Login';
import SelectShop from '@/pages/login/SelectShop';
import Survey from '@/pages/survey';
import Cashier from '@/pages/cashier';
import Order from '@/pages/order';
import ShopSetting from '@/pages/shop/Setting';
import Notice from '@/pages/shop/Notice';
import Clerk from '@/pages/system/Clerk';
import SysParam from '@/pages/system/Param';
import OperationLog from '@/pages/system/OperationLog';
import Goods from '@/pages/goods/Goods';
import Service from '@/pages/goods/Service';
import StockIndex from '@/pages/stock';
import StockIn from '@/pages/stock/In';
import StockOut from '@/pages/stock/Out';
import MemberList from '@/pages/member/List';
import MemberCard from '@/pages/member/Card';
import MemberTimesCard from '@/pages/member/TimesCard';
import MemberPoints from '@/pages/member/Points';
import MarketingList from '@/pages/marketing/List';
import ReportDaily from '@/pages/report/Daily';
import ReportStat from '@/pages/report/Stat';
import ReportRank from '@/pages/report/Rank';
import ComingSoon from '@/components/ComingSoon';

export default function RootApp() {
  return (
    <ConfigProvider
      locale={zhCN}
      theme={{
        algorithm: theme.defaultAlgorithm,
        token: {
          colorPrimary: '#1677FF',
          borderRadius: 6,
          fontSize: 14,
        },
      }}
    >
      <AntdApp>
        <BrowserRouter>
          <Routes>
            {/* 公开路由 */}
            <Route path="/login" element={<Login />} />
            <Route path="/select-shop" element={<SelectShop />} />
            {/* 受保护路由 */}
            <Route element={<AuthGuard />}>
              <Route element={<MainLayout />}>
                <Route path="/" element={<Navigate to="/survey" replace />} />
                <Route path="/survey" element={<Survey />} />
                {/* 接待管理 */}
                <Route path="/work-calendar" element={<ComingSoon title="工作日历" />} />
                {/* 店面业务 */}
                <Route path="/cashier" element={<Cashier />} />
                <Route path="/order" element={<Order />} />
                <Route path="/reservation" element={<ComingSoon title="预约服务" />} />
                <Route path="/washreport" element={<ComingSoon title="洗护报告" />} />
                <Route path="/foster" element={<ComingSoon title="寄养管理" />} />
                <Route path="/reminder" element={<ComingSoon title="提醒" />} />
                <Route path="/accounting" element={<ComingSoon title="记账本" />} />
                <Route path="/petpay" element={<ComingSoon title="宠付宝" />} />
                <Route path="/referral" element={<ComingSoon title="转诊管理" />} />
                {/* 商品服务 */}
                <Route path="/goods" element={<Goods />} />
                <Route path="/service" element={<Service />} />
                <Route path="/live-pet" element={<ComingSoon title="活体管理" />} />
                {/* 库存管理 */}
                <Route path="/stock" element={<StockIndex />} />
                <Route path="/stock/in" element={<StockIn />} />
                <Route path="/stock/out" element={<StockOut />} />
                <Route path="/stock/inventory" element={<ComingSoon title="盘点管理" />} />
                {/* 客户中心 */}
                <Route path="/member" element={<MemberList />} />
                <Route path="/member/pet" element={<ComingSoon title="宠物管理" />} />
                <Route path="/member/card" element={<MemberCard />} />
                <Route path="/member/timescard" element={<MemberTimesCard />} />
                <Route path="/member/points" element={<MemberPoints />} />
                <Route path="/member/marketing" element={<ComingSoon title="会员营销" />} />
                {/* 营销中心 */}
                <Route path="/marketing" element={<MarketingList />} />
                <Route path="/marketing/tool" element={<ComingSoon title="营销工具" />} />
                {/* 数据统计 */}
                <Route path="/report/overview" element={<ComingSoon title="数据总览" />} />
                <Route path="/report/daily" element={<ReportDaily />} />
                <Route path="/report/rank" element={<ReportRank />} />
                <Route path="/report/stat" element={<ReportStat />} />
                <Route path="/report/clerk" element={<ComingSoon title="店员报表" />} />
                {/* 微信商城 */}
                <Route path="/mall/account" element={<ComingSoon title="我的账户" />} />
                <Route path="/mall/goods" element={<ComingSoon title="商城商品" />} />
                <Route path="/mall/order" element={<ComingSoon title="线上订单" />} />
                <Route path="/mall/distributor" element={<ComingSoon title="分销员" />} />
                <Route path="/mall/supply" element={<ComingSoon title="一件代发" />} />
                <Route path="/mall/decorate" element={<ComingSoon title="店铺装修" />} />
                <Route path="/mall/config" element={<ComingSoon title="商城配置" />} />
                <Route path="/mall/wechat" element={<ComingSoon title="公众号" />} />
                {/* 新零售 */}
                <Route path="/retail/meituan-waimai" element={<ComingSoon title="美团外卖" />} />
                <Route path="/retail/meituan-tuangou" element={<ComingSoon title="美团团购" />} />
                <Route path="/retail/taobao" element={<ComingSoon title="淘宝闪购" />} />
                <Route path="/retail/jingdong" element={<ComingSoon title="京东秒送" />} />
                <Route path="/retail/douyin" element={<ComingSoon title="抖音小时达" />} />
                <Route path="/retail/anxin" element={<ComingSoon title="安心生活" />} />
                {/* 基础配置 */}
                <Route path="/shop/setting" element={<ShopSetting />} />
                <Route path="/shop/notice" element={<Notice />} />
                <Route path="/system/clerk" element={<Clerk />} />
                <Route path="/system/param" element={<SysParam />} />
                <Route path="/system/setting" element={<ComingSoon title="系统设置" />} />
                <Route path="/system/print" element={<ComingSoon title="打印设置" />} />
                <Route path="/system/push" element={<ComingSoon title="消息推送" />} />
                <Route path="/system/recycle" element={<ComingSoon title="回收站" />} />
                <Route path="/system/operation-log" element={<OperationLog />} />
              </Route>
            </Route>
          </Routes>
        </BrowserRouter>
      </AntdApp>
    </ConfigProvider>
  );
}
