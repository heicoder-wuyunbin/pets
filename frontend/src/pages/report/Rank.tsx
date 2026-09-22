import { useEffect, useState } from 'react';
import { Card, Segmented, Table, InputNumber, Space, DatePicker, Button, Spin, Empty } from 'antd';
import { reportSalesRank } from '@/api/report';
import type { RankVO } from '@/types/m6';

const { RangePicker } = DatePicker;

export default function ReportRank() {
  const [type, setType] = useState<'goods' | 'service' | 'clerk'>('goods');
  const [topN, setTopN] = useState(10);
  const [range, setRange] = useState<[any, any] | null>(null);
  const [data, setData] = useState<RankVO[]>([]);
  const [loading, setLoading] = useState(false);

  const loadData = () => {
    setLoading(true);
    const start = range?.[0]?.format('YYYY-MM-DD HH:mm:ss');
    const end = range?.[1]?.format('YYYY-MM-DD HH:mm:ss');
    reportSalesRank(type, topN, start, end)
      .then((r) => setData(r.data))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, [type]);

  const columns = [
    { title: '排名', width: 70, render: (_: any, __: any, i: number) => i + 1 },
    { title: type === 'clerk' ? '员工' : '名称', dataIndex: 'itemName' },
    { title: type === 'service' ? '服务次数' : '销量', dataIndex: 'count' },
    ...(type !== 'service' ? [{
      title: '销售额', dataIndex: 'amount', render: (v: number) => `¥${(v || 0).toFixed(2)}`,
    }] : []),
  ];

  return (
    <Card title="销售排行">
      <Space wrap style={{ marginBottom: 16 }}>
        <Segmented
          value={type}
          onChange={(v) => setType(v as any)}
          options={[
            { value: 'goods', label: '商品销量' },
            { value: 'service', label: '服务次数' },
            { value: 'clerk', label: '员工业绩' },
          ]}
        />
        <span>Top:</span>
        <InputNumber min={1} max={100} value={topN} onChange={(v) => setTopN(v || 10)} />
        <RangePicker value={range} onChange={(v) => setRange(v as any)} />
        <Button type="primary" onClick={loadData}>查询</Button>
      </Space>

      <Spin spinning={loading}>
        {data.length === 0 ? <Empty /> : (
          <Table
            rowKey="itemId"
            dataSource={data}
            pagination={false}
            columns={columns as any}
          />
        )}
      </Spin>
    </Card>
  );
}
