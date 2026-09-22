import request from '@/utils/request';
import type { ApiResult } from '@/types/api';
import type {
  CategoryVO, CategorySaveReq, GoodsVO, GoodsPageReq, GoodsSaveReq,
} from '@/types/m4';

/** 商品分类列表 */
export function listGoodsCategory() {
  return request.post<ApiResult<CategoryVO[]>>('/goods/category/list', {}) as unknown as Promise<ApiResult<CategoryVO[]>>;
}

/** 商品分类保存 */
export function saveGoodsCategory(data: CategorySaveReq) {
  return request.post<ApiResult<void>>('/goods/category/save', data) as unknown as Promise<ApiResult<void>>;
}

/** 商品分类删除 */
export function deleteGoodsCategory(id: number) {
  return request.post<ApiResult<void>>('/goods/category/delete', { id }) as unknown as Promise<ApiResult<void>>;
}

/** 商品分页 */
export function pageGoods(data: GoodsPageReq) {
  return request.post<ApiResult<GoodsVO[]>>('/goods/page', data) as unknown as Promise<ApiResult<GoodsVO[]>>;
}

/** 商品详情 */
export function getGoodsDetail(id: number) {
  return request.post<ApiResult<GoodsVO>>('/goods/detail', { id }) as unknown as Promise<ApiResult<GoodsVO>>;
}

/** 商品保存 */
export function saveGoods(data: GoodsSaveReq) {
  return request.post<ApiResult<void>>('/goods/save', data) as unknown as Promise<ApiResult<void>>;
}

/** 商品上下架 */
export function setGoodsStatus(id: number, status: number) {
  return request.post<ApiResult<void>>('/goods/status', { id, status }) as unknown as Promise<ApiResult<void>>;
}
