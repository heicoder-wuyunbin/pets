import request from '@/utils/request';
import type { ApiResult, ShopDetailVO, ShopUpdateReq } from '@/types/api';

/** 门店详情 */
export function getShopDetail() {
  return request.post<ApiResult<ShopDetailVO>>('/shop/detail', {}) as unknown as Promise<ApiResult<ShopDetailVO>>;
}

/** 更新门店资料 */
export function updateShop(data: ShopUpdateReq) {
  return request.post<ApiResult<void>>('/shop/update', data) as unknown as Promise<ApiResult<void>>;
}
