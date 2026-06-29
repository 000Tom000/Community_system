import request from './request'

/** 发送消息给 AI，返回 { reply: "..." } */
export function chatWithAI(messages) {
  return request.post('/ai/chat', { messages })
}
