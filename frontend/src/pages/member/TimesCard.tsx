import { useEffect, useState } from 'react';
import {
  Card, Table, Tag, Button, Space, Modal, Form, Input, InputNumber, Select, message,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import {
  listTimescardTemplate, saveTimescardTemplate, buyTimescard, listMemberTimescard,
} from '@/api/member';
import type { TimescardTemplateVO, MemberTimescardVO } from '@/types/m5';

export default function TimesCardPage() {
  const [templates, setTemplates] = useState<TimescardTemplateVO[]>([]);
  const [memberCards, setMemberCards] = useState<MemberTimescardVO[]>([]);
  const [tab, setTab] = useState<'template' | 'member'>('template');
  const [memberId, setMemberId] = useState<number>(0);
  const [tplModalOpen, setTplModalOpen] = useState(false);
  const [buyModalOpen, setBuyModalOpen] = useState(false);
  const [tplForm] = Form.useForm();
  const [buyForm] = Form.useForm();

  const loadTemplates = () => listTimescardTemplate().then(r => setTemplates(r.data));
  useEffect(() => { loadTemplates(); }, []);

  const loadMemberCards = (mid: number) => {
    if (!mid) return;
    listMemberTimescard(mid).then(r => setMemberCards(r.data));
  };

  const handleTplSave = async () => {
    const values = await tplForm.validateFields();
    await saveTimescardTemplate(values);
    message.success('模板已保存');
    setTplModalOpen(false);
    tplForm.resetFields();
    loadTemplates();
  };

  const handleBuy = async () => {
    const values = await buyForm.validateFields();
    await buyTimescard(values.memberId, values.templateId);
    message.success('购卡成功');
    setBuyModalOpen(false);
    buyForm.resetFields();
    loadMemberCards(values.memberId);
  };

  return (
    <Card>
      <Space wrap style={{ marginBottom: 16 }}>
        <Button type={tab === 'template' ? 'primary' : 'default'} onClick={() => setTab('template')}>次卡模板</Button>
        <Button type={tab === 'member' ? 'primary' : 'default'} onClick={() => setTab('member')}>会员次卡</Button>
      </Space>

      {tab === 'template' && (
        <>
          <div style={{ marginBottom: 16 }}>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => { tplForm.resetFields(); setTplModalOpen(true); }}>新增模板</Button>
          </div>
          <Table
            rowKey="id"
            dataSource={templates}
            pagination={false}
            columns={[
              { title: '模板名', dataIndex: 'name' },
              { title: '绑定服务ID', dataIndex: 'serviceId' },
              { title: '次数', dataIndex: 'times' },
              { title: '售价', dataIndex: 'price', render: (v: number) => `¥${v?.toFixed(2)}` },
              { title: '有效期(天)', dataIndex: 'validDays', render: (v: number) => v === 0 ? '永久' : v },
              { title: '状态', dataIndex: 'status', render: (s: number) => <Tag color={s === 1 ? 'green' : 'default'}>{s === 1 ? '启用' : '停用'}</Tag> },
              { title: '创建时间', dataIndex: 'createTime', width: 170 },
            ]}
          />
        </>
      )}

      {tab === 'member' && (
        <>
          <Space style={{ marginBottom: 16 }}>
            <InputNumber placeholder="会员ID" value={memberId || undefined} onChange={v => setMemberId(v || 0)} style={{ width: 160 }} />
            <Button onClick={() => loadMemberCards(memberId)}>查询</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => { buyForm.resetFields(); setBuyModalOpen(true); }}>购卡</Button>
          </Space>
          <Table
            rowKey="id"
            dataSource={memberCards}
            pagination={false}
            columns={[
              { title: '模板', dataIndex: 'templateName' },
              { title: '剩余次数', dataIndex: 'remainTimes' },
              { title: '到期时间', dataIndex: 'expireTime', render: (v: string) => v || '永久' },
              { title: '购卡时间', dataIndex: 'createTime' },
            ]}
          />
        </>
      )}

      <Modal title="新增次卡模板" open={tplModalOpen} onCancel={() => setTplModalOpen(false)} onOk={handleTplSave}>
        <Form form={tplForm} layout="vertical" initialValues={{ validDays: 0, status: 1 }}>
          <Form.Item name="name" label="模板名" rules={[{ required: true }]}><Input /></Form.Item>
          <Space wrap>
            <Form.Item name="serviceId" label="绑定服务ID" style={{ width: 160 }}>
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
            <Form.Item name="times" label="次数" rules={[{ required: true }]} style={{ width: 160 }}>
              <InputNumber style={{ width: '100%' }} min={1} />
            </Form.Item>
            <Form.Item name="price" label="售价" style={{ width: 160 }}>
              <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
            </Form.Item>
          </Space>
          <Space wrap>
            <Form.Item name="validDays" label="有效期(天)" style={{ width: 160 }}>
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
            <Form.Item name="status" label="状态" style={{ width: 160 }}>
              <Select options={[{ value: 1, label: '启用' }, { value: 0, label: '停用' }]} />
            </Form.Item>
          </Space>
        </Form>
      </Modal>

      <Modal title="会员购卡" open={buyModalOpen} onCancel={() => setBuyModalOpen(false)} onOk={handleBuy}>
        <Form form={buyForm} layout="vertical">
          <Form.Item name="memberId" label="会员ID" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={1} />
          </Form.Item>
          <Form.Item name="templateId" label="次卡模板" rules={[{ required: true }]}>
            <Select options={templates.map(t => ({ value: t.id, label: `${t.name}(${t.times}次)` }))} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
