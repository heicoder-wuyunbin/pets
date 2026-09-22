import { useEffect, useState } from 'react';
import { Card, Table, Tag, Input, Select, Space, Modal, Typography } from 'antd';
import { pageNotice, getNoticeDetail } from '@/api/notice';
import type { NoticeVO, NoticePageReq } from '@/types/api';

export default function Notice() {
  const [data, setData] = useState<NoticeVO[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [params, setParams] = useState<NoticePageReq>({ pageNum: 1, pageSize: 10 });
  const [detailVisible, setDetailVisible] = useState(false);
  const [detail, setDetail] = useState<NoticeVO | null>(null);

  const loadData = () => {
    setLoading(true);
    pageNotice(params)
      .then((res) => {
        setData(res.data);
        setTotal((res.page as any)?.total || 0);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, [params]);

  const handleDetail = async (id: number) => {
    const res = await getNoticeDetail(id);
    setDetail(res.data);
    setDetailVisible(true);
  };

  return (
    <Card title="公告与资讯">
      <Space style={{ marginBottom: 16 }}>
        <Input.Search
          placeholder="搜索标题"
          allowClear
          onSearch={(v) => setParams({ ...params, pageNum: 1, keyword: v })}
          style={{ width: 200 }}
        />
        <Select
          placeholder="类型"
          allowClear
          style={{ width: 120 }}
          onChange={(v) => setParams({ ...params, pageNum: 1, type: v })}
          options={[
            { value: 1, label: '公告' },
            { value: 2, label: '资讯' },
          ]}
        />
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
          { title: '类型', dataIndex: 'type', width: 80, render: (t: number) => <Tag color={t === 1 ? 'blue' : 'green'}>{t === 1 ? '公告' : '资讯'}</Tag> },
          { title: '标题', dataIndex: 'title' },
          { title: '发布时间', dataIndex: 'createTime', width: 180 },
          { title: '操作', width: 80, render: (_, r) => <a onClick={() => handleDetail(r.id)}>查看</a> },
        ]}
      />
      <Modal
        title={detail?.title}
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={640}
      >
        <Typography.Paragraph>{detail?.content}</Typography.Paragraph>
      </Modal>
    </Card>
  );
}
