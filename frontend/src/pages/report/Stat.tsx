import { useEffect, useState } from 'react';
import {
  Table, Progress, Space, Spin, Empty, InputNumber, Button, Tag,
} from 'antd';
import { PageContainer, ProCard, StatisticCard } from '@ant-design/pro-components';
import { memberStat, reportTrend, reportLost } from '@/api/report';
import type { MemberStatVO, TrendVO, LostMemberVO } from '@/types/m6';

export default function ReportStat() {
  const [stat, setStat] = useState<MemberStatVO | null>(null);
  const [trend, setTrend] = useState<TrendVO[]>([]);
  const [lost, setLost] = useState<LostMemberVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [days, setDays] = useState(7);
  const [lostDays, setLostDays] = useState(30);

  const loadStat = () => memberStat().then((r) => setStat(r.data));
  const loadTrend = () => reportTrend(days).then((r) => setTrend(r.data));
  const loadLost = () => reportLost(lostDays).then((r) => setLost(r.data));

  const refreshAll = () => {
    setLoading(true);
    Promise.all([loadStat(), loadTrend(), loadLost()]).finally(() => setLoading(false));
  };

  useEffect(() => { refreshAll(); }, []);

  const maxConsume = Math.max(1, ...trend.map((t) => t.consume || 0));

  return (
    <PageContainer title="用户报表">
      <ProCard style={{ marginBottom: 16 }}>
        <Space>
          <span>趋势天数：</span>
          <InputNumber min={1} max={90} value={days} onChange={(v) => setDays(v || 7)} />
          <span>流失阈值(天)：</span>
          <InputNumber min={1} max={365} value={lostDays} onChange={(v) => setLostDays(v || 30)} />
          <Button type="primary" onClick={refreshAll}>刷新</Button>
        </Space>
      </ProCard>

      <Spin spinning={loading}>
        {/* 会员统计卡片 */}
        <StatisticCard.Group direction="row" style={{ marginBottom: 16 }}>
          <StatisticCard statistic={{ title: '会员总数', value: stat?.totalMember || 0 }} />
          <StatisticCard statistic={{ title: '今日新增', value: stat?.newMemberToday || 0, valueStyle: { color: '#3f8600' } }} />
          <StatisticCard statistic={{ title: '今日活跃', value: stat?.activeMemberToday || 0 }} />
        </StatisticCard.Group>

        <ProCard title="等级分布" style={{ marginBottom: 16 }}>
          <Space wrap>
            {stat?.levelDist?.length ? stat.levelDist.map((l) => (
              <Tag key={l.level} color="blue">Lv.{l.level}: {l.count}</Tag>
            )) : <span>-</span>}
          </Space>
        </ProCard>

        {/* 消费趋势 */}
        <ProCard title="消费/充值趋势" style={{ marginBottom: 16 }}>
          {trend.length === 0 ? <Empty /> : (
            <Table
              rowKey="date"
              dataSource={trend}
              pagination={false}
              columns={[
                { title: '日期', dataIndex: 'date', width: 120 },
                {
                  title: '消费', width: 300,
                  render: (_, r) => (
                    <Space>
                      <Progress
                        percent={Math.round(((r.consume || 0) / maxConsume) * 100)}
                        showInfo={false}
                        style={{ width: 200 }}
                      />
                      <span>¥{(r.consume || 0).toFixed(2)}</span>
                    </Space>
                  ),
                },
                {
                  title: '充值', width: 300,
                  render: (_, r) => (
                    <Space>
                      <Progress
                        strokeColor="#faad14"
                        percent={Math.round(((r.recharge || 0) / Math.max(1, ...trend.map((t) => t.recharge || 0))) * 100)}
                        showInfo={false}
                        style={{ width: 200 }}
                      />
                      <span>¥{(r.recharge || 0).toFixed(2)}</span>
                    </Space>
                  ),
                },
              ]}
            />
          )}
        </ProCard>

        {/* 流失预警 */}
        <ProCard title={<>流失预警 <Tag color="orange">超过 {lostDays} 天未消费</Tag></>}>
          {lost.length === 0 ? <Empty description="暂无流失会员" /> : (
            <Table
              rowKey="id"
              dataSource={lost}
              pagination={{ pageSize: 10 }}
              columns={[
                { title: '会员', dataIndex: 'name', width: 100 },
                { title: '手机号', dataIndex: 'mobile', width: 130 },
                {
                  title: '距上次消费', dataIndex: 'daysSinceLastConsume', width: 130,
                  render: (d: number) => d < 0 ? <Tag>从未消费</Tag> : <Tag color="red">{d} 天</Tag>,
                },
                { title: '最后消费时间', dataIndex: 'lastConsumeTime', render: (v: string) => v || '-' },
              ]}
            />
          )}
        </ProCard>
      </Spin>
    </PageContainer>
  );
}
