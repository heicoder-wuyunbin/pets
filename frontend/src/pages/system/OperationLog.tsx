import { useEffect, useState } from 'react';
import { Card, Table, Tag, Input, DatePicker, Space } from 'antd';
import { pageOperationLog } from '@/api/operationLog';
import type { OperationLogVO, OperationLogPageReq } from '@/types/api';

export default function OperationLog() {
  const [data, setData] = useState<OperationLogVO[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [params, setParams] = useState<OperationLogPageReq>({ pageNum: 1, pageSize: 20 });

  const loadData = () => {
    setLoading(true);
    pageOperationLog(params)
      .then((res) => {
        setData(res.data);
        setTotal((res.page as any)?.total || 0);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, [params]);

  return (
    <Card title="操作日志">
      <Space style={{ marginBottom: 16 }}>
        <Input
          placeholder="操作人"
          allowClear
          onChange={(e) => setParams({ ...params, pageNum: 1, userName: e.target.value })}
          style={{ width: 160 }}
        />
        <Input
          placeholder="模块"
          allowClear
          onChange={(e) => setParams({ ...params, pageNum: 1, module: e.target.value })}
          style={{ width: 160 }}
        />
        <DatePicker.RangePicker
          onChange={(_, dateStrings) => setParams({ ...params, pageNum: 1, startTime: dateStrings[0], endTime: dateStrings[1] })}
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
          { title: '操作人', dataIndex: 'userName', width: 120 },
          { title: '模块', dataIndex: 'module', width: 120 },
          { title: '动作', dataIndex: 'action', width: 120 },
          { title: '状态', dataIndex: 'status', width: 80, render: (s: number) => s === 1 ? <Tag color="green">成功</Tag> : <Tag color="red">失败</Tag> },
          { title: 'IP', dataIndex: 'ip', width: 140 },
          { title: '参数', dataIndex: 'params', ellipsis: true },
          { title: '时间', dataIndex: 'createTime', width: 180 },
        ]}
      />
    </Card>
  );
}
