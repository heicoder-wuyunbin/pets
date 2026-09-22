// ============================================================
// M5 会员中心
// ============================================================

/** 会员列表 VO */
export interface MemberListVO {
  id: number;
  mobile: string;
  name: string;
  gender: number;
  memberLevel: number;
  tags: string;
  balance: number;
  points: number;
  totalConsume: number;
  createTime: string;
}

/** 会员详情 VO */
export interface MemberDetailVO {
  id: number;
  mobile: string;
  name: string;
  gender: number;
  birthday: string;
  memberLevel: number;
  tags: string;
  remark: string;
  createTime: string;
  balance: number;
  points: number;
  totalConsume: number;
  totalRecharge: number;
  pets: MemberPetVO[];
}

export interface MemberPetVO {
  id: number;
  petName: string;
  petType: string;
  breed: string;
  gender: number;
  weight: number;
  birthday: string;
  vaccine: string;
}

/** 会员分页请求 */
export interface MemberPageReq {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  memberLevel?: number;
}

/** 会员保存请求 */
export interface MemberSaveReq {
  id?: number;
  mobile: string;
  name: string;
  gender?: number;
  birthday?: string;
  tags?: string;
  remark?: string;
  pets?: {
    id?: number;
    petName: string;
    petType?: string;
    breed?: string;
    gender?: number;
    weight?: number;
    birthday?: string;
    vaccine?: string;
  }[];
}

/** 卡种 VO */
export interface CardTypeVO {
  id: number;
  name: string;
  faceValue: number;
  discount: number;
  validDays: number;
  status: number;
  createTime: string;
}

/** 卡种保存请求 */
export interface CardTypeSaveReq {
  id?: number;
  name: string;
  faceValue?: number;
  discount?: number;
  validDays?: number;
  status?: number;
}

/** 会员卡 VO */
export interface MemberCardVO {
  id: number;
  memberId: number;
  cardTypeId: number;
  cardTypeName: string;
  balance: number;
  expireTime: string;
  createTime: string;
}

/** 账户流水 VO */
export interface AccountLogVO {
  id: number;
  type: number; // 1充值 2消费 3退款 4积分得 5积分抵
  amount: number;
  balanceAfter: number;
  points: number;
  pointsAfter: number;
  orderId: number;
  remark: string;
  createTime: string;
}

/** 次卡模板 VO */
export interface TimescardTemplateVO {
  id: number;
  name: string;
  serviceId: number;
  times: number;
  price: number;
  validDays: number;
  status: number;
  createTime: string;
}

/** 会员次卡 VO */
export interface MemberTimescardVO {
  id: number;
  memberId: number;
  templateId: number;
  templateName: string;
  remainTimes: number;
  expireTime: string;
  createTime: string;
}

/** 积分规则 */
export interface PointsRule {
  ratio: number;
  enable: number;
  offset: number;
}
