// ============================================================
// M4 商品与库存模块
// ============================================================

/** 商品分类 VO */
export interface CategoryVO {
  id: number;
  parentId: number;
  name: string;
  sort: number;
}

/** 分类保存请求 */
export interface CategorySaveReq {
  id?: number;
  parentId?: number;
  name: string;
  sort?: number;
}

/** 商品 SKU */
export interface GoodsSkuVO {
  id: number;
  goodsId: number;
  specName: string;
  price: number;
}

/** 商品 VO */
export interface GoodsVO {
  id: number;
  categoryId: number;
  name: string;
  barcode: string;
  unit: string;
  retailPrice: number;
  costPrice: number;
  pic: string;
  status: number; // 1上架 0下架
  createTime: string;
  skus: GoodsSkuVO[];
}

/** 商品分页请求 */
export interface GoodsPageReq {
  pageNum?: number;
  pageSize?: number;
  categoryId?: number;
  keyword?: string;
  status?: number;
}

/** 商品保存请求 */
export interface GoodsSaveReq {
  id?: number;
  categoryId?: number;
  name: string;
  barcode?: string;
  unit?: string;
  retailPrice?: number;
  costPrice?: number;
  pic?: string;
  status?: number;
  skus?: { id?: number; specName: string; price?: number }[];
}

/** 服务项目 VO */
export interface ServiceVO {
  id: number;
  categoryId: number;
  name: string;
  price: number;
  duration: number;
  description: string;
  canCard: number;
  status: number;
  createTime: string;
}

/** 服务分页请求 */
export interface ServicePageReq {
  pageNum?: number;
  pageSize?: number;
  categoryId?: number;
  keyword?: string;
  status?: number;
}

/** 服务保存请求 */
export interface ServiceSaveReq {
  id?: number;
  categoryId?: number;
  name: string;
  price?: number;
  duration?: number;
  description?: string;
  canCard?: number;
  status?: number;
}

/** 库存 VO */
export interface StockVO {
  id: number;
  goodsId: number;
  skuId: number;
  goodsName?: string;
  skuName?: string;
  quantity: number;
  warnQty: number;
  avgCost: number;
  updateTime: string;
}

/** 库存分页请求 */
export interface StockPageReq {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  warnOnly?: boolean;
}

/** 入库单 VO */
export interface StockInVO {
  id: number;
  no: string;
  supplier: string;
  totalAmount: number;
  status: number;
  remark: string;
  createTime: string;
  items: { goodsId: number; skuId: number; qty: number; price: number }[];
}

/** 入库单保存请求 */
export interface StockInSaveReq {
  supplier?: string;
  remark?: string;
  items: { goodsId: number; skuId?: number; qty: number; price?: number }[];
}

/** 出库单 VO */
export interface StockOutVO {
  id: number;
  no: string;
  type: number; // 1盘亏 2报损 3退货出库
  totalQty: number;
  remark: string;
  createTime: string;
  items: { goodsId: number; skuId: number; qty: number }[];
}

/** 出库单保存请求 */
export interface StockOutSaveReq {
  type?: number;
  remark?: string;
  items: { goodsId: number; skuId?: number; qty: number }[];
}
