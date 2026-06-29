import request from './request'

/** 帖子列表 */
export function getPosts(params) {
  return request.get('/posts', { params })
}

/** 帖子详情 */
export function getPost(id) {
  return request.get(`/posts/${id}`)
}

/** 发帖 */
export function createPost(data) {
  return request.post('/posts', data)
}

/** 编辑帖子 */
export function updatePost(id, data) {
  return request.put(`/posts/${id}`, data)
}

/** 删除帖子 */
export function deletePost(id) {
  return request.delete(`/posts/${id}`)
}

/** 点赞/取消点赞 */
export function toggleLike(id) {
  return request.post(`/posts/${id}/like`)
}

/** 查询是否已点赞 */
export function getLikeStatus(id) {
  return request.get(`/posts/${id}/like`)
}

/** 置顶（管理员） */
export function togglePin(id) {
  return request.put(`/posts/${id}/pin`)
}

/** 标记解决（问答） */
export function toggleSolved(id) {
  return request.put(`/posts/${id}/solve`)
}
