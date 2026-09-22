import { useEffect, useState } from 'react';
import { Card, DatePicker, Table, Statistic, Row, Col, Space, Spin, Empty } from 'antd';
import { reportDaily } from '@/api/report';
import type { DailyReportVO } from '@/types/m6';

const { RangePicker } = DatePicker;

export default function ReportDaily() {
  const [data, setData] = useState<DailyReportVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [range, setRange] = useState<[any, any] | null>(null);

  const loadData = () => {
    setLoading(true);
    const start = range?.[0]?.format('YYYY-MM-DD');
    const end = range?.[1]?.format('YYYY-MM-DD');
    reportDaily(start, end)
      .then((res) => setData(res.data))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, []);

  const totalSale = data.reduce((s, d) => s + (d.saleAmount || 0), 0);
  const totalOrder = data.reduce((s, d) => s + (d.orderCount || 0), 0);
  const totalRefund = data.reduce((s, d) => s + (d.refundAmount || 0), 0);
  const avgOrder = totalOrder > 0 ? totalSale / totalOrder : 0;

  return (
    <div>
      <Card style={{ marginBottom: 16 }}>
        <Space>
          <span>日期范围：</span>
          <RangePicker
            value={range}
            onChange={(v) => setRange(v as any)}
            allowClear={false}
          />
          <a onClick={loadData}>查询</a>
        </Space>
      </Card>

      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}><Card><Statistic title="区间销售额" value={totalSale} prefix="¥" precision={2} /></Card></Col>
        <Col span={6}><Card><Statistic title="区间订单数" value={totalOrder} /></Card></Col>
        <Col span={6}><Card><Statistic title="区间退款" value={totalRefund} prefix="¥" precision={2} valueStyle={{ color: '#cf1322' }} /></Card></Col>
        <Col span={6}><Card><Statistic title="区间客单价" value={avgOrder} prefix="¥" precision={2} /></Card></Col>
      </Row>

      <Card title="每日明细">
        {loading ? <Spin /> : data.length === 0 ? <Empty /> : (
          <Table
            rowKey="reportDate"
            dataSource={data}
            pagination={false}
            columns={[
              { title: '日期', dataIndex: 'reportDate', width: 120 },
              { title: '销售额', dataIndex: 'saleAmount', render: (v: number) => `¥${(v || 0).toFixed(2)}` },
              { title: '订单数', dataIndex: 'orderCount' },
              { title: '退款', dataIndex: 'refundAmount', render: (v: number) => `¥${(v || 0).toFixed(2)}` },
              { title: '客单价', dataIndex: 'avgOrder', render: (v: number) => `¥${(v || 0).toFixed(2)}` },
              { title: '消费会员数', dataIndex: 'memberCount' },
            ]}
          />
        )}
      </Card>
    </div>
  );
}
