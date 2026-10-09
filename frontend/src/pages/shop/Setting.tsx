import { useEffect, useState } from 'react';
import { Form, Input, Button, Descriptions, message, Spin } from 'antd';
import { PageContainer, ProCard } from '@ant-design/pro-components';
import { getShopDetail, updateShop } from '@/api/shop';
import type { ShopDetailVO } from '@/types/api';

export default function ShopSetting() {
  const [form] = Form.useForm();
  const [detail, setDetail] = useState<ShopDetailVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    getShopDetail()
      .then((res) => {
        setDetail(res.data);
        form.setFieldsValue(res.data);
      })
      .finally(() => setLoading(false));
  }, [form]);

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      await updateShop(values);
      message.success('保存成功');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <PageContainer title="门店设置"><Spin style={{ display: 'block', margin: '100px auto' }} /></PageContainer>;

  return (
    <PageContainer title="门店设置">
      <ProCard>
        {detail && (
          <Descriptions column={1} style={{ marginBottom: 24 }}>
            <Descriptions.Item label="店铺ID">{detail.id}</Descriptions.Item>
            <Descriptions.Item label="版本等级">V{detail.level}</Descriptions.Item>
            <Descriptions.Item label="到期时间">{detail.expireTime}</Descriptions.Item>
            <Descriptions.Item label="短信余量">{detail.smsRemain} 条</Descriptions.Item>
            <Descriptions.Item label="绑定号码">{detail.bindPhone}</Descriptions.Item>
          </Descriptions>
        )}
        <Form form={form} layout="vertical" style={{ maxWidth: 600 }}>
          <Form.Item name="shopName" label="店铺名称" rules={[{ required: true, message: '请输入店铺名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="logo" label="店铺Logo">
            <Input placeholder="Logo URL" />
          </Form.Item>
          <Form.Item name="addr" label="店铺地址">
            <Input />
          </Form.Item>
          <Form.Item name="telephone" label="联系电话">
            <Input />
          </Form.Item>
          <Form.Item>
            <Button type="primary" onClick={handleSave} loading={saving}>保存</Button>
          </Form.Item>
        </Form>
      </ProCard>
    </PageContainer>
  );
}
