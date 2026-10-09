import { useEffect, useRef, useState } from 'react';
import {
  Card, Input, Button, Space, Modal, Form, InputNumber, DatePicker, Select,
  Drawer, Descriptions, message, Popconfirm, Table,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import {
  pageMember, getMemberDetail, saveMember, deleteMember,
} from '@/api/member';
import type { MemberListVO, MemberDetailVO, MemberSaveReq } from '@/types/m5';

const GENDER_MAP: Record<number, string> = { 0: '未知', 1: '男', 2: '女' };

/** antd DatePicker 返回 dayjs 对象，调用其 format 方法 */
const fmtDate = (v: any): string | undefined => (v ? v.format('YYYY-MM-DD') : undefined);

export default function MemberList() {
  const actionRef = useRef<ActionType>();
  const [keyword, setKeyword] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [editRecord, setEditRecord] = useState<MemberDetailVO | null>(null);
  const [detail, setDetail] = useState<MemberDetailVO | null>(null);
  const [detailVisible, setDetailVisible] = useState(false);
  const [form] = Form.useForm<MemberSaveReq>();

  useEffect(() => {
    actionRef.current?.reload();
  }, [keyword]);

  const openAdd = () => {
    setEditRecord(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = async (r: MemberListVO) => {
    const res = await getMemberDetail(r.id);
    setEditRecord(res.data);
    form.setFieldsValue({
      id: res.data.id,
      mobile: res.data.mobile,
      name: res.data.name,
      gender: res.data.gender,
      birthday: res.data.birthday,
      tags: res.data.tags,
      remark: res.data.remark,
      pets: res.data.pets?.map(p => ({ ...p })),
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload: MemberSaveReq = {
      ...values,
      birthday: fmtDate(values.birthday),
      pets: values.pets?.map(p => ({
        ...p,
        birthday: fmtDate((p as any).birthday),
      })),
    };
    await saveMember(payload);
    message.success('保存成功');
    setModalOpen(false);
    actionRef.current?.reload();
  };

  const handleDelete = async (id: number) => {
    await deleteMember(id);
    message.success('已删除');
    actionRef.current?.reload();
  };

  const showDetail = async (id: number) => {
    const res = await getMemberDetail(id);
    setDetail(res.data);
    setDetailVisible(true);
  };

  const columns: ProColumns<MemberListVO>[] = [
    { title: '姓名', dataIndex: 'name', width: 100 },
    { title: '手机号', dataIndex: 'mobile', width: 130 },
    { title: '性别', dataIndex: 'gender', width: 70, render: (_, r: MemberListVO) => GENDER_MAP[r.gender] },
    { title: '等级', dataIndex: 'memberLevel', width: 70, render: (_, r: MemberListVO) => `Lv.${r.memberLevel}` },
    { title: '标签', dataIndex: 'tags', width: 120, ellipsis: true },
    { title: '余额', dataIndex: 'balance', width: 100, render: (_, r: MemberListVO) => `¥${r.balance?.toFixed(2)}` },
    { title: '积分', dataIndex: 'points', width: 80 },
    { title: '累计消费', dataIndex: 'totalConsume', width: 110, render: (_, r: MemberListVO) => `¥${r.totalConsume?.toFixed(2)}` },
    { title: '创建时间', dataIndex: 'createTime', width: 170, search: false },
    {
      title: '操作', width: 180, key: 'action', search: false,
      render: (_, r: MemberListVO) => (
        <Space>
          <a onClick={() => showDetail(r.id)}>详情</a>
          <a onClick={() => openEdit(r)}>编辑</a>
          <Popconfirm title="确认删除该会员？" onConfirm={() => handleDelete(r.id)}>
            <a style={{ color: 'red' }}>删除</a>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer title="客户管理">
      <ProTable<MemberListVO>
        rowKey="id"
        actionRef={actionRef}
        columns={columns}
        search={false}
        options={{ reload: true, density: true, setting: true }}
        toolbar={{
          title: '会员列表',
          actions: [
            <Input.Search
              key="keyword"
              placeholder="姓名/手机号"
              allowClear
              style={{ width: 220 }}
              onSearch={(v) => setKeyword(v)}
            />,
            <Button key="add" type="primary" icon={<PlusOutlined />} onClick={openAdd}>新增会员</Button>,
          ],
        }}
        request={async (params) => {
          const res = await pageMember({
            pageNum: params.current || 1,
            pageSize: params.pageSize || 20,
            keyword,
          });
          return {
            data: res.data || [],
            success: true,
            total: (res as any).page?.total || 0,
          };
        }}
      />

      <Modal
        title={editRecord ? '编辑会员' : '新增会员'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        width={720}
      >
        <Form form={form} layout="vertical" initialValues={{ gender: 0, pets: [] }}>
          <Form.Item name="id" hidden><Input /></Form.Item>
          <Space wrap>
            <Form.Item name="name" label="姓名" rules={[{ required: true }]} style={{ width: 200 }}>
              <Input />
            </Form.Item>
            <Form.Item name="mobile" label="手机号" rules={[{ required: true }]} style={{ width: 200 }}>
              <Input />
            </Form.Item>
            <Form.Item name="gender" label="性别" style={{ width: 120 }}>
              <Select options={[
                { value: 0, label: '未知' },
                { value: 1, label: '男' },
                { value: 2, label: '女' },
              ]} />
            </Form.Item>
          </Space>
          <Space wrap>
            <Form.Item name="birthday" label="生日" style={{ width: 200 }}>
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="tags" label="标签" style={{ width: 300 }}>
              <Input placeholder="多个标签用逗号分隔" />
            </Form.Item>
          </Space>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} />
          </Form.Item>

          <Form.List name="pets">
            {(fields, { add, remove }) => (
              <>
                <div style={{ fontWeight: 'bold', marginBottom: 8 }}>
                  宠物档案
                  <Button type="link" icon={<PlusOutlined />} onClick={() => add({ petName: '' })}>添加</Button>
                </div>
                {fields.map((field) => (
                  <Card key={field.key} size="small" style={{ marginBottom: 8 }}>
                    <Space wrap>
                      <Form.Item {...field} name={[field.name, 'petName']} label="宠物名" rules={[{ required: true }]} style={{ width: 140 }}>
                        <Input />
                      </Form.Item>
                      <Form.Item {...field} name={[field.name, 'petType']} label="类型" style={{ width: 100 }}>
                        <Input placeholder="猫/狗" />
                      </Form.Item>
                      <Form.Item {...field} name={[field.name, 'breed']} label="品种" style={{ width: 120 }}>
                        <Input />
                      </Form.Item>
                      <Form.Item {...field} name={[field.name, 'gender']} label="性别" style={{ width: 100 }}>
                        <Select options={[
                          { value: 0, label: '未知' },
                          { value: 1, label: '公' },
                          { value: 2, label: '母' },
                        ]} />
                      </Form.Item>
                    </Space>
                    <Space wrap>
                      <Form.Item {...field} name={[field.name, 'weight']} label="体重(kg)" style={{ width: 120 }}>
                        <InputNumber style={{ width: '100%' }} min={0} step={0.1} />
                      </Form.Item>
                      <Form.Item {...field} name={[field.name, 'birthday']} label="生日" style={{ width: 160 }}>
                        <DatePicker style={{ width: '100%' }} />
                      </Form.Item>
                      <Form.Item {...field} name={[field.name, 'vaccine']} label="疫苗" style={{ width: 200 }}>
                        <Input />
                      </Form.Item>
                      <Button danger onClick={() => remove(field.name)}>删除</Button>
                    </Space>
                  </Card>
                ))}
              </>
            )}
          </Form.List>
        </Form>
      </Modal>

      <Drawer title="会员详情" open={detailVisible} onClose={() => setDetailVisible(false)} width={560}>
        {detail && (
          <>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="姓名">{detail.name}</Descriptions.Item>
              <Descriptions.Item label="手机号">{detail.mobile}</Descriptions.Item>
              <Descriptions.Item label="性别">{GENDER_MAP[detail.gender]}</Descriptions.Item>
              <Descriptions.Item label="等级">Lv.{detail.memberLevel}</Descriptions.Item>
              <Descriptions.Item label="生日">{detail.birthday || '-'}</Descriptions.Item>
              <Descriptions.Item label="标签">{detail.tags || '-'}</Descriptions.Item>
              <Descriptions.Item label="余额">¥{detail.balance?.toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="积分">{detail.points}</Descriptions.Item>
              <Descriptions.Item label="累计消费">¥{detail.totalConsume?.toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="累计充值">¥{detail.totalRecharge?.toFixed(2)}</Descriptions.Item>
              {detail.remark && <Descriptions.Item label="备注" span={2}>{detail.remark}</Descriptions.Item>}
            </Descriptions>

            <h4 style={{ marginTop: 16 }}>宠物档案</h4>
            <Table
              rowKey="id"
              dataSource={detail.pets}
              pagination={false}
              size="small"
              columns={[
                { title: '宠物名', dataIndex: 'petName' },
                { title: '类型', dataIndex: 'petType' },
                { title: '品种', dataIndex: 'breed' },
                { title: '性别', dataIndex: 'gender', render: (g: number) => GENDER_MAP[g] },
                { title: '体重', dataIndex: 'weight', render: (v: number) => v ? `${v}kg` : '-' },
                { title: '生日', dataIndex: 'birthday' },
                { title: '疫苗', dataIndex: 'vaccine' },
              ]}
            />
          </>
        )}
      </Drawer>
    </PageContainer>
  );
}
