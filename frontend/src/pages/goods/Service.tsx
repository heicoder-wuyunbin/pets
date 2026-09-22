import { useEffect, useState } from 'react';
import {
  Card, Table, Tag, Input, Select, Button, Space, Modal, Form, InputNumber,
  message,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import {
  listServiceCategory, saveServiceCategory, pageService, saveService, setServiceStatus,
} from '@/api/service';
import type { CategoryVO, ServiceVO, ServicePageReq, ServiceSaveReq } from '@/types/m4';

export default function Service() {
  const [data, setData] = useState<ServiceVO[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [params, setParams] = useState<ServicePageReq>({ pageNum: 1, pageSize: 20 });
  const [categories, setCategories] = useState<CategoryVO[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [catModalOpen, setCatModalOpen] = useState(false);
  const [editRecord, setEditRecord] = useState<ServiceVO | null>(null);
  const [form] = Form.useForm<ServiceSaveReq>();
  const [catForm] = Form.useForm<{ name: string; sort?: number }>();

  const loadData = () => {
    setLoading(true);
    pageService(params).then((res) => {
      setData(res.data);
      setTotal((res.page as any)?.total || 0);
    }).finally(() => setLoading(false));
  };

  const loadCategories = () => {
    listServiceCategory().then((res) => setCategories(res.data));
  };

  useEffect(() => { loadData(); }, [params]);
  useEffect(() => { loadCategories(); }, []);

  const openAdd = () => {
    setEditRecord(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (r: ServiceVO) => {
    setEditRecord(r);
    form.setFieldsValue({
      id: r.id, categoryId: r.categoryId, name: r.name, price: r.price,
      duration: r.duration, description: r.description, canCard: r.canCard, status: r.status,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    await saveService(values);
    message.success('保存成功');
    setModalOpen(false);
    loadData();
  };

  const toggleStatus = async (r: ServiceVO) => {
    await setServiceStatus(r.id, r.status === 1 ? 0 : 1);
    message.success('操作成功');
    loadData();
  };

  const handleCatSave = async () => {
    const values = await catForm.validateFields();
    await saveServiceCategory(values);
    message.success('分类已保存');
    setCatModalOpen(false);
    catForm.resetFields();
    loadCategories();
  };

  return (
    <Card>
      <Space wrap style={{ marginBottom: 16 }}>
        <Select
          placeholder="服务分类"
          allowClear
          style={{ width: 140 }}
          options={categories.map(c => ({ value: c.id, label: c.name }))}
          onChange={(v) => setParams({ ...params, pageNum: 1, categoryId: v })}
        />
        <Button onClick={() => { catForm.resetFields(); setCatModalOpen(true); }}>分类管理</Button>
        <Select
          placeholder="状态"
          allowClear
          style={{ width: 100 }}
          onChange={(v) => setParams({ ...params, pageNum: 1, status: v })}
          options={[{ value: 1, label: '上架' }, { value: 0, label: '下架' }]}
        />
        <Input.Search
          placeholder="服务名称"
          allowClear
          style={{ width: 200 }}
          onSearch={(v) => setParams({ ...params, pageNum: 1, keyword: v })}
        />
        <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>新增服务</Button>
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
          { title: '服务名称', dataIndex: 'name' },
          { title: '价格', dataIndex: 'price', width: 100, render: (v: number) => `¥${v?.toFixed(2)}` },
          { title: '时长(分)', dataIndex: 'duration', width: 90 },
          { title: '可用次卡', dataIndex: 'canCard', width: 90, render: (v: number) => v === 1 ? '是' : '否' },
          {
            title: '状态', dataIndex: 'status', width: 80,
            render: (s: number) => <Tag color={s === 1 ? 'green' : 'default'}>{s === 1 ? '上架' : '下架'}</Tag>,
          },
          { title: '创建时间', dataIndex: 'createTime', width: 170 },
          {
            title: '操作', width: 160,
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
        title={editRecord ? '编辑服务' : '新增服务'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        width={600}
      >
        <Form form={form} layout="vertical" initialValues={{ status: 1, canCard: 0 }}>
          <Form.Item name="id" hidden><Input /></Form.Item>
          <Space wrap>
            <Form.Item name="categoryId" label="服务分类" style={{ width: 200 }}>
              <Select options={categories.map(c => ({ value: c.id, label: c.name }))} />
            </Form.Item>
            <Form.Item name="name" label="服务名称" rules={[{ required: true }]} style={{ width: 240 }}>
              <Input />
            </Form.Item>
          </Space>
          <Space wrap>
            <Form.Item name="price" label="价格" style={{ width: 160 }}>
              <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
            </Form.Item>
            <Form.Item name="duration" label="时长(分钟)" style={{ width: 160 }}>
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
            <Form.Item name="canCard" label="可用次卡" style={{ width: 120 }}>
              <Select options={[{ value: 0, label: '否' }, { value: 1, label: '是' }]} />
            </Form.Item>
          </Space>
          <Form.Item name="status" label="状态">
            <Select options={[{ value: 1, label: '上架' }, { value: 0, label: '下架' }]} />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="新增服务分类"
        open={catModalOpen}
        onCancel={() => setCatModalOpen(false)}
        onOk={handleCatSave}
      >
        <Form form={catForm} layout="vertical">
          <Form.Item name="name" label="分类名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="sort" label="排序">
            <InputNumber min={0} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
