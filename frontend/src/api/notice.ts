import request from '@/utils/request';
import type { ApiResult, NoticeVO, NoticePageReq } from '@/types/api';

/** 公告分页 */
export function pageNotice(data: NoticePageReq) {
  return request.post<ApiResult<NoticeVO[]>>('/notice/page', data) as unknown as Promise<ApiResult<NoticeVO[]>>;
}

/** 公告详情 */
export function getNoticeDetail(id: number) {
  return request.post<ApiResult<NoticeVO>>('/notice/detail', { id }) as unknown as Promise<ApiResult<NoticeVO>>;
}
