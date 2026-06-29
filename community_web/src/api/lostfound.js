import request from './request'

/** 列表 */
export function getItems(params) {
  return request.get('/lost-found', { params })
}

/** 详情 */
export function getItem(id) {
  return request.get(`/lost-found/${id}`)
}

/** 发布 */
export function createItem(data) {
  return request.post('/lost-found', data)
}

/** 编辑 */
export function updateItem(id, data) {
  return request.put(`/lost-found/${id}`, data)
}

/** 删除 */
export function deleteItem(id) {
  return request.delete(`/lost-found/${id}`)
}

/** 关闭（标记已找到/已归还） */
export function closeItem(id) {
  return request.put(`/lost-found/${id}/close`)
}
