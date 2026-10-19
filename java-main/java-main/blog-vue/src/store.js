// ===================== 喵喵博客 · 状态管理 =====================
// 已对接 Spring Boot 后端（MySQL + MyBatis-Plus）：
//   - 登录态保存在 localStorage + 后端 HttpSession（Cookie）
//   - 文章 / 用户数据全部存放在服务器数据库中
// 仅用于演示，密码未加密，请勿用于真实敏感账号。
import { reactive, computed } from 'vue'

const K_CUR = 'mewblog_current'

const EMOJIS = ['🐱', '🌈', '⭐', '🍰', '🌸', '🎈', '🐻', '💖', '🌟', '🍓']
const COLORS = ['#ff9ec4', '#ffd6a5', '#b8f2d8', '#cdb4f6', '#a0e7ff']

const state = reactive({
  currentUser: localStorage.getItem(K_CUR) || '',
  posts: [],
  filter: 'all',
})

const isLoggedIn = computed(() => !!state.currentUser)

// ---------- 后端请求封装：统一返回 { ok, msg, data } ----------
async function api(path, { method = 'GET', body } = {}) {
  try {
    const res = await fetch('/api' + path, {
      method,
      headers: body ? { 'Content-Type': 'application/json' } : undefined,
      body: body ? JSON.stringify(body) : undefined,
    })
    return await res.json()
  } catch (e) {
    return { ok: false, msg: '网络异常，请确认后端已启动喵～' }
  }
}

// 拉取全部文章
async function fetchPosts() {
  const res = await api('/posts')
  if (res.ok) state.posts = res.data
}

const filteredPosts = computed(() => {
  let list = state.posts.slice().sort((a, b) => b.ts - a.ts)
  if (state.filter === 'mine') list = list.filter((p) => p.author === state.currentUser)
  else if (state.filter === 'liked') list = list.filter((p) => p.likes.includes(state.currentUser))
  return list
})

// ---------- 认证 ----------
async function register(user, pass) {
  user = (user || '').trim()
  if (!user || !pass) return { ok: false, msg: '用户名和密码都不能空哦～' }
  if (pass.length < 4) return { ok: false, msg: '密码至少要 4 位喵' }
  const res = await api('/auth/register', {
    method: 'POST',
    body: { username: user, password: pass },
  })
  if (!res.ok) return res
  state.currentUser = user // 注册后自动登录
  localStorage.setItem(K_CUR, user)
  await fetchPosts()
  return res
}

async function login(user, pass) {
  user = (user || '').trim()
  const res = await api('/auth/login', {
    method: 'POST',
    body: { username: user, password: pass },
  })
  if (!res.ok) return res
  state.currentUser = user
  localStorage.setItem(K_CUR, user)
  await fetchPosts()
  return res
}

async function logout() {
  await api('/auth/logout', { method: 'POST' })
  state.currentUser = ''
  localStorage.removeItem(K_CUR)
  state.posts = []
}

// ---------- 文章 ----------
async function addPost({ title, body, emoji }) {
  title = (title || '').trim()
  body = (body || '').trim()
  if (!title || !body) return { ok: false, msg: '标题和正文都要写哦～' }
  const res = await api('/posts', {
    method: 'POST',
    body: { title, body, emoji: emoji || '🐱' },
  })
  if (res.ok) await fetchPosts()
  return res
}

async function toggleLike(id) {
  if (!state.currentUser) return
  const res = await api(`/posts/${id}/like`, { method: 'POST' })
  if (res.ok) {
    // 就地更新点赞列表，避免整页刷新
    const p = state.posts.find((x) => x.id === id)
    if (p) p.likes = res.data
  }
}

async function updatePost(id, { title, body, emoji }) {
  if (!title || !title.trim() || !body || !body.trim())
    return { ok: false, msg: '标题和正文都要写哦～' }
  const res = await api(`/posts/${id}`, {
    method: 'PUT',
    body: { title: title.trim(), body: body.trim(), emoji },
  })
  if (res.ok) await fetchPosts()
  return res
}

async function deletePost(id) {
  const res = await api(`/posts/${id}`, { method: 'DELETE' })
  if (res.ok) await fetchPosts()
}

function setFilter(f) {
  state.filter = f
}

function timeAgo(ts) {
  const d = Math.floor((Date.now() - ts) / 1000)
  if (d < 60) return '刚刚'
  if (d < 3600) return Math.floor(d / 60) + ' 分钟前'
  if (d < 86400) return Math.floor(d / 3600) + ' 小时前'
  if (d < 86400 * 30) return Math.floor(d / 86400) + ' 天前'
  return new Date(ts).toLocaleDateString('zh-CN')
}

function colorOf(id) {
  return COLORS[id % COLORS.length]
}

function esc(s) {
  return String(s).replace(/[&<>"']/g, (m) =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m])
  )
}

// 应用启动时恢复会话：后端会话还在则拉取文章，否则清除本地记录
if (state.currentUser) {
  api('/auth/me').then((res) => {
    if (res.ok && res.data) {
      fetchPosts()
    } else {
      state.currentUser = ''
      localStorage.removeItem(K_CUR)
    }
  })
}

export function useStore() {
  return {
    state,
    isLoggedIn,
    filteredPosts,
    fetchPosts,
    EMOJIS,
    register,
    login,
    logout,
    addPost,
    updatePost,
    toggleLike,
    deletePost,
    setFilter,
    timeAgo,
    colorOf,
    esc,
  }
}
