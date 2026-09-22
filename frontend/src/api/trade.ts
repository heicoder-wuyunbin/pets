import request from '@/utils/request';
import type { ApiResult, CheckoutReq, CheckoutResult } from '@/types/api';

/** 收银台结算 */
export function checkout(data: CheckoutReq) {
  return request.post<ApiResult<CheckoutResult>>('/trade/checkout', data) as unknown as Promise<ApiResult<CheckoutResult>>;
}
