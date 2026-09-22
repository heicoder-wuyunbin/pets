import request from '@/utils/request';
import type { ApiResult, SysParamVO, SysParamListReq, SysParamSaveReq } from '@/types/api';

/** 系统参数列表 */
export function listSysParam(data: SysParamListReq) {
  return request.post<ApiResult<SysParamVO[]>>('/sys-param/list', data) as unknown as Promise<ApiResult<SysParamVO[]>>;
}

/** 保存系统参数 */
export function saveSysParam(data: SysParamSaveReq) {
  return request.post<ApiResult<void>>('/sys-param/save', data) as unknown as Promise<ApiResult<void>>;
}
