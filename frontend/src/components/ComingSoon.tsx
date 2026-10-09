import { PageContainer } from '@ant-design/pro-components';
import { ProCard } from '@ant-design/pro-components';
import { Empty, Button, Space, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';

interface ComingSoonProps {
  title: string;
  desc?: string;
  /** 计划中的能力点，可选；用于让“建设中”页显得有规划 */
  features?: string[];
}

/**
 * 功能占位页（Ant Design Pro 最佳实践版本）
 * - 统一使用 PageContainer 提供与真实页一致的页头/面包屑骨架
 * - 主体用 ProCard + Empty 呈现“建设中”状态，保持 Pro 设计语言一致
 * 适用场景：v70 同构菜单中后端尚未提供接口、暂时未实现的模块
 */
export default function ComingSoon({ title, desc, features }: ComingSoonProps) {
  const navigate = useNavigate();
  return (
    <PageContainer
      title={title}
      subTitle="模块建设中"
      extra={
        <Button type="primary" onClick={() => navigate('/survey')}>
          返回首页
        </Button>
      }
    >
      <ProCard style={{ minHeight: 380 }} bordered>
        <div
          style={{
            minHeight: 320,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description={
              <Space direction="vertical" size={6} style={{ textAlign: 'center' }}>
                <Typography.Title level={5} style={{ margin: 0 }}>
                  {title} · 功能建设中
                </Typography.Title>
                <Typography.Text type="secondary">
                  {desc ?? '该模块正在规划与开发中，后续版本将接入对应业务能力。'}
                </Typography.Text>
                {features?.length ? (
                  <Typography.Paragraph type="secondary" style={{ marginTop: 8, maxWidth: 420 }}>
                    已规划能力：{features.join('、')}
                  </Typography.Paragraph>
                ) : null}
              </Space>
            }
          >
            <Button onClick={() => navigate('/survey')}>返回首页</Button>
          </Empty>
        </div>
      </ProCard>
    </PageContainer>
  );
}
