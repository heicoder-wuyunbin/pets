import request from '@/utils/request';
import type {
  ApiResult, OrderListVO, OrderPageReq, OrderDetailVO,
} from '@/types/api';

/** 订单分页 */
export function pageOrder(data: OrderPageReq) {
  return request.post<ApiResult<OrderListVO[]>>('/trade/order/page', data) as unknown as Promise<ApiResult<OrderListVO[]>>;
}

/** 订单详情 */
export function getOrderDetail(orderId: number) {
  return request.post<ApiResult<OrderDetailVO>>('/trade/order/detail', { orderId }) as unknown as Promise<ApiResult<OrderDetailVO>>;
}

/** 取消订单 */
export function cancelOrder(orderId: number) {
  return request.post<ApiResult<void>>('/trade/order/cancel', { orderId }) as unknown as Promise<ApiResult<void>>;
}

/** 订单退款 */
export function refundOrder(orderId: number) {
  return request.post<ApiResult<void>>('/trade/order/refund', { orderId }) as unknown as Promise<ApiResult<void>>;
}
