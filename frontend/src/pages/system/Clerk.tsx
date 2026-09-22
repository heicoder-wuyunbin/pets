import { useEffect, useState } from 'react';
import { Card, Table, Tag, Input, Button, Space, Modal, Form, Input as AntInput, Select, message, Popconfirm } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { pageClerk, addClerk, editClerk, updateClerkStatus, resetClerkPwd } from '@/api/clerk';
import type { ClerkVO, ClerkPageReq, ClerkAddReq, ClerkEditReq, ClerkResetPwdReq } from '@/types/api';

export default function Clerk() {
  const [data, setData] = useState<ClerkVO[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [params, setParams] = useState<ClerkPageReq>({ pageNum: 1, pageSize: 20 });
  const [addVisible, setAddVisible] = useState(false);
  const [editVisible, setEditVisible] = useState(false);
  const [pwdVisible, setPwdVisible] = useState(false);
  const [current, setCurrent] = useState<ClerkVO | null>(null);
  const [addForm] = Form.useForm();
  const [editForm] = Form.useForm();
  const [pwdForm] = Form.useForm();

  const loadData = () => {
    setLoading(true);
    pageClerk(params)
      .then((res) => {
        setData(res.data);
        setTotal((res.page as any)?.total || 0);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, [params]);

  const handleAdd = async () => {
    const values: ClerkAddReq = await addForm.validateFields();
    await addClerk(values);
    message.success('新增成功');
    setAddVisible(false);
    addForm.resetFields();
    loadData();
  };

  const handleEdit = async () => {
    const values: ClerkEditReq = await editForm.validateFields();
    await editClerk({ ...values, id: current!.id });
    message.success('编辑成功');
    setEditVisible(false);
    loadData();
  };

  const handleStatus = async (record: ClerkVO, status: number) => {
    await updateClerkStatus({ id: record.id, status });
    message.success('状态已更新');
    loadData();
  };

  const handleResetPwd = async () => {
    const values = await pwdForm.validateFields();
    const req: ClerkResetPwdReq = { id: current!.id, newPassword: values.newPassword };
    await resetClerkPwd(req);
    message.success('密码已重置');
    setPwdVisible(false);
    pwdForm.resetFields();
  };

  return (
    <Card title="员工管理">
      <Space style={{ marginBottom: 16 }}>
        <Input.Search
          placeholder="姓名/手机号"
          allowClear
          onSearch={(v) => setParams({ ...params, pageNum: 1, keyword: v })}
          style={{ width: 200 }}
        />
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setAddVisible(true)}>新增员工</Button>
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
          { title: '姓名', dataIndex: 'truename' },
          { title: '手机号', dataIndex: 'phone' },
          { title: '角色', dataIndex: 'roleName' },
          { title: '状态', dataIndex: 'status', width: 100, render: (s: number) => s === 1 ? <Tag color="green">在职</Tag> : <Tag color="red">离职</Tag> },
          { title: '入职日期', dataIndex: 'hireDate', width: 120 },
          {
            title: '操作', width: 240, render: (_, r) => (
              <Space>
                <a onClick={() => { setCurrent(r); editForm.setFieldsValue({ truename: r.truename, roleId: r.roleId }); setEditVisible(true); }}>编辑</a>
                <Popconfirm title={`确认${r.status === 1 ? '禁用' : '启用'}该员工？`} onConfirm={() => handleStatus(r, r.status === 1 ? 0 : 1)}>
                  <a>{r.status === 1 ? '禁用' : '启用'}</a>
                </Popconfirm>
                <a onClick={() => { setCurrent(r); setPwdVisible(true); }}>重置密码</a>
              </Space>
            ),
          },
        ]}
      />

      {/* 新增 */}
      <Modal title="新增员工" open={addVisible} onOk={handleAdd} onCancel={() => setAddVisible(false)} destroyOnClose>
        <Form form={addForm} layout="vertical">
          <Form.Item name="phone" label="手机号" rules={[{ required: true, message: '请输入手机号' }]}>
            <AntInput />
          </Form.Item>
          <Form.Item name="truename" label="姓名" rules={[{ required: true, message: '请输入姓名' }]}>
            <AntInput />
          </Form.Item>
          <Form.Item name="roleId" label="角色" rules={[{ required: true, message: '请选择角色' }]}>
            <Select options={[{ value: 2, label: '店员' }]} />
          </Form.Item>
          <Form.Item name="password" label="初始密码" rules={[{ required: true, message: '请输入初始密码' }]}>
            <AntInput.Password />
          </Form.Item>
        </Form>
      </Modal>

      {/* 编辑 */}
      <Modal title="编辑员工" open={editVisible} onOk={handleEdit} onCancel={() => setEditVisible(false)} destroyOnClose>
        <Form form={editForm} layout="vertical">
          <Form.Item name="truename" label="姓名">
            <AntInput />
          </Form.Item>
          <Form.Item name="roleId" label="角色">
            <Select options={[{ value: 2, label: '店员' }]} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 重置密码 */}
      <Modal title="重置密码" open={pwdVisible} onOk={handleResetPwd} onCancel={() => setPwdVisible(false)} destroyOnClose>
        <Form form={pwdForm} layout="vertical">
          <Form.Item name="newPassword" label="新密码" rules={[{ required: true, message: '请输入新密码' }]}>
            <AntInput.Password />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
