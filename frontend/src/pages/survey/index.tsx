import { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, List, Tag, Empty, Spin, Space } from 'antd';
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

  if (loading) return <Spin style={{ display: 'block', margin: '100px auto' }} />;
  if (!data) return <Empty description="暂无数据" />;

  const { statistics, todo, shop, news } = data;

  return (
    <div>
      {/* 统计卡 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic title="今日订单" value={statistics.todayOrder} />
            <div style={{ marginTop: 8 }}>较昨日 <TrendTag value={statistics.orderTrend} /></div>
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="今日会员" value={statistics.todayMember} />
            <div style={{ marginTop: 8 }}>较昨日 <TrendTag value={statistics.memberTrend} /></div>
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="今日提醒" value={statistics.todayReminder} />
            <div style={{ marginTop: 8 }}>较昨日 <TrendTag value={statistics.reminderTrend} /></div>
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="今日预约" value={statistics.todayAppointment} />
            <div style={{ marginTop: 8 }}>较昨日 <TrendTag value={statistics.appointmentTrend} /></div>
          </Card>
        </Col>
      </Row>

      <Row gutter={16}>
        {/* 待办区 */}
        <Col span={12}>
          <Card title="待办事项">
            <Row gutter={[16, 16]}>
              <Col span={8}><Card size="small"><Statistic title="待付款" value={todo.waitPay} /></Card></Col>
              <Col span={8}><Card size="small"><Statistic title="待发货" value={todo.waitDeliver} /></Card></Col>
              <Col span={8}><Card size="small"><Statistic title="待退款" value={todo.waitRefund} /></Card></Col>
              <Col span={8}><Card size="small"><Statistic title="预约待处理" value={todo.waitAppointment} /></Card></Col>
              <Col span={8}><Card size="small"><Statistic title="库存不足" value={todo.stockShortage} /></Card></Col>
            </Row>
          </Card>
        </Col>

        {/* 店铺信息 */}
        <Col span={12}>
          <Card title="店铺信息">
            <List>
              <List.Item><span>店铺名称：</span>{shop.shopName}</List.Item>
              <List.Item><span>绑定号码：</span>{shop.bindPhone}</List.Item>
              <List.Item><span>到期时间：</span>{shop.expireTime}</List.Item>
              <List.Item><span>短信余量：</span>{shop.smsRemain} 条</List.Item>
            </List>
          </Card>
        </Col>
      </Row>

      {/* 公告资讯 */}
      <Card title="公告与资讯" style={{ marginTop: 16 }}>
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
      </Card>
    </div>
  );
}
