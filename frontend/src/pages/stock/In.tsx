import { useEffect, useState } from 'react';
import {
  Card, Table, Input, Button, Space, Modal, Form, InputNumber, message,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { pageStockIn, createStockIn } from '@/api/stock';
import type { StockInVO, StockInSaveReq } from '@/types/m4';

export default function StockIn() {
  const [data, setData] = useState<StockInVO[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm<StockInSaveReq>();

  const loadData = () => {
    setLoading(true);
    pageStockIn(pageNum, 20, keyword).then((res) => {
      setData(res.data);
      setTotal((res.page as any)?.total || 0);
    }).finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, [pageNum]);

  const openAdd = () => {
    form.resetFields();
    form.setFieldsValue({ items: [{ goodsId: 0, qty: 1, price: 0 }] });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const no = await createStockIn(values);
    message.success(`入库单创建成功：${no.data}`);
    setModalOpen(false);
    loadData();
  };

  return (
    <Card>
      <Space wrap style={{ marginBottom: 16 }}>
        <Input.Search
          placeholder="入库单号/供应商"
          allowClear
          style={{ width: 220 }}
          onSearch={(v) => { setKeyword(v); setPageNum(1); loadData(); }}
        />
        <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>新增入库</Button>
      </Space>

      <Table
        rowKey="id"
        loading={loading}
        dataSource={data}
        pagination={{
          current: pageNum, pageSize: 20, total,
          onChange: (p) => setPageNum(p),
        }}
        columns={[
          { title: '入库单号', dataIndex: 'no', width: 180 },
          { title: '供应商', dataIndex: 'supplier' },
          { title: '总金额', dataIndex: 'totalAmount', width: 110, render: (v: number) => `¥${v?.toFixed(2)}` },
          { title: '状态', dataIndex: 'status', width: 80, render: (s: number) => s === 1 ? '已入库' : '已取消' },
          { title: '备注', dataIndex: 'remark' },
          { title: '创建时间', dataIndex: 'createTime', width: 170 },
        ]}
      />

      <Modal
        title="新增入库单"
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        width={720}
      >
        <Form form={form} layout="vertical">
          <Space wrap>
            <Form.Item name="supplier" label="供应商" style={{ width: 240 }}><Input /></Form.Item>
            <Form.Item name="remark" label="备注" style={{ width: 300 }}><Input /></Form.Item>
          </Space>
          <Form.List name="items">
            {(fields, { add, remove }) => (
              <>
                <div style={{ fontWeight: 'bold', marginBottom: 8 }}>
                  入库明细
                  <Button type="link" icon={<PlusOutlined />} onClick={() => add({ goodsId: 0, qty: 1, price: 0 })}>添加</Button>
                </div>
                {fields.map((field) => (
                  <Space key={field.key} style={{ marginBottom: 8 }}>
                    <Form.Item {...field} name={[field.name, 'goodsId']} rules={[{ required: true }]} style={{ width: 140 }}>
                      <InputNumber style={{ width: '100%' }} placeholder="商品ID" min={0} />
                    </Form.Item>
                    <Form.Item {...field} name={[field.name, 'skuId']} style={{ width: 140 }}>
                      <InputNumber style={{ width: '100%' }} placeholder="SKU ID" min={0} />
                    </Form.Item>
                    <Form.Item {...field} name={[field.name, 'qty']} rules={[{ required: true }]} style={{ width: 120 }}>
                      <InputNumber style={{ width: '100%' }} placeholder="数量" min={1} />
                    </Form.Item>
                    <Form.Item {...field} name={[field.name, 'price']} style={{ width: 140 }}>
                      <InputNumber style={{ width: '100%' }} placeholder="单价" min={0} step={0.01} />
                    </Form.Item>
                    <Button danger onClick={() => remove(field.name)}>删除</Button>
                  </Space>
                ))}
              </>
            )}
          </Form.List>
        </Form>
      </Modal>
    </Card>
  );
}
