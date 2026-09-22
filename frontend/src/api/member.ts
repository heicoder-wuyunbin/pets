import request from '@/utils/request';
import type { ApiResult } from '@/types/api';
import type {
  MemberListVO, MemberDetailVO, MemberPageReq, MemberSaveReq,
  CardTypeVO, CardTypeSaveReq, MemberCardVO, AccountLogVO,
  TimescardTemplateVO, MemberTimescardVO, PointsRule,
} from '@/types/m5';

// ==================== 会员档案 ====================

export function pageMember(data: MemberPageReq) {
  return request.post<ApiResult<MemberListVO[]>>('/member/page', data) as unknown as Promise<ApiResult<MemberListVO[]>>;
}

export function getMemberDetail(id: number) {
  return request.post<ApiResult<MemberDetailVO>>('/member/detail', { id }) as unknown as Promise<ApiResult<MemberDetailVO>>;
}

export function saveMember(data: MemberSaveReq) {
  return request.post<ApiResult<void>>('/member/save', data) as unknown as Promise<ApiResult<void>>;
}

export function deleteMember(id: number) {
  return request.post<ApiResult<void>>('/member/delete', { id }) as unknown as Promise<ApiResult<void>>;
}

// ==================== 会员卡 ====================

export function listCardType() {
  return request.post<ApiResult<CardTypeVO[]>>('/member/card/type/list', {}) as unknown as Promise<ApiResult<CardTypeVO[]>>;
}

export function saveCardType(data: CardTypeSaveReq) {
  return request.post<ApiResult<void>>('/member/card/type/save', data) as unknown as Promise<ApiResult<void>>;
}

export function rechargeCard(data: { memberId: number; cardTypeId?: number; amount: number }) {
  return request.post<ApiResult<void>>('/member/card/recharge', data) as unknown as Promise<ApiResult<void>>;
}

export function listMemberCard(memberId: number) {
  return request.post<ApiResult<MemberCardVO[]>>('/member/card/list', { memberId }) as unknown as Promise<ApiResult<MemberCardVO[]>>;
}

// ==================== 账户流水 ====================

export function listAccountLogs(memberId: number, type?: number) {
  return request.post<ApiResult<AccountLogVO[]>>('/member/account/logs', { memberId, type }) as unknown as Promise<ApiResult<AccountLogVO[]>>;
}

// ==================== 次卡 ====================

export function listTimescardTemplate() {
  return request.post<ApiResult<TimescardTemplateVO[]>>('/member/timescard/template/list', {}) as unknown as Promise<ApiResult<TimescardTemplateVO[]>>;
}

export function saveTimescardTemplate(data: any) {
  return request.post<ApiResult<void>>('/member/timescard/template/save', data) as unknown as Promise<ApiResult<void>>;
}

export function buyTimescard(memberId: number, templateId: number) {
  return request.post<ApiResult<void>>('/member/timescard/buy', { memberId, templateId }) as unknown as Promise<ApiResult<void>>;
}

export function listMemberTimescard(memberId: number) {
  return request.post<ApiResult<MemberTimescardVO[]>>('/member/timescard/list', { memberId }) as unknown as Promise<ApiResult<MemberTimescardVO[]>>;
}

// ==================== 积分 ====================

export function getPointsRule() {
  return request.post<ApiResult<PointsRule>>('/member/points/rule/get', {}) as unknown as Promise<ApiResult<PointsRule>>;
}

export function savePointsRule(data: PointsRule) {
  return request.post<ApiResult<void>>('/member/points/rule/save', data) as unknown as Promise<ApiResult<void>>;
}
