import { useState } from 'react';
import {
  Card, Input, Button, Table, InputNumber, Select, Space, Statistic,
  Modal, Form, message, Tag, Divider,
} from 'antd';
import { DeleteOutlined, PlusOutlined, ShoppingCartOutlined } from '@ant-design/icons';
import { checkout } from '@/api/trade';
import type { CheckoutItem } from '@/types/api';

const PAY_TYPES = [
  { value: 'cash', label: '现金' },
  { value: 'wechat', label: '微信' },
  { value: 'alipay', label: '支付宝' },
  { value: 'card', label: '刷卡' },
];

interface CartItem extends CheckoutItem {
  key: number;
}

export default function Cashier() {
  const [cart, setCart] = useState<CartItem[]>([]);
  const [payType, setPayType] = useState('cash');
  const [discount, setDiscount] = useState(100);
  const [memberKeyword, setMemberKeyword] = useState('');
  const [memberName, setMemberName] = useState('');
  const [itemModalVisible, setItemModalVisible] = useState(false);
  const [itemForm] = Form.useForm();
  const [checkingOut, setCheckingOut] = useState(false);

  // 计算合计
  const total = cart.reduce(
    (sum, item) => sum + item.price * item.qty * (item.discount || 100) / 100, 0,
  );
  const payable = total * (discount / 100);

  const handleAddItem = () => {
    itemForm.resetFields();
    setItemModalVisible(true);
  };

  const handleConfirmAddItem = async () => {
    const values = await itemForm.validateFields();
    const newItem: CartItem = {
      key: Date.now(),
      itemType: values.itemType,
      itemId: values.itemId,
      itemName: values.itemName,
      price: values.price,
      qty: values.qty,
      discount: values.discount || 100,
    };
    setCart([...cart, newItem]);
    setItemModalVisible(false);
  };

  const handleRemoveItem = (key: number) => {
    setCart(cart.filter((i) => i.key !== key));
  };

  const handleQtyChange = (key: number, qty: number) => {
    setCart(cart.map((i) => (i.key === key ? { ...i, qty } : i)));
  };

  const handleDiscountChange = (key: number, d: number) => {
    setCart(cart.map((i) => (i.key === key ? { ...i, discount: d } : i)));
  };

  const handleCheckout = async () => {
    if (cart.length === 0) {
      message.warning('购物清单为空');
      return;
    }
    setCheckingOut(true);
    try {
      const res = await checkout({
        memberName: memberName || undefined,
        payType,
        discount,
        items: cart.map(({ key, ...rest }) => rest),
      });
      const r = res.data;
      const mktDiscount = r.marketingDiscount || 0;
      let msg = `下单成功！订单号：${r.orderNo}，实收 ¥${(r.actualReceived || 0).toFixed(2)}`;
      if (r.activityName && mktDiscount > 0) {
        msg += `（${r.activityName} 优惠 ¥${mktDiscount.toFixed(2)}）`;
      }
      message.success(msg, 5);
      setCart([]);
      setMemberName('');
      setDiscount(100);
    } finally {
      setCheckingOut(false);
    }
  };

  return (
    <div style={{ display: 'flex', gap: 16, height: 'calc(100vh - 110px)' }}>
      {/* 左侧：会员 + 商品录入 */}
      <div style={{ flex: '0 0 360px', display: 'flex', flexDirection: 'column', gap: 16 }}>
        <Card title="会员" size="small">
          <Space.Compact style={{ width: '100%' }}>
            <Input
              placeholder="会员姓名/手机号"
              value={memberKeyword}
              onChange={(e) => setMemberKeyword(e.target.value)}
              onPressEnter={() => setMemberName(memberKeyword)}
            />
            <Button onClick={() => setMemberName(memberKeyword)}>选择</Button>
          </Space.Compact>
          {memberName && (
            <div style={{ marginTop: 8 }}>
              <Tag color="blue">当前会员：{memberName}</Tag>
              <Button type="link" size="small" onClick={() => setMemberName('')}>清除</Button>
            </div>
          )}
        </Card>

        <Card
          title="商品/服务"
          size="small"
          extra={<Button type="primary" icon={<PlusOutlined />} size="small" onClick={handleAddItem}>添加</Button>}
        >
          <Input placeholder="扫码枪/手动输入条码后回车" style={{ marginBottom: 8 }} />
          <div style={{ color: '#999', fontSize: 12 }}>
            点击「添加」手动录入商品或服务
          </div>
        </Card>
      </div>

      {/* 右侧：购物清单 + 结算 */}
      <Card
        title={<Space><ShoppingCartOutlined /> 购物清单（共 {cart.length} 件）</Space>}
        style={{ flex: 1 }}
        bodyStyle={{ display: 'flex', flexDirection: 'column', height: '100%' }}
      >
        <div style={{ flex: 1, overflow: 'auto' }}>
          <Table
            rowKey="key"
            dataSource={cart}
            pagination={false}
            size="small"
            columns={[
              { title: '消费项目', dataIndex: 'itemName' },
              { title: '类型', dataIndex: 'itemType', width: 70, render: (t: number) => t === 1 ? '商品' : '服务' },
              { title: '零售价', dataIndex: 'price', width: 90, render: (v: number) => `¥${v}` },
              {
                title: '折扣率(%)', dataIndex: 'discount', width: 100,
                render: (v: number, r) => (
                  <InputNumber min={0} max={100} value={v} size="small" style={{ width: 70 }}
                    onChange={(val) => handleDiscountChange(r.key, val || 100)} />
                ),
              },
              {
                title: '数量', dataIndex: 'qty', width: 90,
                render: (v: number, r) => (
                  <InputNumber min={1} value={v} size="small" style={{ width: 60 }}
                    onChange={(val) => handleQtyChange(r.key, val || 1)} />
                ),
              },
              {
                title: '小计', width: 90,
                render: (_, r) => `¥${(r.price * r.qty * (r.discount || 100) / 100).toFixed(2)}`,
              },
              {
                title: '操作', width: 60,
                render: (_, r) => <Button type="text" danger icon={<DeleteOutlined />} onClick={() => handleRemoveItem(r.key)} />,
              },
            ]}
          />
        </div>

        <Divider style={{ margin: '12px 0' }} />

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Space>
            <span>整单折扣:</span>
            <InputNumber min={0} max={100} value={discount} onChange={(v) => setDiscount(v || 100)} style={{ width: 80 }} />
            <span>%</span>
            <Select value={payType} onChange={setPayType} options={PAY_TYPES} style={{ width: 100 }} />
          </Space>
          <Space size="large">
            <Statistic title="总价" value={total} precision={2} prefix="¥" />
            <Statistic title="应付金额" value={payable} precision={2} prefix="¥" valueStyle={{ color: '#cf1322' }} />
            <Button type="primary" size="large" loading={checkingOut} onClick={handleCheckout}>去结账</Button>
          </Space>
        </div>
      </Card>

      {/* 添加商品/服务弹窗 */}
      <Modal title="添加商品/服务" open={itemModalVisible} onOk={handleConfirmAddItem} onCancel={() => setItemModalVisible(false)} destroyOnClose>
        <Form form={itemForm} layout="vertical">
          <Form.Item name="itemType" label="类型" rules={[{ required: true }]}>
            <Select options={[{ value: 1, label: '商品' }, { value: 2, label: '服务' }]} />
          </Form.Item>
          <Form.Item name="itemId" label="商品/服务ID" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="itemName" label="名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="price" label="单价" rules={[{ required: true }]}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="qty" label="数量" rules={[{ required: true }]} initialValue={1}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="discount" label="折扣率(%)" initialValue={100}>
            <InputNumber min={0} max={100} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
