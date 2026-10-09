import { useEffect, useState } from 'react';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import { Tag, Tabs, Drawer, Descriptions, Popconfirm, message } from 'antd';
import { pageOrder, getOrderDetail, cancelOrder, refundOrder } from '@/api/order';
import type { OrderListVO, OrderPageReq, OrderDetailVO, OrderItemVO, PaymentVO } from '@/types/api';

const STATUS_MAP: Record<number, { text: string; color: string }> = {
  0: { text: '待支付', color: 'orange' },
  1: { text: '已完成', color: 'green' },
  2: { text: '已取消', color: 'default' },
  3: { text: '退款中', color: 'blue' },
  4: { text: '已退款', color: 'red' },
};

const PAY_TYPE_MAP: Record<string, string> = {
  cash: '现金', wechat: '微信', alipay: '支付宝', card: '刷卡',
};

export default function Order() {
  const [data, setData] = useState<OrderListVO[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [params, setParams] = useState<OrderPageReq>({ pageNum: 1, pageSize: 20 });
  const [detail, setDetail] = useState<OrderDetailVO | null>(null);
  const [detailVisible, setDetailVisible] = useState(false);

  const loadData = () => {
    setLoading(true);
    pageOrder(params)
      .then((res) => {
        setData(res.data);
        setTotal((res.page as any)?.total || 0);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, [params]);

  const handleDetail = async (orderId: number) => {
    const res = await getOrderDetail(orderId);
    setDetail(res.data);
    setDetailVisible(true);
  };
  const handleCancel = async (orderId: number) => {
    await cancelOrder(orderId);
    message.success('订单已取消');
    loadData();
  };
  const handleRefund = async (orderId: number) => {
    await refundOrder(orderId);
    message.success('退款成功');
    loadData();
  };

  const summaryAmount = data.reduce((sum, o) => sum + (o.actual || 0), 0);

  return (
    <PageContainer title="订单管理">
      <Tabs
        items={[
          { key: 'sale', label: '销售订单' },
          { key: 'recharge', label: '充值订单' },
          { key: 'card', label: '权益卡订单' },
          { key: 'coupon', label: '付费券订单' },
        ]}
      />
      <ProTable<OrderListVO>
        rowKey="id"
        loading={loading}
        dataSource={data}
        search={false}
        options={{ reload: loadData, density: true, setting: true }}
        headerTitle={
          <span>
            实付金额：<b style={{ color: '#cf1322' }}>¥{summaryAmount.toFixed(2)}</b>
            <span style={{ marginLeft: 24 }}>有效订单数：<b>{data.filter((o) => o.status === 1).length}</b></span>
          </span>
        }
        pagination={{
          current: params.pageNum,
          pageSize: params.pageSize,
          total,
          onChange: (page, pageSize) => setParams({ ...params, pageNum: page, pageSize }),
        }}
        columns={[
          { title: '订单号', dataIndex: 'orderNo', width: 180 },
          { title: '用户信息', dataIndex: 'memberName', render: (_, r: OrderListVO) => r.memberName || '散客' },
          { title: '订单合计', dataIndex: 'total', render: (_, r: OrderListVO) => `¥${r.total?.toFixed(2)}` },
          { title: '实付金额', dataIndex: 'actual', render: (_, r: OrderListVO) => `¥${r.actual?.toFixed(2)}` },
          { title: '支付方式', dataIndex: 'payType', render: (_, r: OrderListVO) => PAY_TYPE_MAP[r.payType] || r.payType },
          { title: '状态', dataIndex: 'status', width: 90, render: (_, r: OrderListVO) => <Tag color={STATUS_MAP[r.status]?.color}>{STATUS_MAP[r.status]?.text}</Tag> },
          { title: '消费时间', dataIndex: 'createTime', width: 170 },
          { title: '备注', dataIndex: 'remark', ellipsis: true },
          {
            title: '操作', width: 160, fixed: 'right',
            render: (_, r) => (
              <span>
                <a onClick={() => handleDetail(r.id)}>详情</a>
                {r.status === 0 && (
                  <Popconfirm title="确认取消该订单？" onConfirm={() => handleCancel(r.id)}>
                    <a style={{ marginLeft: 8 }}>取消</a>
                  </Popconfirm>
                )}
                {r.status === 1 && (
                  <Popconfirm title="确认退款？" onConfirm={() => handleRefund(r.id)}>
                    <a style={{ marginLeft: 8, color: 'red' }}>退款</a>
                  </Popconfirm>
                )}
              </span>
            ),
          },
        ]}
      />

      <Drawer title="订单详情" open={detailVisible} onClose={() => setDetailVisible(false)} width={640}>
        {detail && (
          <>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="订单号">{detail.orderNo}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={STATUS_MAP[detail.status]?.color}>{STATUS_MAP[detail.status]?.text}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="会员">{detail.memberName || '散客'}</Descriptions.Item>
              <Descriptions.Item label="收银员">{detail.clerkName || '-'}</Descriptions.Item>
              <Descriptions.Item label="订单合计">¥{detail.total?.toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="折扣率">{detail.discount}%</Descriptions.Item>
              <Descriptions.Item label="应付金额">¥{detail.payable?.toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="实付金额">¥{detail.actual?.toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="支付方式">{PAY_TYPE_MAP[detail.payType] || detail.payType}</Descriptions.Item>
              <Descriptions.Item label="下单时间">{detail.createTime}</Descriptions.Item>
              {detail.remark && <Descriptions.Item label="备注" span={2}>{detail.remark}</Descriptions.Item>}
            </Descriptions>

            <h4 style={{ marginTop: 16 }}>商品明细</h4>
            <ProTable<OrderItemVO>
              rowKey="id" dataSource={detail.items} pagination={false} search={false}
              options={false} size="small"
              columns={[
                { title: '类型', dataIndex: 'itemType', width: 60, render: (_, r: OrderItemVO) => r.itemType === 1 ? '商品' : '服务' },
                { title: '名称', dataIndex: 'itemName' },
                { title: '单价', dataIndex: 'price', render: (_, r: OrderItemVO) => `¥${r.price}` },
                { title: '数量', dataIndex: 'qty' },
                { title: '小计', dataIndex: 'amount', render: (_, r: OrderItemVO) => `¥${r.amount?.toFixed(2)}` },
              ]}
            />

            <h4 style={{ marginTop: 16 }}>支付流水</h4>
            <ProTable<PaymentVO>
              rowKey="id" dataSource={detail.payments} pagination={false} search={false}
              options={false} size="small"
              columns={[
                { title: '支付方式', dataIndex: 'payType', render: (_, r: PaymentVO) => PAY_TYPE_MAP[r.payType] || r.payType },
                { title: '金额', dataIndex: 'amount', render: (_, r: PaymentVO) => `¥${r.amount?.toFixed(2)}` },
                { title: '状态', dataIndex: 'status', render: (_, r: PaymentVO) => r.status === 1 ? '成功' : '失败' },
                { title: '退款', dataIndex: 'isRefund', render: (_, r: PaymentVO) => r.isRefund === 1 ? <Tag color="red">退款</Tag> : '-' },
                { title: '时间', dataIndex: 'createTime' },
              ]}
            />
          </>
        )}
      </Drawer>
    </PageContainer>
  );
}
