<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getItems } from '@/api/secondhand'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const categoryTabs = [
  { key: '', label: '全部' },
  { key: 'electronics', label: '数码电子' },
  { key: 'books', label: '书籍教材' },
  { key: 'clothing', label: '服饰鞋包' },
  { key: 'sports', label: '运动户外' },
  { key: 'daily', label: '生活日用' },
  { key: 'other', label: '其他' },
]

const sortOptions = [
  { value: 'new', label: '最新' },
  { value: 'hot', label: '热门' },
  { value: 'price_asc', label: '价格↑' },
  { value: 'price_desc', label: '价格↓' },
]

const activeCategory = ref('')
const activeSort = ref('new')
const keyword = ref('')
const items = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

const categoryLabels = Object.fromEntries(categoryTabs.map(t => [t.key, t.label]))

async function fetchItems() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, sort: activeSort.value }
    if (activeCategory.value) params.category = activeCategory.value
    if (keyword.value) params.keyword = keyword.value
    const res = await getItems(params)
    items.value = res.data.list
    total.value = res.data.total
  } catch { /* 拦截器已提示 */ }
  finally { loading.value = false }
}

function search() { page.value = 1; fetchItems() }

function goDetail(id) { router.push(`/secondhand/${id}`) }

function goCreate() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); router.push('/login'); return }
  router.push('/secondhand/create')
}

watch(activeCategory, () => { page.value = 1; fetchItems() })
watch(activeSort, () => { page.value = 1; fetchItems() })
onMounted(fetchItems)
</script>

<template>
  <div class="sh-list">
    <div class="sh-toolbar">
      <div class="top-row">
        <el-radio-group v-model="activeCategory" size="small">
          <el-radio-button v-for="c in categoryTabs" :key="c.key" :value="c.key">{{ c.label }}</el-radio-button>
        </el-radio-group>
        <div class="actions">
          <el-select v-model="activeSort" size="small" style="width:100px">
            <el-option v-for="s in sortOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
          <el-input v-model="keyword" placeholder="搜索商品…" size="small" style="width:200px"
                    clearable @keyup.enter="search" @clear="search" />
          <el-button type="primary" size="small" @click="goCreate">✍️ 发布商品</el-button>
        </div>
      </div>
    </div>

    <div v-loading="loading" class="item-list">
      <div v-if="items.length === 0 && !loading" class="empty">
        <el-empty description="暂无商品" />
      </div>

      <div v-for="item in items" :key="item.id" class="item-card" @click="goDetail(item.id)">
        <div class="item-left">
          <el-avatar :size="40" :src="item.authorAvatar" />
        </div>
        <div class="item-body">
          <div class="item-title">
            <el-tag v-if="item.status === 1" size="small" type="info" style="margin-right:6px">已售</el-tag>
            <span class="title-text">{{ item.title }}</span>
          </div>
          <div class="item-meta">
            <span class="price">¥{{ item.price }}</span>
            <span v-if="item.originalPrice" class="original">¥{{ item.originalPrice }}</span>
            <span class="dot">·</span>
            <span>{{ item.conditionText }}</span>
            <span v-if="item.isNegotiable" class="negotiable">· 可议价</span>
            <span class="dot">·</span>
            <span class="author">{{ item.authorNickname }}</span>
            <span class="dot">·</span>
            <span>{{ item.createdAt }}</span>
          </div>
        </div>
        <div class="item-stats">
          <span>👁 {{ item.viewCount }}</span>
        </div>
      </div>
    </div>

    <div class="pagination" v-if="total > size">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total"
                     layout="prev, pager, next" @current-change="fetchItems" background small />
    </div>
  </div>
</template>

<style scoped>
.sh-list { max-width: 900px; margin: 0 auto; padding: 20px 0; }
.sh-toolbar { margin-bottom: 16px; }
.top-row { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.actions { display: flex; gap: 10px; align-items: center; }
.item-card { display: flex; align-items: flex-start; gap: 14px; background: #fff; padding: 16px 20px;
  border-radius: 6px; margin-bottom: 8px; cursor: pointer; transition: box-shadow .2s; }
.item-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.item-body { flex: 1; min-width: 0; }
.title-text { font-size: 16px; font-weight: 500; color: #303133; }
.item-meta { margin-top: 6px; font-size: 13px; color: #909399; display: flex; align-items: center; gap: 2px; flex-wrap: wrap; }
.price { font-size: 16px; font-weight: 700; color: #f56c6c; }
.original { font-size: 12px; color: #c0c4cc; text-decoration: line-through; margin-left: 4px; }
.dot { margin: 0 4px; }
.negotiable { color: #e6a23c; }
.item-stats { font-size: 13px; color: #909399; white-space: nowrap; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
.empty { margin-top: 60px; }
</style>
