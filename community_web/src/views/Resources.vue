<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getResources, getDownloadUrl } from '@/api/resource'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const categories = [
  { key: '', label: '全部' },
  { key: 'courseware', label: '课件' },
  { key: 'notes', label: '笔记' },
  { key: 'booklist', label: '书单' },
  { key: 'exam', label: '试卷' },
  { key: 'other', label: '其他' },
]
const sorts = [
  { key: 'new', label: '最新' },
  { key: 'hot', label: '最热' },
  { key: 'rating', label: '评分最高' },
]

const category = ref('')
const keyword = ref('')
const sort = ref('new')
const resources = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

const catLabels = Object.fromEntries(categories.map(c => [c.key, c.label]))

async function fetchList() {
  loading.value = true
  try {
    const res = await getResources({
      category: category.value || undefined,
      keyword: keyword.value || undefined,
      sort: sort.value,
      page: page.value,
      size: size.value,
    })
    resources.value = res.data.list
    total.value = res.data.total
  } catch { /* ignore */ }
  finally { loading.value = false }
}

function search() { page.value = 1; fetchList() }

watch([category, sort], () => { page.value = 1; fetchList() })

onMounted(fetchList)

function goDetail(id) { router.push(`/resources/${id}`) }
function goUpload() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); router.push('/login'); return }
  router.push('/resources/upload')
}

// 文件类型图标
function fileIcon(type) {
  const map = { pdf: '📕', ppt: '📊', doc: '📝', xls: '📈', zip: '📦', img: '🖼️', txt: '📄' }
  return map[type] || '📁'
}
</script>

<template>
  <div class="resources-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="left">
        <el-radio-group v-model="category" size="small">
          <el-radio-button v-for="c in categories" :key="c.key" :value="c.key">{{ c.label }}</el-radio-button>
        </el-radio-group>
      </div>
      <div class="right">
        <el-select v-model="sort" size="small" style="width:110px">
          <el-option v-for="s in sorts" :key="s.key" :label="s.label" :value="s.key" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索资源…" size="small" style="width:200px"
                  clearable @keyup.enter="search" @clear="search" />
        <el-button type="primary" size="small" @click="goUpload">📤 上传</el-button>
      </div>
    </div>

    <!-- 列表 -->
    <div v-loading="loading">
      <el-empty v-if="!loading && resources.length === 0" description="暂无资源" />

      <div v-for="r in resources" :key="r.id" class="resource-card" @click="goDetail(r.id)">
        <div class="r-icon">{{ fileIcon(r.fileType) }}</div>
        <div class="r-body">
          <div class="r-title">{{ r.title }}</div>
          <div class="r-meta">
            <span>{{ r.authorNickname }}</span>
            <span class="dot">·</span>
            <span>{{ r.createdAt }}</span>
            <span class="dot">·</span>
            <span>{{ r.fileSizeStr }}</span>
            <span class="dot">·</span>
            <el-tag size="small">{{ catLabels[r.category] || r.category }}</el-tag>
          </div>
        </div>
        <div class="r-stats">
          <span>⬇️ {{ r.downloadCount }}</span>
          <span>⭐ {{ r.ratingAvg ?? 0 }}</span>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > size">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                     layout="prev, pager, next" @current-change="fetchList" background small />
    </div>
  </div>
</template>

<style scoped>
.resources-page { max-width: 900px; margin: 0 auto; padding: 20px 0; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 10px; }
.right { display: flex; gap: 10px; align-items: center; }
.resource-card {
  display: flex; align-items: center; gap: 14px;
  background: #fff; padding: 16px 20px; border-radius: 6px;
  margin-bottom: 8px; cursor: pointer; transition: box-shadow .2s;
}
.resource-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.r-icon { font-size: 36px; width: 50px; text-align: center; }
.r-body { flex: 1; min-width: 0; }
.r-title { font-size: 16px; font-weight: 500; color: #303133; }
.r-meta { margin-top: 4px; font-size: 13px; color: #909399; }
.dot { margin: 0 6px; }
.r-stats { display: flex; flex-direction: column; gap: 4px; font-size: 13px; color: #909399; white-space: nowrap; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
