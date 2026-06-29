import request from './request'

/** 问题列表 */
export function getQuestions(params) {
  return request.get('/qa', { params })
}

/** 问题详情（含回答） */
export function getQuestion(id) {
  return request.get(`/qa/${id}`)
}

/** 提问 */
export function createQuestion(data) {
  return request.post('/qa', data)
}

/** 编辑问题 */
export function updateQuestion(id, data) {
  return request.put(`/qa/${id}`, data)
}

/** 删除问题 */
export function deleteQuestion(id) {
  return request.delete(`/qa/${id}`)
}

/** 写回答 */
export function createAnswer(questionId, data) {
  return request.post(`/qa/${questionId}/answers`, data)
}

/** 删除回答 */
export function deleteAnswer(id) {
  return request.delete(`/qa/answers/${id}`)
}

/** 采纳回答 */
export function acceptAnswer(id, questionId) {
  return request.put(`/qa/answers/${id}/accept`, { questionId })
}

/** 点赞/取消点赞回答 */
export function toggleAnswerLike(id) {
  return request.post(`/qa/answers/${id}/like`)
}
