import { useEffect, useState } from 'react';
import { PageContainer, StatisticCard, ProCard } from '@ant-design/pro-components';
import { List, Tag, Empty, Space } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';
import { getSurvey } from '@/api/survey';
import type { SurveyVO } from '@/types/api';

function TrendTag({ value }: { value: string }) {
  const num = parseFloat(value);
  if (num > 0) return <Tag color="green"><ArrowUpOutlined /> {value}</Tag>;
  if (num < 0) return <Tag color="red"><ArrowDownOutlined /> {value}</Tag>;
  return <Tag>{value}</Tag>;
}

export default function Survey() {
  const [data, setData] = useState<SurveyVO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getSurvey()
      .then((res) => setData(res.data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <PageContainer title="店铺概况" loading />;
  if (!data) return <PageContainer title="店铺概况"><Empty description="暂无数据" /></PageContainer>;

  const { statistics, todo, shop, news } = data;

  return (
    <PageContainer title="店铺概况" content={`${shop.shopName} · 数据概览`}>
      {/* 今日核心指标 */}
      <StatisticCard.Group direction="row" loading={loading}>
        <StatisticCard
          title="今日订单"
          statistic={{ value: statistics.todayOrder, description: <span>较昨日 <TrendTag value={statistics.orderTrend} /></span> }}
        />
        <StatisticCard
          title="今日会员"
          statistic={{ value: statistics.todayMember, description: <span>较昨日 <TrendTag value={statistics.memberTrend} /></span> }}
        />
        <StatisticCard
          title="今日提醒"
          statistic={{ value: statistics.todayReminder, description: <span>较昨日 <TrendTag value={statistics.reminderTrend} /></span> }}
        />
        <StatisticCard
          title="今日预约"
          statistic={{ value: statistics.todayAppointment, description: <span>较昨日 <TrendTag value={statistics.appointmentTrend} /></span> }}
        />
      </StatisticCard.Group>

      {/* 待办 + 店铺信息 */}
      <ProCard gutter={16} style={{ marginTop: 16 }} ghost>
        <ProCard colSpan={12} title="待办事项" headerBordered>
          <ProCard ghost gutter={[16, 16]}>
            <ProCard colSpan={8} bordered><StatisticCard statistic={{ title: '待付款', value: todo.waitPay }} /></ProCard>
            <ProCard colSpan={8} bordered><StatisticCard statistic={{ title: '待发货', value: todo.waitDeliver }} /></ProCard>
            <ProCard colSpan={8} bordered><StatisticCard statistic={{ title: '待退款', value: todo.waitRefund }} /></ProCard>
            <ProCard colSpan={8} bordered><StatisticCard statistic={{ title: '预约待处理', value: todo.waitAppointment }} /></ProCard>
            <ProCard colSpan={8} bordered><StatisticCard statistic={{ title: '库存不足', value: todo.stockShortage }} /></ProCard>
          </ProCard>
        </ProCard>

        <ProCard colSpan={12} title="店铺信息" headerBordered>
          <List>
            <List.Item><span>店铺名称：</span>{shop.shopName}</List.Item>
            <List.Item><span>绑定号码：</span>{shop.bindPhone}</List.Item>
            <List.Item><span>到期时间：</span>{shop.expireTime}</List.Item>
            <List.Item><span>短信余量：</span>{shop.smsRemain} 条</List.Item>
          </List>
        </ProCard>
      </ProCard>

      {/* 公告与资讯 */}
      <ProCard title="公告与资讯" style={{ marginTop: 16 }} headerBordered>
        {news.length === 0 ? (
          <Empty description="暂无公告" />
        ) : (
          <List
            dataSource={news}
            renderItem={(item) => (
              <List.Item>
                <Space>
                  <Tag color={item.type === 'announcement' ? 'blue' : 'green'}>
                    {item.type === 'announcement' ? '公告' : '资讯'}
                  </Tag>
                  <span>{item.title}</span>
                </Space>
                <span style={{ color: '#999' }}>{item.time}</span>
              </List.Item>
            )}
          />
        )}
      </ProCard>
    </PageContainer>
  );
}
