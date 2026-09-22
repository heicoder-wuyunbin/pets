import { useEffect, useState } from 'react';
import {
  Card, Table, Tag, Input, Select, Button, Space, Modal, Form, InputNumber,
  message,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import {
  listGoodsCategory, pageGoods, saveGoods, setGoodsStatus,
} from '@/api/goods';
import type {
  CategoryVO, GoodsVO, GoodsPageReq, GoodsSaveReq,
} from '@/types/m4';

export default function Goods() {
  const [data, setData] = useState<GoodsVO[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [params, setParams] = useState<GoodsPageReq>({ pageNum: 1, pageSize: 20 });
  const [categories, setCategories] = useState<CategoryVO[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editRecord, setEditRecord] = useState<GoodsVO | null>(null);
  const [form] = Form.useForm<GoodsSaveReq>();

  const loadData = () => {
    setLoading(true);
    pageGoods(params).then((res) => {
      setData(res.data);
      setTotal((res.page as any)?.total || 0);
    }).finally(() => setLoading(false));
  };

  const loadCategories = () => {
    listGoodsCategory().then((res) => setCategories(res.data));
  };

  useEffect(() => { loadData(); }, [params]);
  useEffect(() => { loadCategories(); }, []);

  const openAdd = () => {
    setEditRecord(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (r: GoodsVO) => {
    setEditRecord(r);
    form.setFieldsValue({
      id: r.id,
      categoryId: r.categoryId,
      name: r.name,
      barcode: r.barcode,
      unit: r.unit,
      retailPrice: r.retailPrice,
      costPrice: r.costPrice,
      status: r.status,
      skus: r.skus?.map(s => ({ specName: s.specName, price: s.price })),
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    await saveGoods(values);
    message.success('保存成功');
    setModalOpen(false);
    loadData();
  };

  const toggleStatus = async (r: GoodsVO) => {
    await setGoodsStatus(r.id, r.status === 1 ? 0 : 1);
    message.success('操作成功');
    loadData();
  };

  return (
    <Card>
      <Space wrap style={{ marginBottom: 16 }}>
        <Select
          placeholder="商品分类"
          allowClear
          style={{ width: 140 }}
          options={categories.map(c => ({ value: c.id, label: c.name }))}
          onChange={(v) => setParams({ ...params, pageNum: 1, categoryId: v })}
        />
        <Select
          placeholder="状态"
          allowClear
          style={{ width: 100 }}
          onChange={(v) => setParams({ ...params, pageNum: 1, status: v })}
          options={[{ value: 1, label: '上架' }, { value: 0, label: '下架' }]}
        />
        <Input.Search
          placeholder="商品名/条码"
          allowClear
          style={{ width: 220 }}
          onSearch={(v) => setParams({ ...params, pageNum: 1, keyword: v })}
        />
        <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>新增商品</Button>
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
          { title: '商品名称', dataIndex: 'name' },
          { title: '条码', dataIndex: 'barcode', width: 140 },
          { title: '单位', dataIndex: 'unit', width: 70 },
          { title: '零售价', dataIndex: 'retailPrice', width: 100, render: (v: number) => `¥${v?.toFixed(2)}` },
          { title: '成本价', dataIndex: 'costPrice', width: 100, render: (v: number) => `¥${v?.toFixed(2)}` },
          { title: 'SKU数', dataIndex: 'skus', width: 80, render: (s: any[]) => s?.length || 0 },
          {
            title: '状态', dataIndex: 'status', width: 80,
            render: (s: number) => <Tag color={s === 1 ? 'green' : 'default'}>{s === 1 ? '上架' : '下架'}</Tag>,
          },
          { title: '创建时间', dataIndex: 'createTime', width: 170 },
          {
            title: '操作', width: 180,
            render: (_, r) => (
              <Space>
                <a onClick={() => openEdit(r)}>编辑</a>
                <a onClick={() => toggleStatus(r)}>{r.status === 1 ? '下架' : '上架'}</a>
              </Space>
            ),
          },
        ]}
      />

      <Modal
        title={editRecord ? '编辑商品' : '新增商品'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        width={720}
      >
        <Form form={form} layout="vertical" initialValues={{ status: 1, skus: [] }}>
          <Form.Item name="id" hidden><Input /></Form.Item>
          <Space wrap>
            <Form.Item name="categoryId" label="商品分类" style={{ width: 200 }}>
              <Select options={categories.map(c => ({ value: c.id, label: c.name }))} />
            </Form.Item>
            <Form.Item name="name" label="商品名称" rules={[{ required: true }]} style={{ width: 240 }}>
              <Input />
            </Form.Item>
          </Space>
          <Space wrap>
            <Form.Item name="barcode" label="条码" style={{ width: 180 }}><Input /></Form.Item>
            <Form.Item name="unit" label="单位" style={{ width: 120 }}><Input placeholder="如：袋" /></Form.Item>
          </Space>
          <Space wrap>
            <Form.Item name="retailPrice" label="零售价" style={{ width: 160 }}>
              <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
            </Form.Item>
            <Form.Item name="costPrice" label="成本价" style={{ width: 160 }}>
              <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
            </Form.Item>
            <Form.Item name="status" label="状态" style={{ width: 120 }}>
              <Select options={[{ value: 1, label: '上架' }, { value: 0, label: '下架' }]} />
            </Form.Item>
          </Space>
          <Form.List name="skus">
            {(fields, { add, remove }) => (
              <>
                <div style={{ marginBottom: 8, fontWeight: 'bold' }}>
                  SKU 规格
                  <Button type="link" icon={<PlusOutlined />} onClick={() => add({ specName: '', price: 0 })}>添加</Button>
                </div>
                {fields.map((field) => (
                  <Space key={field.key} style={{ marginBottom: 8 }}>
                    <Form.Item {...field} name={[field.name, 'specName']} style={{ width: 200 }}>
                      <Input placeholder="规格名" />
                    </Form.Item>
                    <Form.Item {...field} name={[field.name, 'price']} style={{ width: 160 }}>
                      <InputNumber style={{ width: '100%' }} placeholder="价格" min={0} step={0.01} />
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
