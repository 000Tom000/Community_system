import request from './request'

/** 资源列表 */
export function getResources(params) {
  return request.get('/resources', { params })
}

/** 资源详情 + 我的评分 */
export function getResource(id) {
  return request.get(`/resources/${id}`)
}

/** 上传资源 — 用 FormData，axios 自动设置 Content-Type+boundary */
export function uploadResource(formData) {
  return request.post('/resources', formData)
}

/** 删除资源 */
export function deleteResource(id) {
  return request.delete(`/resources/${id}`)
}

/** 评分 */
export function rateResource(id, rating) {
  return request.post(`/resources/${id}/rate`, { rating })
}

/** 下载链接 */
export function getDownloadUrl(id) {
  return `http://localhost:8080/api/resources/${id}/download`
}
