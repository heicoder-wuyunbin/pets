import { useEffect, useState } from 'react';
import { Card, Form, InputNumber, Select, Button, message, Descriptions } from 'antd';
import { getPointsRule, savePointsRule } from '@/api/member';
import type { PointsRule } from '@/types/m5';

export default function PointsPage() {
  const [rule, setRule] = useState<PointsRule>({ ratio: 1, enable: 1, offset: 0 });
  const [form] = Form.useForm();

  const loadRule = () => {
    getPointsRule().then(r => {
      setRule(r.data);
      form.setFieldsValue(r.data);
    });
  };

  useEffect(() => { loadRule(); }, []);

  const handleSave = async () => {
    const values = await form.validateFields();
    await savePointsRule(values);
    message.success('积分规则已保存');
    loadRule();
  };

  return (
    <Card title="积分规则">
      <Descriptions column={1} bordered size="small" style={{ marginBottom: 16 }}>
        <Descriptions.Item label="当前比率">消费 1 元 = {rule.ratio} 积分</Descriptions.Item>
        <Descriptions.Item label="是否启用">{rule.enable === 1 ? '启用' : '停用'}</Descriptions.Item>
        <Descriptions.Item label="起计门槛">满 {rule.offset} 元开始计积分</Descriptions.Item>
      </Descriptions>

      <Form form={form} layout="vertical" style={{ maxWidth: 480 }}>
        <Form.Item name="ratio" label="积分比率（消费1元得多少积分）" rules={[{ required: true }]}>
          <InputNumber style={{ width: '100%' }} min={0} step={0.1} />
        </Form.Item>
        <Form.Item name="enable" label="是否启用" rules={[{ required: true }]}>
          <Select options={[{ value: 1, label: '启用' }, { value: 0, label: '停用' }]} />
        </Form.Item>
        <Form.Item name="offset" label="起计门槛（满多少元开始计积分）" rules={[{ required: true }]}>
          <InputNumber style={{ width: '100%' }} min={0} step={0.01} />
        </Form.Item>
        <Button type="primary" onClick={handleSave}>保存规则</Button>
      </Form>
    </Card>
  );
}
