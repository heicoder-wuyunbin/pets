import request from '@/utils/request';
import type { ApiResult } from '@/types/api';
import type {
  CategoryVO, CategorySaveReq, ServiceVO, ServicePageReq, ServiceSaveReq,
} from '@/types/m4';

/** 服务分类列表 */
export function listServiceCategory() {
  return request.post<ApiResult<CategoryVO[]>>('/service/category/list', {}) as unknown as Promise<ApiResult<CategoryVO[]>>;
}

/** 服务分类保存 */
export function saveServiceCategory(data: CategorySaveReq) {
  return request.post<ApiResult<void>>('/service/category/save', data) as unknown as Promise<ApiResult<void>>;
}

/** 服务分页 */
export function pageService(data: ServicePageReq) {
  return request.post<ApiResult<ServiceVO[]>>('/service/page', data) as unknown as Promise<ApiResult<ServiceVO[]>>;
}

/** 服务保存 */
export function saveService(data: ServiceSaveReq) {
  return request.post<ApiResult<void>>('/service/save', data) as unknown as Promise<ApiResult<void>>;
}

/** 服务上下架 */
export function setServiceStatus(id: number, status: number) {
  return request.post<ApiResult<void>>('/service/status', { id, status }) as unknown as Promise<ApiResult<void>>;
}
