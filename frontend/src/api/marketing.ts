import request from '@/utils/request';
import type { ApiResult } from '@/types/api';
import type {
  MarketingVO, MarketingPageReq, MarketingSaveReq, MarketingCalcReq, MarketingCalcResult,
} from '@/types/m6';

/** 营销活动分页 */
export function pageMarketing(data: MarketingPageReq) {
  return request.post<ApiResult<MarketingVO[]>>('/marketing/page', data) as unknown as Promise<ApiResult<MarketingVO[]>>;
}

/** 营销活动列表（全部） */
export function listMarketing() {
  return request.post<ApiResult<MarketingVO[]>>('/marketing/list', {}) as unknown as Promise<ApiResult<MarketingVO[]>>;
}

/** 营销活动保存 */
export function saveMarketing(data: MarketingSaveReq) {
  return request.post<ApiResult<void>>('/marketing/save', data) as unknown as Promise<ApiResult<void>>;
}

/** 营销活动启停 */
export function setMarketingStatus(id: number, status: number) {
  return request.post<ApiResult<void>>('/marketing/status', { id, status }) as unknown as Promise<ApiResult<void>>;
}

/** 营销优惠计算（收银结算调用） */
export function calcMarketing(data: MarketingCalcReq) {
  return request.post<ApiResult<MarketingCalcResult>>('/marketing/calculate', data) as unknown as Promise<ApiResult<MarketingCalcResult>>;
}
