// ============================================================
// M6 营销与数据统计
// ============================================================

/** 营销活动 VO */
export interface MarketingVO {
  id: number;
  name: string;
  type: number; // 1满减 2折扣
  threshold: number;
  discountValue: number;
  goodsIds: string;
  startTime: string;
  endTime: string;
  status: number; // 1启用 0停用
  createTime: string;
}

/** 营销活动分页请求 */
export interface MarketingPageReq {
  pageNum?: number;
  pageSize?: number;
  status?: number;
}

/** 营销活动保存请求 */
export interface MarketingSaveReq {
  id?: number;
  name: string;
  type: number;
  threshold?: number;
  discountValue?: number;
  goodsIds?: string;
  startTime?: string;
  endTime?: string;
  status?: number;
}

/** 营销计算请求 */
export interface MarketingCalcReq {
  shopId: number;
  goodsIds: number[];
  totalAmount: number;
}

/** 营销计算结果 */
export interface MarketingCalcResult {
  activityId: number | null;
  activityName: string | null;
  discountAmount: number;
  payableAmount: number;
}

/** 营业日报 VO */
export interface DailyReportVO {
  reportDate: string;
  saleAmount: number;
  orderCount: number;
  refundAmount: number;
  avgOrder: number;
  memberCount: number;
  newMember: number;
}

/** 会员统计 VO */
export interface MemberStatVO {
  totalMember: number;
  newMemberToday: number;
  activeMemberToday: number;
  levelDist: { level: number; count: number }[];
}

/** 消费充值趋势 VO */
export interface TrendVO {
  date: string;
  recharge: number;
  consume: number;
}

/** 流失会员 VO */
export interface LostMemberVO {
  id: number;
  name: string;
  mobile: string;
  totalConsume: number;
  lastConsumeTime: string;
  daysSinceLastConsume: number;
}

/** 销售排行 VO */
export interface RankVO {
  itemId: number;
  itemName: string;
  count: number;
  amount: number;
}
