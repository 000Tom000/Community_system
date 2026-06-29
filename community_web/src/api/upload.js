import request from './request'

/** 上传图片 — 返回 { url: "/uploads/images/xxx.jpg" } */
export function uploadImage(file) {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/upload/image', fd)
}
