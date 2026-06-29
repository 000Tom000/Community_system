<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getNotices } from '@/api/notice'

const router = useRouter()
const userStore = useUserStore()

const categoryTabs = [
  { key: '', label: '全部' },
  { key: 'academic', label: '学术讲座' },
  { key: 'club', label: '社团活动' },
  { key: 'admin', label: '学校通知' },
  { key: 'exam', label: '考试教务' },
  { key: 'general', label: '综合' },
]

const sortOptions = [
  { value: 'new', label: '最新' },
  { value: 'hot', label: '热门' },
]

const activeCategory = ref('')
const activeSort = ref('new')
const keyword = ref('')
const notices = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

const categoryLabels = Object.fromEntries(categoryTabs.map(t => [t.key, t.label]))

const isAdmin = () => userStore.user?.role === 'admin'

async function fetchNotices() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, sort: activeSort.value }
    if (activeCategory.value) params.category = activeCategory.value
    if (keyword.value) params.keyword = keyword.value
    const res = await getNotices(params)
    notices.value = res.data.list
    total.value = res.data.total
  } catch { /* 拦截器已提示 */ }
  finally { loading.value = false }
}

function search() { page.value = 1; fetchNotices() }
function goDetail(id) { router.push(`/notices/${id}`) }
function goCreate() { router.push('/notices/create') }

watch(activeCategory, () => { page.value = 1; fetchNotices() })
watch(activeSort, () => { page.value = 1; fetchNotices() })
onMounted(fetchNotices)
</script>

<template>
  <div class="notice-list">
    <div class="toolbar">
      <div class="top-row">
        <el-radio-group v-model="activeCategory" size="small">
          <el-radio-button v-for="c in categoryTabs" :key="c.key" :value="c.key">{{ c.label }}</el-radio-button>
        </el-radio-group>
        <div class="actions">
          <el-select v-model="activeSort" size="small" style="width:100px">
            <el-option v-for="s in sortOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
          <el-input v-model="keyword" placeholder="搜索公告…" size="small" style="width:200px"
                    clearable @keyup.enter="search" @clear="search" />
          <el-button v-if="isAdmin()" type="primary" size="small" @click="goCreate">📢 发布公告</el-button>
        </div>
      </div>
    </div>

    <div v-loading="loading" class="item-list">
      <div v-if="notices.length === 0 && !loading" class="empty">
        <el-empty description="暂无公告" />
      </div>

      <div v-for="n in notices" :key="n.id" class="item-card" :class="{ pinned: n.isPinned }" @click="goDetail(n.id)">
        <div class="item-left">
          <el-avatar :size="40" :src="n.authorAvatar" />
        </div>
        <div class="item-body">
          <div class="item-title">
            <el-tag v-if="n.isPinned" size="small" type="danger" style="margin-right:6px">置顶</el-tag>
            <el-tag v-if="n.level === 'urgent'" size="small" type="danger" style="margin-right:6px">紧急</el-tag>
            <el-tag v-else-if="n.level === 'important'" size="small" type="warning" style="margin-right:6px">重要</el-tag>
            <span class="title-text">{{ n.title }}</span>
          </div>
          <div class="item-meta">
            <span class="author">{{ n.authorNickname }}</span>
            <span class="dot">·</span>
            <span>{{ n.createdAt }}</span>
            <span class="dot">·</span>
            <span>{{ categoryLabels[n.category] || n.category }}</span>
          </div>
        </div>
        <div class="item-stats">
          <span>👁 {{ n.viewCount }}</span>
        </div>
      </div>
    </div>

    <div class="pagination" v-if="total > size">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                     layout="prev, pager, next" @current-change="fetchNotices" background small />
    </div>
  </div>
</template>

<style scoped>
.notice-list { max-width: 900px; margin: 0 auto; padding: 20px 0; }
.toolbar { margin-bottom: 16px; }
.top-row { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.actions { display: flex; gap: 10px; align-items: center; }
.item-card { display: flex; align-items: flex-start; gap: 14px; background: #fff; padding: 16px 20px;
  border-radius: 6px; margin-bottom: 8px; cursor: pointer; transition: box-shadow .2s; }
.item-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.item-card.pinned { background: #fef0f0; }
.item-body { flex: 1; min-width: 0; }
.title-text { font-size: 16px; font-weight: 500; color: #303133; }
.item-meta { margin-top: 6px; font-size: 13px; color: #909399; }
.dot { margin: 0 6px; }
.item-stats { font-size: 13px; color: #909399; white-space: nowrap; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
.empty { margin-top: 60px; }
</style>
