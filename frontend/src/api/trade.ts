import request from '@/utils/request';
import type { ApiResult, CheckoutReq, CheckoutResult, CashbookVO, CashbookPageReq } from '@/types/api';

/** 收银台结算 */
export function checkout(data: CheckoutReq) {
  return request.post<ApiResult<CheckoutResult>>('/trade/checkout', data) as unknown as Promise<ApiResult<CheckoutResult>>;
}

/** 现金流水分页 */
export function pageCashbook(data: CashbookPageReq) {
  return request.post<ApiResult<CashbookVO[]>>('/trade/cashbook/page', data) as unknown as Promise<ApiResult<CashbookVO[]>>;
}
