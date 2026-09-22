import request from '@/utils/request';
import type { ApiResult } from '@/types/api';
import type {
  DailyReportVO, MemberStatVO, TrendVO, LostMemberVO, RankVO,
} from '@/types/m6';

/** 营业日报区间查询 */
export function reportDaily(startDate?: string, endDate?: string) {
  return request.post<ApiResult<DailyReportVO[]>>('/report/daily', { startDate, endDate }) as unknown as Promise<ApiResult<DailyReportVO[]>>;
}

/** 会员统计 */
export function memberStat() {
  return request.post<ApiResult<MemberStatVO>>('/report/member-stat', {}) as unknown as Promise<ApiResult<MemberStatVO>>;
}

/** 消费充值趋势 */
export function reportTrend(days: number) {
  return request.post<ApiResult<TrendVO[]>>('/report/trend', { days }) as unknown as Promise<ApiResult<TrendVO[]>>;
}

/** 流失预警 */
export function reportLost(days: number) {
  return request.post<ApiResult<LostMemberVO[]>>('/report/lost', { days }) as unknown as Promise<ApiResult<LostMemberVO[]>>;
}

/** 销售排行 */
export function reportSalesRank(type: 'goods' | 'service' | 'clerk', topN: number, startTime?: string, endTime?: string) {
  return request.post<ApiResult<RankVO[]>>('/report/sales-rank', { type, topN, startTime, endTime }) as unknown as Promise<ApiResult<RankVO[]>>;
}
