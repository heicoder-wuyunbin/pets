import { useEffect, useState } from 'react';
import {
  Card, Table, Input, Button, Space, Modal, Form, InputNumber, Select, message,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { pageStockOut, createStockOut } from '@/api/stock';
import type { StockOutVO, StockOutSaveReq } from '@/types/m4';

const TYPE_MAP: Record<number, string> = {
  1: '盘亏',
  2: '报损',
  3: '退货出库',
};

export default function StockOut() {
  const [data, setData] = useState<StockOutVO[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm<StockOutSaveReq>();

  const loadData = () => {
    setLoading(true);
    pageStockOut(pageNum, 20, keyword).then((res) => {
      setData(res.data);
      setTotal((res.page as any)?.total || 0);
    }).finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, [pageNum]);

  const openAdd = () => {
    form.resetFields();
    form.setFieldsValue({ type: 1, items: [{ goodsId: 0, qty: 1 }] });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const no = await createStockOut(values);
    message.success(`出库单创建成功：${no.data}`);
    setModalOpen(false);
    loadData();
  };

  return (
    <Card>
      <Space wrap style={{ marginBottom: 16 }}>
        <Input.Search
          placeholder="出库单号"
          allowClear
          style={{ width: 220 }}
          onSearch={(v) => { setKeyword(v); setPageNum(1); loadData(); }}
        />
        <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>新增出库</Button>
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
          { title: '出库单号', dataIndex: 'no', width: 180 },
          { title: '类型', dataIndex: 'type', width: 100, render: (t: number) => TYPE_MAP[t] || t },
          { title: '总数量', dataIndex: 'totalQty', width: 100 },
          { title: '备注', dataIndex: 'remark' },
          { title: '创建时间', dataIndex: 'createTime', width: 170 },
        ]}
      />

      <Modal
        title="新增出库单"
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        width={720}
      >
        <Form form={form} layout="vertical" initialValues={{ type: 1 }}>
          <Space wrap>
            <Form.Item name="type" label="出库类型" style={{ width: 160 }}>
              <Select options={[
                { value: 1, label: '盘亏' },
                { value: 2, label: '报损' },
                { value: 3, label: '退货出库' },
              ]} />
            </Form.Item>
            <Form.Item name="remark" label="备注" style={{ width: 360 }}><Input /></Form.Item>
          </Space>
          <Form.List name="items">
            {(fields, { add, remove }) => (
              <>
                <div style={{ fontWeight: 'bold', marginBottom: 8 }}>
                  出库明细
                  <Button type="link" icon={<PlusOutlined />} onClick={() => add({ goodsId: 0, qty: 1 })}>添加</Button>
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
