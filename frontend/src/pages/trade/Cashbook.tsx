import { useEffect, useState } from 'react';
import { Space, Select, DatePicker, Button, Tag } from 'antd';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import { pageCashbook } from '@/api/trade';
import type { CashbookVO, CashbookPageReq } from '@/types/api';

const { RangePicker } = DatePicker;

const TYPE_MAP: Record<number, { text: string; color: string }> = {
  1: { text: '收入', color: 'red' },
  2: { text: '支出', color: 'green' },
};

const fmt = (v: string | undefined) => (v ? v : '-');

export default function Cashbook() {
  const [data, setData] = useState<CashbookVO[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [params, setParams] = useState<CashbookPageReq>({ pageNum: 1, pageSize: 20 });

  const loadData = () => {
    setLoading(true);
    pageCashbook(params)
      .then((res) => {
        setData(res.data);
        setTotal((res.page as any)?.total || 0);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, [params]);

  return (
    <PageContainer title="记账本">
      <Space style={{ marginBottom: 16 }} wrap>
        <Select
          placeholder="收支类型"
          allowClear
          style={{ width: 140 }}
          options={[
            { value: 1, label: '收入' },
            { value: 2, label: '支出' },
          ]}
          onChange={(v) => setParams({ ...params, pageNum: 1, type: v })}
        />
        <RangePicker
          onChange={(v) =>
            setParams({
              ...params,
              pageNum: 1,
              startTime: v?.[0]?.format('YYYY-MM-DD'),
              endTime: v?.[1]?.format('YYYY-MM-DD'),
            })
          }
        />
        <Button type="primary" onClick={loadData}>查询</Button>
      </Space>

      <ProTable<CashbookVO>
        search={false}
        options={{ reload: loadData, density: true, setting: true }}
        rowKey="id"
        loading={loading}
        dataSource={data}
        pagination={{
          current: params.pageNum,
          pageSize: params.pageSize,
          total,
          onChange: (page, pageSize) => setParams({ ...params, pageNum: page, pageSize }),
        }}
        columns={[
          { title: '流水号', dataIndex: 'id', width: 90, search: false },
          {
            title: '类型', dataIndex: 'type', width: 90,
            render: (_, r: CashbookVO) => {
              const t = TYPE_MAP[r.type];
              return t ? <Tag color={t.color}>{t.text}</Tag> : <span>{r.type}</span>;
            },
          },
          {
            title: '金额', dataIndex: 'amount', width: 130,
            render: (_, r: CashbookVO) => (
              <span style={{ color: r.type === 1 ? '#cf1322' : '#3f8600', fontWeight: 600 }}>
                {r.type === 1 ? '+' : '-'}¥{(r.amount ?? 0).toFixed(2)}
              </span>
            ),
          },
          { title: '业务类型', dataIndex: 'bizType', width: 120 },
          { title: '关联订单', dataIndex: 'orderId', width: 120, render: (_, r: CashbookVO) => (r.orderId ? r.orderId : '-') },
          { title: '操作人ID', dataIndex: 'clerkId', width: 110, render: (_, r: CashbookVO) => (r.clerkId ? r.clerkId : '-') },
          { title: '备注', dataIndex: 'memo', ellipsis: true, render: (_, r: CashbookVO) => fmt(r.memo) },
          { title: '创建时间', dataIndex: 'createTime', width: 180, search: false },
        ]}
      />
    </PageContainer>
  );
}
