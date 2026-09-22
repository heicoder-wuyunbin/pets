import request from '@/utils/request';
import type {
  ApiResult, ClerkVO, ClerkPageReq, ClerkAddReq, ClerkEditReq, ClerkStatusReq, ClerkResetPwdReq,
} from '@/types/api';

/** 员工分页 */
export function pageClerk(data: ClerkPageReq) {
  return request.post<ApiResult<ClerkVO[]>>('/clerk/page', data) as unknown as Promise<ApiResult<ClerkVO[]>>;
}

/** 新增员工 */
export function addClerk(data: ClerkAddReq) {
  return request.post<ApiResult<void>>('/clerk/add', data) as unknown as Promise<ApiResult<void>>;
}

/** 编辑员工 */
export function editClerk(data: ClerkEditReq) {
  return request.post<ApiResult<void>>('/clerk/edit', data) as unknown as Promise<ApiResult<void>>;
}

/** 切换员工状态 */
export function updateClerkStatus(data: ClerkStatusReq) {
  return request.post<ApiResult<void>>('/clerk/status', data) as unknown as Promise<ApiResult<void>>;
}

/** 重置密码 */
export function resetClerkPwd(data: ClerkResetPwdReq) {
  return request.post<ApiResult<void>>('/clerk/reset-pwd', data) as unknown as Promise<ApiResult<void>>;
}
