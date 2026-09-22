import { useEffect, useState } from 'react';
import {
  Card, Table, Tag, Input, Select, Space, Modal, InputNumber, message,
} from 'antd';
import { pageStock, setStockWarnQty } from '@/api/stock';
import type { StockVO, StockPageReq } from '@/types/m4';

export default function StockIndex() {
  const [data, setData] = useState<StockVO[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [params, setParams] = useState<StockPageReq>({ pageNum: 1, pageSize: 20 });
  const [warnModalOpen, setWarnModalOpen] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<StockVO | null>(null);
  const [warnQty, setWarnQty] = useState<number>(0);

  const loadData = () => {
    setLoading(true);
    pageStock(params).then((res) => {
      setData(res.data);
      setTotal((res.page as any)?.total || 0);
    }).finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, [params]);

  const openWarn = (r: StockVO) => {
    setCurrentRecord(r);
    setWarnQty(r.warnQty || 0);
    setWarnModalOpen(true);
  };

  const saveWarn = async () => {
    if (!currentRecord) return;
    await setStockWarnQty(currentRecord.id, warnQty);
    message.success('预警阈值已设置');
    setWarnModalOpen(false);
    loadData();
  };

  return (
    <Card>
      <Space wrap style={{ marginBottom: 16 }}>
        <Select
          placeholder="显示"
          style={{ width: 140 }}
          defaultValue={false}
          onChange={(v) => setParams({ ...params, pageNum: 1, warnOnly: v })}
          options={[
            { value: false, label: '全部库存' },
            { value: true, label: '仅预警库存' },
          ]}
        />
        <Input.Search
          placeholder="商品ID/SKU ID"
          allowClear
          style={{ width: 200 }}
          onSearch={(v) => setParams({ ...params, pageNum: 1, keyword: v })}
        />
      </Space>

      <Table
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
          { title: '商品ID', dataIndex: 'goodsId', width: 120 },
          { title: 'SKU ID', dataIndex: 'skuId', width: 120 },
          {
            title: '当前库存', dataIndex: 'quantity', width: 110,
            render: (v: number, r: StockVO) => {
              const warn = (r.warnQty || 0) > 0 && v <= r.warnQty;
              return <Tag color={warn ? 'red' : 'green'}>{v}</Tag>;
            },
          },
          { title: '预警阈值', dataIndex: 'warnQty', width: 100 },
          { title: '均价', dataIndex: 'avgCost', width: 100, render: (v: number) => `¥${v?.toFixed(2)}` },
          { title: '更新时间', dataIndex: 'updateTime', width: 170 },
          {
            title: '操作', width: 120,
            render: (_, r) => (
              <Space>
                <a onClick={() => openWarn(r)}>设置预警</a>
              </Space>
            ),
          },
        ]}
      />

      <Modal
        title="设置预警阈值"
        open={warnModalOpen}
        onCancel={() => setWarnModalOpen(false)}
        onOk={saveWarn}
      >
        <div style={{ marginBottom: 8 }}>商品ID：{currentRecord?.goodsId}</div>
        <InputNumber
          min={0}
          value={warnQty}
          onChange={(v) => setWarnQty(v ?? 0)}
          style={{ width: '100%' }}
          placeholder="预警库存数量"
        />
        <div style={{ color: '#888', marginTop: 8, fontSize: 12 }}>
          当库存数量小于等于该值时标记为预警
        </div>
      </Modal>
    </Card>
  );
}
