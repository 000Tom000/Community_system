import request from './request'

export function getItems(params) {
  return request.get('/secondhand', { params })
}

export function getItem(id) {
  return request.get(`/secondhand/${id}`)
}

export function createItem(data) {
  return request.post('/secondhand', data)
}

export function updateItem(id, data) {
  return request.put(`/secondhand/${id}`, data)
}

export function deleteItem(id) {
  return request.delete(`/secondhand/${id}`)
}

export function markSold(id) {
  return request.put(`/secondhand/${id}/sold`)
}
