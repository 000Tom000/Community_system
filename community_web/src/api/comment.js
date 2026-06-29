import request from './request'

/** 获取评论列表 */
export function getComments(postId) {
  return request.get('/comments', { params: { postId } })
}

/** 发表评论/回复 */
export function addComment(data) {
  return request.post('/comments', data)
}

/** 删除评论 */
export function deleteComment(id) {
  return request.delete(`/comments/${id}`)
}

/** 点赞/取消评论 */
export function toggleCommentLike(id) {
  return request.post(`/comments/${id}/like`)
}
