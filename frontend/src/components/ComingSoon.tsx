import { Result, Button } from 'antd';
import { useNavigate } from 'react-router-dom';

interface ComingSoonProps {
  title: string;
  desc?: string;
}

/** 功能占位页：v70 同构菜单中暂未实现的功能统一展示 */
export default function ComingSoon({ title, desc }: ComingSoonProps) {
  const navigate = useNavigate();
  return (
    <Result
      status="info"
      title={title}
      subTitle={desc ?? '该功能模块正在建设中，敬请期待'}
      extra={
        <Button type="primary" onClick={() => navigate('/survey')}>
          返回首页
        </Button>
      }
    />
  );
}
