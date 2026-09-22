import request from '@/utils/request';
import type { ApiResult } from '@/types/api';
import type {
  StockVO, StockPageReq, StockInVO, StockInSaveReq, StockOutVO, StockOutSaveReq,
} from '@/types/m4';

/** 库存分页 */
export function pageStock(data: StockPageReq) {
  return request.post<ApiResult<StockVO[]>>('/stock/page', data) as unknown as Promise<ApiResult<StockVO[]>>;
}

/** 设置预警阈值 */
export function setStockWarnQty(id: number, warnQty: number) {
  return request.post<ApiResult<void>>('/stock/warn', { id, warnQty }) as unknown as Promise<ApiResult<void>>;
}

/** 创建入库单 */
export function createStockIn(data: StockInSaveReq) {
  return request.post<ApiResult<string>>('/stock/in/create', data) as unknown as Promise<ApiResult<string>>;
}

/** 入库单分页 */
export function pageStockIn(pageNum: number, pageSize: number, keyword?: string) {
  return request.post<ApiResult<StockInVO[]>>('/stock/in/page', { pageNum, pageSize, keyword }) as unknown as Promise<ApiResult<StockInVO[]>>;
}

/** 创建出库单 */
export function createStockOut(data: StockOutSaveReq) {
  return request.post<ApiResult<string>>('/stock/out/create', data) as unknown as Promise<ApiResult<string>>;
}

/** 出库单分页 */
export function pageStockOut(pageNum: number, pageSize: number, keyword?: string) {
  return request.post<ApiResult<StockOutVO[]>>('/stock/out/page', { pageNum, pageSize, keyword }) as unknown as Promise<ApiResult<StockOutVO[]>>;
}
