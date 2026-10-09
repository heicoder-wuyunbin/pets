import { useEffect, useState } from 'react';
import {
  Button, Space, Modal, Form, Input, InputNumber, Select, DatePicker,
  Switch, message, Tag,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import {
  pageMarketing, saveMarketing, setMarketingStatus,
} from '@/api/marketing';
import type { MarketingVO, MarketingPageReq, MarketingSaveReq } from '@/types/m6';

const TYPE_MAP: Record<number, string> = { 1: '满减', 2: '折扣' };

const fmtDateTime = (v: any): string | undefined => (v ? v.format('YYYY-MM-DD HH:mm:ss') : undefined);

export default function MarketingList() {
  const [data, setData] = useState<MarketingVO[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [params, setParams] = useState<MarketingPageReq>({ pageNum: 1, pageSize: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [editRecord, setEditRecord] = useState<MarketingVO | null>(null);
  const [form] = Form.useForm<MarketingSaveReq & { startTimeRange?: any[] }>();

  const loadData = () => {
    setLoading(true);
    pageMarketing(params).then((res) => {
      setData(res.data);
      setTotal((res.page as any)?.total || 0);
    }).finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, [params]);

  const openAdd = () => {
    setEditRecord(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (r: MarketingVO) => {
    setEditRecord(r);
    form.setFieldsValue({
      id: r.id,
      name: r.name,
      type: r.type,
      threshold: r.threshold,
      discountValue: r.discountValue,
      goodsIds: r.goodsIds,
      status: r.status,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const [start, end] = values.startTimeRange || [];
    const payload: MarketingSaveReq = {
      id: values.id,
      name: values.name,
      type: values.type,
      threshold: values.threshold,
      discountValue: values.discountValue,
      goodsIds: values.goodsIds,
      startTime: fmtDateTime(start),
      endTime: fmtDateTime(end),
      status: values.status ?? 1,
    };
    await saveMarketing(payload);
    message.success('保存成功');
    setModalOpen(false);
    loadData();
  };

  const handleToggle = async (r: MarketingVO) => {
    await setMarketingStatus(r.id, r.status === 1 ? 0 : 1);
    message.success('操作成功');
    loadData();
  };

  return (
    <PageContainer title="营销管理">
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>新建活动</Button>
        <Select
          placeholder="状态筛选"
          allowClear
          style={{ width: 140 }}
          options={[
            { value: 1, label: '启用中' },
            { value: 0, label: '已停用' },
          ]}
          onChange={(v) => setParams({ ...params, pageNum: 1, status: v })}
        />
      </Space>

      <ProTable<MarketingVO>
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
          { title: '活动名称', dataIndex: 'name', width: 180 },
          {
            title: '类型', dataIndex: 'type', width: 80,
            render: (_, r: MarketingVO) => <Tag color={r.type === 1 ? 'blue' : 'purple'}>{TYPE_MAP[r.type]}</Tag>,
          },
          {
            title: '规则', width: 180,
            render: (_, r: MarketingVO) => {
              if (r.type === 1) return `满${r.threshold || 0}元减${r.discountValue || 0}元`;
              return `打${(r.discountValue * 10).toFixed(1)}折`;
            },
          },
          { title: '限定商品', dataIndex: 'goodsIds', width: 140, render: (_, r: MarketingVO) => r.goodsIds || '全场通用' },
          { title: '开始时间', dataIndex: 'startTime', width: 160 },
          { title: '结束时间', dataIndex: 'endTime', width: 160 },
          {
            title: '状态', dataIndex: 'status', width: 90,
            render: (_, r: MarketingVO) => (r.status === 1 ? <Tag color="green">启用</Tag> : <Tag>停用</Tag>),
          },
          {
            title: '操作', width: 160,
            render: (_, r) => (
              <Space>
                <a onClick={() => openEdit(r)}>编辑</a>
                <a onClick={() => handleToggle(r)}>{r.status === 1 ? '停用' : '启用'}</a>
              </Space>
            ),
          },
        ]}
      />

      <Modal
        title={editRecord ? '编辑活动' : '新建活动'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        width={640}
      >
        <Form form={form} layout="vertical" initialValues={{ type: 1, status: 1 }}>
          <Form.Item name="id" hidden><Input /></Form.Item>
          <Form.Item name="name" label="活动名称" rules={[{ required: true }]}>
            <Input placeholder="如：满100减20" />
          </Form.Item>
          <Space wrap>
            <Form.Item name="type" label="活动类型" rules={[{ required: true }]} style={{ width: 160 }}>
              <Select options={[
                { value: 1, label: '满减' },
                { value: 2, label: '折扣' },
              ]} />
            </Form.Item>
            <Form.Item
              noStyle
              shouldUpdate={(p, c) => p.type !== c.type}
            >
              {({ getFieldValue }) => {
                const type = getFieldValue('type');
                if (type === 1) {
                  return (
                    <>
                      <Form.Item name="threshold" label="满减门槛(元)" style={{ width: 160 }}>
                        <InputNumber style={{ width: '100%' }} min={0} />
                      </Form.Item>
                      <Form.Item name="discountValue" label="减免金额(元)" style={{ width: 160 }}>
                        <InputNumber style={{ width: '100%' }} min={0} />
                      </Form.Item>
                    </>
                  );
                }
                return (
                  <Form.Item name="discountValue" label="折扣率(0~1)" style={{ width: 160 }}>
                    <InputNumber style={{ width: '100%' }} min={0} max={1} step={0.1} placeholder="如0.8" />
                  </Form.Item>
                );
              }}
            </Form.Item>
          </Space>
          <Form.Item name="goodsIds" label="限定商品ID" tooltip="多个商品ID用逗号分隔，留空表示全场通用">
            <Input placeholder="如：1001,1002,1003" />
          </Form.Item>
          <Form.Item name="startTimeRange" label="活动时间">
            <DatePicker.RangePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="status" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="停用" />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
}
