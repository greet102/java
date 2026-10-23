// ===================== 喵喵博客 · 状态管理 =====================
// 已对接 Spring Boot 后端（MySQL + MyBatis-Plus）：
//   - 登录态保存在 localStorage + 后端 HttpSession（Cookie）
//   - 文章 / 用户 / 点赞 / 收藏 / 关注 数据全部存放在服务器数据库中
// 仅用于演示，密码未加密，请勿用于真实敏感账号。
import { reactive, computed } from 'vue'

const K_CUR = 'mewblog_current'

const EMOJIS = ['🐱', '🌈', '⭐', '🍰', '🌸', '🎈', '🐻', '💖', '🌟', '🍓']
const COLORS = ['#ff9ec4', '#ffd6a5', '#b8f2d8', '#cdb4f6', '#a0e7ff']

const state = reactive({
  currentUser: localStorage.getItem(K_CUR) || '',
  posts: [],
  filter: 'all', // all | mine | liked | faved
  view: 'home', // home | profile
  profileUser: '', // 正在查看的个人主页用户名
  profile: null, // 主页数据（可能是别人也可能是自己）
  myProfile: null, // 自己的资料，用于侧边栏展示

  // 私信相关
  conversations: [], // 会话列表
  unread: 0, // 未读私信数
  chatOpen: false, // 私信窗口是否打开
  chatWith: '', // 当前正在聊的人
  thread: [], // 当前会话的消息列表
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

// ---------- 小工具：兼容后端还没升级的旧数据 ----------
const likesOf = (p) => p.likes || []
const favsOf = (p) => p.favorites || []

// 拉取全部文章
async function fetchPosts() {
  const res = await api('/posts')
  if (res.ok) state.posts = res.data || []
}

// 拉取自己的资料（侧边栏头像、昵称、统计用）
async function fetchMyProfile() {
  if (!state.currentUser) {
    state.myProfile = null
    return
  }
  const res = await api(`/users/${encodeURIComponent(state.currentUser)}/profile`)
  if (res.ok) state.myProfile = res.data
}

const filteredPosts = computed(() => {
  let list = state.posts.slice().sort((a, b) => b.ts - a.ts)
  if (state.filter === 'mine') list = list.filter((p) => p.author === state.currentUser)
  else if (state.filter === 'liked')
    list = list.filter((p) => likesOf(p).includes(state.currentUser))
  else if (state.filter === 'faved')
    list = list.filter((p) => favsOf(p).includes(state.currentUser))
  // “我的关注”展示的是用户列表，文章列表返回空即可（由页面单独渲染）
  else if (state.filter === 'following') list = []
  return list
})

// ---------- 私信 ----------
async function fetchUnread() {
  if (!state.currentUser) {
    state.unread = 0
    return
  }
  const res = await api('/messages/unread')
  if (res.ok) state.unread = res.data || 0
}

async function fetchConversations() {
  if (!state.currentUser) {
    state.conversations = []
    return
  }
  const res = await api('/messages/conversations')
  if (res.ok) state.conversations = res.data || []
}

async function fetchThread(username) {
  if (!username) return
  const res = await api(`/messages/${encodeURIComponent(username)}`)
  if (res.ok) state.thread = res.data || []
}

/** 打开与某人的私信窗口（对方不在会话列表里也能开，等于新建会话） */
async function openChat(username) {
  if (!username || !state.currentUser) return
  if (username === state.currentUser) return
  state.chatWith = username
  state.chatOpen = true
  state.thread = []
  await Promise.all([fetchThread(username), fetchConversations()])
  await fetchUnread()
}

function closeChat() {
  state.chatOpen = false
  state.chatWith = ''
  state.thread = []
  fetchConversations()
  fetchUnread()
}

/** 发一条私信 */
async function sendDM(username, content) {
  const text = (content || '').trim()
  if (!text) return false
  const res = await api(`/messages/${encodeURIComponent(username)}`, {
    method: 'POST',
    body: { content: text },
  })
  if (!res.ok) return false
  await fetchThread(username)
  await fetchConversations()
  return true
}

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
  await fetchMyProfile()
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
  await fetchMyProfile()
  return res
}

async function logout() {
  await api('/auth/logout', { method: 'POST' })
  state.currentUser = ''
  localStorage.removeItem(K_CUR)
  state.posts = []
  state.myProfile = null
  state.profile = null
  state.profileUser = ''
  state.view = 'home'
  state.filter = 'all'
  syncHash()
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
  if (res.ok) {
    await fetchPosts()
    await fetchMyProfile()
    // 主页若正开着，顺带刷新
    if (state.view === 'profile') await fetchProfile(state.profileUser)
  }
  return res
}

async function toggleLike(id) {
  if (!state.currentUser) return
  const res = await api(`/posts/${id}/like`, { method: 'POST' })
  if (res.ok) {
    // 就地更新点赞列表，避免整页刷新
    const p = state.posts.find((x) => x.id === id)
    if (p) p.likes = res.data
    if (state.view === 'profile') await fetchProfile(state.profileUser)
  }
}

// 收藏 / 取消收藏
async function toggleFavorite(id) {
  if (!state.currentUser) return
  const res = await api(`/posts/${id}/favorite`, { method: 'POST' })
  if (res.ok) {
    const p = state.posts.find((x) => x.id === id)
    if (p) p.favorites = res.data
    await fetchMyProfile()
    if (state.view === 'profile') await fetchProfile(state.profileUser)
    return true
  }
  return false
}

async function updatePost(id, { title, body, emoji }) {
  if (!title || !title.trim() || !body || !body.trim())
    return { ok: false, msg: '标题和正文都要写哦～' }
  const res = await api(`/posts/${id}`, {
    method: 'PUT',
    body: { title: title.trim(), body: body.trim(), emoji },
  })
  if (res.ok) {
    await fetchPosts()
    if (state.view === 'profile') await fetchProfile(state.profileUser)
  }
  return res
}

async function deletePost(id) {
  const res = await api(`/posts/${id}`, { method: 'DELETE' })
  if (res.ok) {
    await fetchPosts()
    await fetchMyProfile()
    if (state.view === 'profile') await fetchProfile(state.profileUser)
  }
}

// ---------- 关注 / 个人主页 ----------
// 关注后：无论看的是谁的主页，列表与计数都刷新一次，保证一致
async function toggleFollow(username) {
  if (!state.currentUser) return false
  const res = await api(`/users/${encodeURIComponent(username)}/follow`, { method: 'POST' })
  if (!res.ok) return false
  // 就地更新主页上的关注状态与粉丝数，避免等待请求时空窗
  if (state.profile && state.profile.username === username) {
    state.profile.following = res.data.following
    state.profile.followerCount = res.data.followerCount
  }
  // 当前正看着某个主页时，它的关注/粉丝列表可能也因此增删，拉一次最新的
  if (state.view === 'profile' && state.profileUser) await fetchProfile(state.profileUser)
  await fetchMyProfile()
  return true
}

async function fetchProfile(username) {
  if (!username) return null
  const res = await api(`/users/${encodeURIComponent(username)}/profile`)
  if (res.ok) {
    state.profile = res.data
    if (username === state.currentUser) state.myProfile = res.data
    return res.data
  }
  return null
}

async function updateProfile(form) {
  const res = await api('/users/me/profile', { method: 'PUT', body: form })
  if (res.ok) {
    await fetchMyProfile()
    if (state.view === 'profile') await fetchProfile(state.profileUser)
  }
  return res
}

// 打开某人的个人主页（同时写进地址栏 hash，支持刷新与直接分享链接）
async function openProfile(username) {
  if (!username) return
  if (state.view === 'profile' && state.profileUser === username && state.profile) return
  state.profileUser = username
  state.view = 'profile'
  state.profile = null
  syncHash()
  await fetchProfile(username)
}

function backHome() {
  state.view = 'home'
  state.profile = null
  state.profileUser = ''
  syncHash()
}

// ---------- 极简 hash 路由：#/ 首页，#/u/用户名 个人主页 ----------
function hashOf() {
  return state.view === 'profile' && state.profileUser
    ? '#/u/' + encodeURIComponent(state.profileUser)
    : '#/'
}

function syncHash() {
  if (typeof location === 'undefined') return
  const target = hashOf()
  if (location.hash !== target) location.hash = target
}

function applyHash() {
  if (typeof location === 'undefined' || !state.currentUser) return
  const m = /^#\/u\/(.+)$/.exec(location.hash)
  if (m) {
    openProfile(decodeURIComponent(m[1]))
  } else if (state.view === 'profile') {
    backHome()
  }
}

if (typeof window !== 'undefined') {
  window.addEventListener('hashchange', applyHash)
}

function setFilter(f) {
  state.filter = f
  if (f !== 'profile') {
    state.view = 'home'
    if (state.profileUser) {
      state.profile = null
      state.profileUser = ''
    }
    syncHash()
  }
  // 切到“我的关注”前刷新一次资料，保证列表是最新的
  if (f === 'following') fetchMyProfile()
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

// 应用启动时恢复会话：后端会话还在则拉取文章与资料，否则清除本地记录
if (state.currentUser) {
  api('/auth/me').then((res) => {
    if (res.ok && res.data) {
      fetchPosts()
      fetchMyProfile()
      if (typeof location !== 'undefined' && /^#\/u\/.+$/.test(location.hash)) applyHash()
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
    fetchMyProfile,
    fetchProfile,
    EMOJIS,
    register,
    login,
    logout,
    addPost,
    updatePost,
    toggleLike,
    toggleFavorite,
    deletePost,
    setFilter,
    toggleFollow,
    openProfile,
    updateProfile,
    backHome,
    timeAgo,
    colorOf,
    esc,
  }
}
