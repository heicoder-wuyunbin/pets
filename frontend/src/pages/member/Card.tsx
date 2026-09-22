import { useEffect, useState } from 'react';
import {
  Card, Table, Tag, Button, Space, Modal, Form, Input, InputNumber,
  Select, message,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import {
  listCardType, saveCardType, rechargeCard, listMemberCard, listAccountLogs,
} from '@/api/member';
import type { CardTypeVO, CardTypeSaveReq, MemberCardVO, AccountLogVO } from '@/types/m5';

const LOG_TYPE_MAP: Record<number, { text: string; color: string }> = {
  1: { text: '充值', color: 'green' },
  2: { text: '消费', color: 'orange' },
  3: { text: '退款', color: 'blue' },
  4: { text: '积分得', color: 'purple' },
  5: { text: '积分抵', color: 'cyan' },
};

export default function MemberCardPage() {
  const [cardTypes, setCardTypes] = useState<CardTypeVO[]>([]);
  const [memberCards, setMemberCards] = useState<MemberCardVO[]>([]);
  const [logs, setLogs] = useState<AccountLogVO[]>([]);
  const [tab, setTab] = useState<'type' | 'card' | 'log'>('type');
  const [memberId, setMemberId] = useState<number>(0);
  const [typeModalOpen, setTypeModalOpen] = useState(false);
  const [rechargeModalOpen, setRechargeModalOpen] = useState(false);
  const [typeForm] = Form.useForm<CardTypeSaveReq>();
  const [rechargeForm] = Form.useForm();

  const loadCardTypes = () => listCardType().then(r => setCardTypes(r.data));

  useEffect(() => { loadCardTypes(); }, []);

  const loadMemberCards = (mid: number) => {
    if (!mid) return;
    listMemberCard(mid).then(r => setMemberCards(r.data));
  };

  const loadLogs = (mid: number, type?: number) => {
    if (!mid) return;
    listAccountLogs(mid, type).then(r => setLogs(r.data));
  };

  const handleTypeSave = async () => {
    const values = await typeForm.validateFields();
    await saveCardType(values);
    message.success('卡种已保存');
    setTypeModalOpen(false);
    typeForm.resetFields();
    loadCardTypes();
  };

  const handleRecharge = async () => {
    const values = await rechargeForm.validateFields();
    await rechargeCard({ memberId: values.memberId, cardTypeId: values.cardTypeId, amount: values.amount });
    message.success('充值成功');
    setRechargeModalOpen(false);
    rechargeForm.resetFields();
    if (values.memberId) {
      loadMemberCards(values.memberId);
      loadLogs(values.memberId);
    }
  };

  return (
    <Card>
      <Space wrap style={{ marginBottom: 16 }}>
        <Button type={tab === 'type' ? 'primary' : 'default'} onClick={() => setTab('type')}>卡种管理</Button>
        <Button type={tab === 'card' ? 'primary' : 'default'} onClick={() => setTab('card')}>会员充值</Button>
        <Button type={tab === 'log' ? 'primary' : 'default'} onClick={() => setTab('log')}>账户流水</Button>
      </Space>

      {tab === 'type' && (
        <>
          <div style={{ marginBottom: 16 }}>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => { typeForm.resetFields(); setTypeModalOpen(true); }}>新增卡种</Button>
          </div>
          <Table
            rowKey="id"
            dataSource={cardTypes}
            pagination={false}
            columns={[
              { title: '卡名', dataIndex: 'name' },
              { title: '面额', dataIndex: 'faceValue', render: (v: number) => `¥${v?.toFixed(2)}` },
              { title: '折扣', dataIndex: 'discount', render: (v: number) => `${(v * 10).toFixed(1)}折` },
              { title: '有效期(天)', dataIndex: 'validDays', render: (v: number) => v === 0 ? '永久' : v },
              { title: '状态', dataIndex: 'status', render: (s: number) => <Tag color={s === 1 ? 'green' : 'default'}>{s === 1 ? '启用' : '停用'}</Tag> },
              { title: '创建时间', dataIndex: 'createTime', width: 170 },
            ]}
          />
        </>
      )}

      {tab === 'card' && (
        <>
          <Space style={{ marginBottom: 16 }}>
            <InputNumber placeholder="会员ID" value={memberId || undefined} onChange={v => setMemberId(v || 0)} style={{ width: 160 }} />
            <Button onClick={() => loadMemberCards(memberId)}>查询</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => { rechargeForm.resetFields(); setRechargeModalOpen(true); }}>充值</Button>
          </Space>
          <Table
            rowKey="id"
            dataSource={memberCards}
            pagination={false}
            columns={[
              { title: '卡种', dataIndex: 'cardTypeName', render: (v: string) => v || '-' },
              { title: '余额', dataIndex: 'balance', render: (v: number) => `¥${v?.toFixed(2)}` },
              { title: '到期时间', dataIndex: 'expireTime', render: (v: string) => v || '永久' },
              { title: '开卡时间', dataIndex: 'createTime' },
            ]}
          />
        </>
      )}

      {tab === 'log' && (
        <>
          <Space style={{ marginBottom: 16 }}>
            <InputNumber placeholder="会员ID" value={memberId || undefined} onChange={v => setMemberId(v || 0)} style={{ width: 160 }} />
            <Select placeholder="类型" allowClear style={{ width: 120 }} onChange={(v) => loadLogs(memberId, v)}
              options={Object.entries(LOG_TYPE_MAP).map(([k, v]) => ({ value: Number(k), label: v.text }))} />
            <Button onClick={() => loadLogs(memberId)}>查询</Button>
          </Space>
          <Table
            rowKey="id"
            dataSource={logs}
            pagination={false}
            columns={[
              { title: '类型', dataIndex: 'type', width: 90, render: (t: number) => <Tag color={LOG_TYPE_MAP[t]?.color}>{LOG_TYPE_MAP[t]?.text}</Tag> },
              { title: '金额变动', dataIndex: 'amount', render: (v: number) => `${v > 0 ? '+' : ''}¥${v?.toFixed(2)}` },
              { title: '变动后余额', dataIndex: 'balanceAfter', render: (v: number) => `¥${v?.toFixed(2)}` },
              { title: '积分变动', dataIndex: 'points', render: (v: number) => v !== 0 ? (v > 0 ? `+${v}` : v) : '-' },
              { title: '变动后积分', dataIndex: 'pointsAfter' },
              { title: '关联订单', dataIndex: 'orderId', render: (v: number) => v || '-' },
              { title: '备注', dataIndex: 'remark' },
              { title: '时间', dataIndex: 'createTime', width: 170 },
            ]}
          />
        </>
      )}

      <Modal title="新增卡种" open={typeModalOpen} onCancel={() => setTypeModalOpen(false)} onOk={handleTypeSave}>
        <Form form={typeForm} layout="vertical" initialValues={{ discount: 1, validDays: 0, status: 1 }}>
          <Form.Item name="name" label="卡名" rules={[{ required: true }]}><Input /></Form.Item>
          <Space wrap>
            <Form.Item name="faceValue" label="面额" style={{ width: 160 }}>
              <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
            </Form.Item>
            <Form.Item name="discount" label="折扣" style={{ width: 160 }}>
              <InputNumber style={{ width: '100%' }} min={0} max={1} step={0.01} />
            </Form.Item>
            <Form.Item name="validDays" label="有效期(天)" style={{ width: 160 }}>
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
          </Space>
          <Form.Item name="status" label="状态">
            <Select options={[{ value: 1, label: '启用' }, { value: 0, label: '停用' }]} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="会员充值" open={rechargeModalOpen} onCancel={() => setRechargeModalOpen(false)} onOk={handleRecharge}>
        <Form form={rechargeForm} layout="vertical">
          <Form.Item name="memberId" label="会员ID" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={1} />
          </Form.Item>
          <Form.Item name="cardTypeId" label="卡种">
            <Select options={cardTypes.map(c => ({ value: c.id, label: c.name }))} />
          </Form.Item>
          <Form.Item name="amount" label="充值金额" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} min={0.01} step={0.01} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
