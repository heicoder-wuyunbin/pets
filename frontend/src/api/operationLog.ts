import request from '@/utils/request';
import type { ApiResult, OperationLogVO, OperationLogPageReq } from '@/types/api';

/** 操作日志分页 */
export function pageOperationLog(data: OperationLogPageReq) {
  return request.post<ApiResult<OperationLogVO[]>>('/operation-log/page', data) as unknown as Promise<ApiResult<OperationLogVO[]>>;
}
