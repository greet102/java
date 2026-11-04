<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from '../store'

const {
  state,
  adminMe,
  adminLogout,
  fetchAdminStats,
  fetchAdminUsers,
  updateAdminUser,
  deleteAdminUser,
} = useStore()

const ready = ref(false)
const tab = ref('overview') // overview | users

const stats = reactive({ userCount: 0, postCount: 0, followCount: 0, todayUserCount: 0 })

const users = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const loading = ref(false)
const err = ref('')

const showEdit = ref(false)
const editing = ref('')
const form = reactive({ nickname: '', slogan: '', bio: '', avatar: '', location: '', password: '', permission: 1 })
const EMOJIS = ['🐱', '🌈', '⭐', '🍰', '🌸', '🎈', '🐻', '💖', '🌟', '🍓']

const toast = ref('')
let toastTimer = null

onMounted(async () => {
  const res = await adminMe()
  if (!res.ok) {
    location.hash = '#/admin/login'
    return
  }
  ready.value = true
  loadStats()
  loadUsers()
})

function showToast(text) {
  toast.value = text
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 1800)
}

async function loadStats() {
  const res = await fetchAdminStats()
  if (res.ok && res.data) Object.assign(stats, res.data)
}

async function loadUsers() {
  loading.value = true
  err.value = ''
  const res = await fetchAdminUsers({
    keyword: keyword.value.trim(),
    page: page.value,
    size: size.value,
  })
  loading.value = false
  if (!res.ok) {
    err.value = res.msg
    return
  }
  users.value = res.data.records || []
  total.value = res.data.total || 0
}

function search() {
  page.value = 1
  loadUsers()
}

function gotoPage(p) {
  if (p < 1) return
  page.value = p
  loadUsers()
}

function totalPages() {
  return Math.max(1, Math.ceil(total.value / size.value))
}

function openEdit(u) {
  editing.value = u.username
  form.nickname = u.nickname || ''
  form.slogan = u.slogan || ''
  form.bio = u.bio || ''
  form.avatar = u.avatar || '🐱'
  form.location = u.location || ''
  form.password = ''
  form.permission = u.permission ?? 1
  showEdit.value = true
}

async function saveEdit() {
  const payload = {
    nickname: form.nickname,
    slogan: form.slogan,
    bio: form.bio,
    avatar: form.avatar,
    location: form.location,
    permission: Number(form.permission),
  }
  if (form.password.trim()) payload.password = form.password
  const res = await updateAdminUser(editing.value, payload)
  if (!res.ok) return showToast('保存失败：' + res.msg)
  showEdit.value = false
  showToast('已保存 ✏️')
  loadUsers()
  loadStats()
}

async function removeUser(u) {
  if (
    !confirm(
      `确定删除用户「${u.username}」吗？\n该用户的文章、点赞、收藏、关注、私信都会被一并删除，且不可恢复。`
    )
  ) {
    return
  }
  const res = await deleteAdminUser(u.username)
  if (!res.ok) return showToast('删除失败：' + res.msg)
  showToast('已删除 🗑️')
  // 若当前页只剩下这一条且不是第一页，回退一页
  if (users.value.length === 1 && page.value > 1) page.value--
  loadUsers()
  loadStats()
}

function onLogout() {
  adminLogout()
  // 退出后台后直接回到前台首页（若未登录前台则落在前台登录页，属于正常的首页入口）
  location.hash = '#/'
}

function backHome() {
  location.hash = '#/'
}

function formatTime(v) {
  if (!v) return '—'
  if (typeof v === 'number') return new Date(v).toLocaleString('zh-CN')
  const d = new Date(v)
  if (isNaN(d.getTime())) return String(v)
  return d.toLocaleString('zh-CN', { hour12: false })
}
</script>

<template>
  <div class="admin-shell" v-if="ready">
    <header class="admin-top">
      <div class="admin-brand">🛠️ 喵喵博客 · 后台管理</div>
      <div class="admin-top-right">
        <span class="admin-user">👤 {{ state.adminUser }}</span>
        <button class="btn btn-ghost" @click="backHome">返回前台</button>
        <button class="btn btn-ghost" @click="onLogout">退出后台</button>
      </div>
    </header>

    <div class="admin-body">
      <aside class="admin-side">
        <button class="admin-nav" :class="{ active: tab === 'overview' }" @click="tab = 'overview'">
          📊 数据概览
        </button>
        <button class="admin-nav" :class="{ active: tab === 'users' }" @click="tab = 'users'">
          👥 用户管理
        </button>
      </aside>

      <main class="admin-main">
        <!-- 数据概览 -->
        <section v-if="tab === 'overview'">
          <h2 class="admin-h2">📊 数据概览</h2>
          <div class="stat-grid">
            <div class="stat-box"><b>{{ stats.userCount }}</b><span>注册用户</span></div>
            <div class="stat-box"><b>{{ stats.postCount }}</b><span>文章总数</span></div>
            <div class="stat-box"><b>{{ stats.followCount }}</b><span>关注关系</span></div>
            <div class="stat-box"><b>{{ stats.todayUserCount }}</b><span>今日新增</span></div>
          </div>
          <p class="admin-tip">
            💡 切换到「用户管理」可以对首页展示的用户数据进行搜索、编辑资料、重置密码与删除。
          </p>
        </section>

        <!-- 用户管理 -->
        <section v-else>
          <div class="user-head">
            <h2 class="admin-h2">👥 用户管理</h2>
            <span class="feed-count">共 {{ total }} 位用户</span>
            <input
              class="search-input"
              v-model="keyword"
              @keyup.enter="search"
              placeholder="🔍 搜索用户名 / 昵称"
            />
            <button class="btn btn-ghost btn-sm" @click="search">搜索</button>
          </div>

          <div v-if="err" class="admin-err">{{ err }}</div>
          <div v-else-if="loading" class="admin-loading">加载中…</div>
          <table v-else class="user-table">
            <thead>
              <tr>
                <th>头像</th>
                <th>用户名</th>
                <th>昵称</th>
                <th>权限</th>
                <th>标语</th>
                <th>地区</th>
                <th>注册时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="u in users" :key="u.id">
                <td class="u-avatar">{{ u.avatar || '🐱' }}</td>
                <td>{{ u.username }}</td>
                <td>{{ u.nickname || '—' }}</td>
                <td>{{ Number(u.permission) === 0 ? '🛠 管理员' : '👤 普通用户' }}</td>
                <td class="u-slogan">{{ u.slogan || '—' }}</td>
                <td>{{ u.location || '—' }}</td>
                <td class="u-time">{{ formatTime(u.createdAt) }}</td>
                <td class="u-actions">
                  <button class="act-btn" @click="openEdit(u)">编辑</button>
                  <button class="act-btn del" @click="removeUser(u)">删除</button>
                </td>
              </tr>
              <tr v-if="!users.length">
                <td colspan="8" class="empty">没有匹配的用户，换个关键词试试～</td>
              </tr>
            </tbody>
          </table>

          <div class="pager" v-if="total > size">
            <button class="pg-btn" :disabled="page <= 1" @click="gotoPage(page - 1)">上一页</button>
            <span class="pg-info">第 {{ page }} / {{ totalPages() }} 页</span>
            <button
              class="pg-btn"
              :disabled="page >= totalPages()"
              @click="gotoPage(page + 1)"
            >
              下一页
            </button>
          </div>
        </section>
      </main>
    </div>

    <!-- 编辑弹窗 -->
    <div class="modal-mask" v-if="showEdit" @click.self="showEdit = false">
      <div class="modal-card edit-card">
        <button class="modal-close" @click="showEdit = false">✕</button>
        <h2 class="form-title">编辑用户 · {{ editing }}</h2>
        <div class="form">
          <label class="field">
            <span>昵称</span>
            <input v-model="form.nickname" placeholder="昵称" />
          </label>
          <label class="field">
            <span>标语</span>
            <input v-model="form.slogan" placeholder="一句话签名" />
          </label>
          <label class="field">
            <span>地区</span>
            <input v-model="form.location" placeholder="所在地" />
          </label>
          <label class="field">
            <span>个人简介</span>
            <textarea v-model="form.bio" rows="3" placeholder="个人简介"></textarea>
          </label>
          <div class="field">
            <span>头像</span>
            <div class="emoji-row wrap">
              <button
                v-for="e in EMOJIS"
                :key="e"
                class="emoji-opt"
                :class="{ sel: form.avatar === e }"
                @click="form.avatar = e"
              >
                {{ e }}
              </button>
            </div>
          </div>
          <label class="field">
            <span>重置密码（留空则不修改）</span>
            <input type="password" v-model="form.password" placeholder="输入新密码" />
          </label>
          <div class="field">
            <span>权限</span>
            <select v-model="form.permission" class="perm-select">
              <option :value="1">👤 普通用户（默认）</option>
              <option :value="0">🛠 管理员（可登录后台）</option>
            </select>
          </div>
          <button class="btn btn-primary" @click="saveEdit">保存</button>
        </div>
      </div>
    </div>

    <div v-if="toast" class="toast">{{ toast }}</div>
  </div>
</template>

<style scoped>
.admin-shell {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.admin-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 26px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 16px rgba(156, 120, 200, 0.12);
}
.admin-brand {
  font-size: 20px;
  font-weight: 800;
  color: var(--ink);
}
.admin-top-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.admin-user {
  font-size: 14px;
  color: var(--ink-soft);
  font-weight: 700;
}
.admin-body {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 22px;
  padding: 22px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}
.admin-side {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.admin-nav {
  text-align: left;
  border: none;
  cursor: pointer;
  font-family: inherit;
  font-weight: 700;
  font-size: 15px;
  color: var(--ink);
  background: rgba(255, 255, 255, 0.7);
  padding: 13px 16px;
  border-radius: 14px;
  transition: all 0.15s;
}
.admin-nav:hover {
  background: #fff;
  transform: translateX(3px);
}
.admin-nav.active {
  background: linear-gradient(135deg, var(--pink), var(--lav));
  color: #fff;
  box-shadow: 0 6px 14px rgba(255, 140, 190, 0.4);
}
.admin-main {
  min-width: 0;
}
.admin-h2 {
  font-size: 24px;
  color: var(--ink);
  margin-bottom: 16px;
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.stat-box {
  background: var(--card);
  border-radius: var(--radius);
  padding: 22px;
  text-align: center;
  box-shadow: var(--shadow);
}
.stat-box b {
  display: block;
  font-size: 34px;
  color: var(--ink);
}
.stat-box span {
  font-size: 13px;
  color: var(--ink-soft);
}
.admin-tip {
  margin-top: 18px;
  font-size: 13px;
  color: var(--ink-soft);
  background: rgba(255, 255, 255, 0.6);
  border-radius: 14px;
  padding: 12px 14px;
  line-height: 1.6;
}
.user-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.user-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  background: var(--card);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
}
.user-table th {
  background: #faf4ff;
  color: var(--ink-soft);
  font-size: 13px;
  font-weight: 700;
  padding: 12px 14px;
  text-align: left;
}
.user-table td {
  padding: 11px 14px;
  font-size: 14px;
  color: var(--ink);
  border-top: 1px solid #f3ebfa;
  vertical-align: middle;
}
.user-table tbody tr:hover {
  background: #fff7fb;
}
.u-avatar {
  font-size: 22px;
}
.u-slogan {
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--ink-soft);
}
.u-time {
  color: var(--ink-soft);
  white-space: nowrap;
}
.u-actions {
  white-space: nowrap;
}
.admin-err {
  color: #ff5c8a;
  background: rgba(255, 255, 255, 0.7);
  padding: 16px;
  border-radius: var(--radius);
}
.admin-loading {
  color: var(--ink-soft);
  background: rgba(255, 255, 255, 0.7);
  padding: 20px;
  border-radius: var(--radius);
  text-align: center;
}
.pager {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
  margin-top: 18px;
}
.pg-btn {
  border: none;
  cursor: pointer;
  font-family: inherit;
  font-weight: 700;
  font-size: 14px;
  background: #f6effb;
  color: var(--ink);
  padding: 8px 16px;
  border-radius: 999px;
  transition: all 0.15s;
}
.pg-btn:hover:not(:disabled) {
  background: #efe3f8;
}
.pg-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.pg-info {
  font-size: 14px;
  color: var(--ink-soft);
}
.perm-select {
  padding: 10px 14px;
  border: 2px solid #f0d7ee;
  border-radius: 14px;
  background: #fff;
  color: var(--ink);
  font-size: 14px;
  outline: none;
}
.perm-select:focus {
  border-color: #d9b8f0;
}
.btn-sm {
  padding: 9px 16px;
  font-size: 13px;
  width: auto;
}
@media (max-width: 820px) {
  .admin-body {
    grid-template-columns: 1fr;
  }
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
