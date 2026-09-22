import { useEffect, useState } from 'react';
import { Card, Tabs, Form, Input, Button, message, Spin } from 'antd';
import { listSysParam, saveSysParam } from '@/api/sysParam';
import type { SysParamVO } from '@/types/api';

export default function SysParam() {
  const [groups, setGroups] = useState<Record<string, SysParamVO[]>>({});
  const [loading, setLoading] = useState(true);
  const [form] = Form.useForm();

  const loadData = () => {
    setLoading(true);
    listSysParam({})
      .then((res) => {
        const grouped: Record<string, SysParamVO[]> = {};
        res.data.forEach((p) => {
          if (!grouped[p.groupKey]) grouped[p.groupKey] = [];
          grouped[p.groupKey].push(p);
        });
        setGroups(grouped);
        const values: Record<string, string> = {};
        res.data.forEach((p) => { values[p.paramKey] = p.paramValue; });
        form.setFieldsValue(values);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, []);

  const handleSave = async () => {
    const values = await form.getFieldsValue();
    const items = Object.entries(values).map(([paramKey, paramValue]) => ({ paramKey, paramValue: String(paramValue ?? '') }));
    await saveSysParam({ items });
    message.success('保存成功');
    loadData();
  };

  if (loading) return <Spin style={{ display: 'block', margin: '100px auto' }} />;

  const tabItems = Object.entries(groups).map(([group, params]) => ({
    key: group,
    label: group,
    children: (
      <Form form={form} layout="vertical" style={{ maxWidth: 500 }}>
        {params.map((p) => (
          <Form.Item key={p.paramKey} name={p.paramKey} label={p.paramKey} extra={p.remark}>
            <Input />
          </Form.Item>
        ))}
      </Form>
    ),
  }));

  return (
    <Card title="系统参数" extra={<Button type="primary" onClick={handleSave}>保存</Button>}>
      <Tabs items={tabItems} />
    </Card>
  );
}
