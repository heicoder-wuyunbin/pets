/** 统一响应体，对应后端 com.pets.common.web.R */
export interface ApiResult<T> {
  code: number;
  msg: string;
  data: T;
  page: unknown;
}

/** 登录请求 — 对应 LoginReq */
export interface LoginParams {
  phone: string;
  password: string;
  remember?: boolean;
  lang?: string;
}

/** 店铺选项 — 对应 ShopOption record */
export interface ShopOption {
  clerkId: number;
  shopId: number;
  shopName: string;
  roleName: string;
}

/** 登录/选店响应 — 对应 LoginResp record */
export interface LoginResult {
  token: string;
  shopId: number | null;
  shopName: string | null;
  roleCode: string | null;
  roleName: string | null;
  needSelect: boolean;
  shops: ShopOption[] | null;
}

/** 选店请求 — 对应 SelectShopReq */
export interface SelectShopParams {
  clerkId: number;
}

/** 注册请求 — 对应 RegisterReq */
export interface RegisterParams {
  phone: string;
  code: string;
  password: string;
}

/** 发送验证码请求 — 对应 SendCodeReq */
export interface SendCodeParams {
  phone: string;
  bizType: 'register' | 'reset_password';
}

/** 重置密码请求 — 对应 ResetPasswordReq */
export interface ResetPasswordParams {
  phone: string;
  code: string;
  newPassword: string;
}

// ============================================================
// M2 店铺概况 + 基础配置
// ============================================================

/** 店铺概况看板 — 对应 SurveyVO */
export interface SurveyVO {
  statistics: SurveyStatistics;
  todo: SurveyTodo;
  shop: SurveyShopInfo;
  news: SurveyNewsItem[];
}

export interface SurveyStatistics {
  todayOrder: number;
  orderTrend: string;
  todayMember: number;
  memberTrend: string;
  todayReminder: number;
  reminderTrend: string;
  todayAppointment: number;
  appointmentTrend: string;
}

export interface SurveyTodo {
  waitPay: number;
  waitDeliver: number;
  waitRefund: number;
  waitAppointment: number;
  stockShortage: number;
}

export interface SurveyShopInfo {
  id: number;
  shopName: string;
  bindPhone: string;
  expireTime: string;
  smsRemain: number;
}

export interface SurveyNewsItem {
  id: number;
  title: string;
  type: string;
  time: string;
}

/** 门店详情 — 对应 ShopDetailVO */
export interface ShopDetailVO {
  id: number;
  shopName: string;
  logo: string;
  bindPhone: string;
  level: number;
  expireTime: string;
  smsRemain: number;
  addr: string;
  telephone: string;
}

/** 门店更新请求 — 对应 ShopUpdateReq */
export interface ShopUpdateReq {
  shopName: string;
  logo?: string;
  addr?: string;
  telephone?: string;
}

/** 公告 VO — 对应 NoticeVO */
export interface NoticeVO {
  id: number;
  type: number;
  title: string;
  content: string;
  createTime: string;
}

/** 公告分页请求 */
export interface NoticePageReq {
  pageNum?: number;
  pageSize?: number;
  type?: number;
  keyword?: string;
}

/** 员工 VO — 对应 ClerkVO */
export interface ClerkVO {
  id: number;
  accountId: number;
  phone: string;
  truename: string;
  roleId: number;
  roleName: string;
  isSuper: number;
  status: number;
  hireDate: string;
}

/** 员工分页请求 */
export interface ClerkPageReq {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: number;
}

/** 新增员工请求 */
export interface ClerkAddReq {
  phone: string;
  truename: string;
  roleId: number;
  password: string;
}

/** 编辑员工请求 */
export interface ClerkEditReq {
  id: number;
  truename?: string;
  roleId?: number;
}

/** 员工状态请求 */
export interface ClerkStatusReq {
  id: number;
  status: number;
}

/** 重置密码请求 */
export interface ClerkResetPwdReq {
  id: number;
  newPassword: string;
}

/** 系统参数 VO */
export interface SysParamVO {
  id: number;
  groupKey: string;
  paramKey: string;
  paramValue: string;
  remark: string;
}

/** 系统参数列表请求 */
export interface SysParamListReq {
  group?: string;
}

/** 系统参数保存请求 */
export interface SysParamSaveReq {
  items: { paramKey: string; paramValue: string }[];
}

/** 操作日志 VO */
export interface OperationLogVO {
  id: number;
  shopId: number;
  userId: number;
  userName: string;
  module: string;
  action: string;
  params: string;
  ip: string;
  status: number;
  failReason: string;
  createTime: string;
}

/** 操作日志分页请求 */
export interface OperationLogPageReq {
  pageNum?: number;
  pageSize?: number;
  module?: string;
  userName?: string;
  startTime?: string;
  endTime?: string;
}

// ============================================================
// M3 收银台 + 订单管理
// ============================================================

/** 结算请求明细项 */
export interface CheckoutItem {
  itemType: number; // 1商品 2服务
  itemId: number;
  itemName: string;
  skuId?: number;
  price: number;
  qty: number;
  discount?: number;
}

/** 收银台结算请求 */
export interface CheckoutReq {
  memberId?: number;
  memberName?: string;
  payType: string; // cash/wechat/alipay/card
  discount?: number;
  remark?: string;
  items: CheckoutItem[];
}

/** 结算结果 */
export interface CheckoutResult {
  orderId: number;
  orderNo: string;
  total: number;
  payable: number;
  actualReceived: number;
  marketingDiscount?: number;
  activityName?: string;
}

/** 订单列表 VO */
export interface OrderListVO {
  id: number;
  orderNo: string;
  memberName: string;
  total: number;
  payable: number;
  actual: number;
  payType: string;
  status: number; // 0待支付 1已完成 2已取消 3退款中 4已退款
  remark: string;
  createTime: string;
}

/** 订单分页请求 */
export interface OrderPageReq {
  pageNum?: number;
  pageSize?: number;
  status?: number;
  clerkId?: number;
  keyword?: string;
  startTime?: string;
  endTime?: string;
}

/** 订单明细 VO */
export interface OrderItemVO {
  id: number;
  itemType: number;
  itemId: number;
  itemName: string;
  price: number;
  qty: number;
  discount: number;
  amount: number;
}

/** 订单详情 VO */
export interface OrderDetailVO {
  id: number;
  orderNo: string;
  memberId: number;
  memberName: string;
  clerkId: number;
  clerkName: string;
  total: number;
  discount: number;
  payable: number;
  actual: number;
  payType: string;
  status: number;
  remark: string;
  createTime: string;
  items: OrderItemVO[];
  payments: PaymentVO[];
}

/** 支付流水 VO */
export interface PaymentVO {
  id: number;
  orderId: number;
  orderNo: string;
  payType: string;
  amount: number;
  status: number;
  bizNo: string;
  isRefund: number;
  createTime: string;
}

/** 支付流水分页请求 */
export interface PaymentPageReq {
  pageNum?: number;
  pageSize?: number;
  payType?: string;
  orderId?: number;
  startTime?: string;
  endTime?: string;
}

/** 现金流水 VO */
export interface CashbookVO {
  id: number;
  type: number; // 1收入 2支出
  amount: number;
  bizType: string;
  orderId: number;
  clerkId: number;
  memo: string;
  createTime: string;
}

/** 现金流水分页请求 */
export interface CashbookPageReq {
  pageNum?: number;
  pageSize?: number;
  type?: number;
  startTime?: string;
  endTime?: string;
}

/** 服务单 VO */
export interface WorkOrderVO {
  id: number;
  orderId: number;
  orderItemId: number;
  serviceId: number;
  serviceName: string;
  clerkId: number;
  status: number; // 0待服务 1服务中 2已完成 3已取消
  remark: string;
  pics: string;
  startTime: string;
  finishTime: string;
  createTime: string;
}
