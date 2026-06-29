import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由表 — 所有功能模块均已规划，未实现页面先用占位组件
 */
const routes = [
  // ===== 无需主布局的页面 =====
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' },
  },

  // ===== 主布局页面 =====
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      // 首页
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '' },
      },
      // --- 信息广场 ---
      {
        path: 'notices',
        name: 'Notices',
        component: () => import('@/views/Placeholder.vue'),
        props: { title: '校园公告', icon: '📋', desc: '学校通知、学术讲座、社团活动发布' },
        meta: { title: '校园公告' },
      },
      {
        path: 'lost-found',
        name: 'LostFound',
        component: () => import('@/views/LostFoundList.vue'),
        meta: { title: '失物招领' },
      },
      {
        path: 'lost-found/create',
        name: 'LostFoundCreate',
        component: () => import('@/views/CreateLostFound.vue'),
        meta: { title: '发布信息' },
      },
      {
        path: 'lost-found/:id',
        name: 'LostFoundDetail',
        component: () => import('@/views/LostFoundDetail.vue'),
        meta: { title: '物品详情' },
      },
      {
        path: 'lost-found/:id/edit',
        name: 'LostFoundEdit',
        component: () => import('@/views/CreateLostFound.vue'),
        meta: { title: '编辑信息' },
      },
      {
        path: 'secondhand',
        name: 'SecondHand',
        component: () => import('@/views/Placeholder.vue'),
        props: { title: '二手市场', icon: '🛒', desc: '闲置物品交易信息' },
        meta: { title: '二手市场' },
      },
      // --- 交流社区 ---
      {
        path: 'forum',
        name: 'Forum',
        component: () => import('@/views/Forum.vue'),
        meta: { title: '论坛广场' },
      },
      {
        path: 'forum/create',
        name: 'ForumCreate',
        component: () => import('@/views/CreatePost.vue'),
        meta: { title: '发帖' },
      },
      {
        path: 'forum/:id',
        name: 'PostDetail',
        component: () => import('@/views/PostDetail.vue'),
        meta: { title: '帖子详情' },
      },
      {
        path: 'forum/:id/edit',
        name: 'PostEdit',
        component: () => import('@/views/CreatePost.vue'),
        meta: { title: '编辑帖子' },
      },
      {
        path: 'qa',
        name: 'QA',
        component: () => import('@/views/QAList.vue'),
        meta: { title: '问答中心' },
      },
      {
        path: 'qa/ask',
        name: 'QAAsk',
        component: () => import('@/views/AskQuestion.vue'),
        meta: { title: '提问' },
      },
      {
        path: 'qa/:id',
        name: 'QADetail',
        component: () => import('@/views/QADetail.vue'),
        meta: { title: '问题详情' },
      },
      {
        path: 'qa/:id/edit',
        name: 'QAEdit',
        component: () => import('@/views/AskQuestion.vue'),
        meta: { title: '编辑问题' },
      },
      // --- 学习资源 ---
      {
        path: 'resources',
        name: 'Resources',
        component: () => import('@/views/Resources.vue'),
        meta: { title: '资料库' },
      },
      {
        path: 'resources/upload',
        name: 'ResourceUpload',
        component: () => import('@/views/ResourceUpload.vue'),
        meta: { title: '上传资源' },
      },
      {
        path: 'resources/:id',
        name: 'ResourceDetail',
        component: () => import('@/views/ResourceDetail.vue'),
        meta: { title: '资源详情' },
      },
      // --- 个人 ---
      {
        path: 'profile/:id',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '用户主页' },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '账号设置' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
