import request from './request'

/** 注册 */
export function register(username, password, nickname) {
  return request.post('/users/register', { username, password, nickname })
}

/** 登录 */
export function login(username, password) {
  return request.post('/users/login', { username, password })
}

/** 获取当前用户信息 */
export function getMe() {
  return request.get('/users/me')
}

/** 查看用户主页 */
export function getUserById(id) {
  return request.get(`/users/${id}`)
}

/** 更新个人资料 */
export function updateProfile(data) {
  return request.put('/users/me', data)
}

/** 修改密码 */
export function changePassword(oldPassword, newPassword) {
  return request.put('/users/password', { oldPassword, newPassword })
}

/** 退出登录 */
export function logout() {
  return request.post('/users/logout')
}
