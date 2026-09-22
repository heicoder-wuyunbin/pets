import request from '@/utils/request';
import type { ApiResult, SurveyVO } from '@/types/api';

/** 店铺概况看板 */
export function getSurvey() {
  return request.post<ApiResult<SurveyVO>>('/shop/survey', {}) as unknown as Promise<ApiResult<SurveyVO>>;
}
