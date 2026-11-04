<script setup>
import { ref, computed, watch } from 'vue'
import { useStore } from '../store'
import Sidebar from './Sidebar.vue'
import PostCard from './PostCard.vue'
import PostModal from './PostModal.vue'
import ProfileView from './ProfileView.vue'
import UserRow from './UserRow.vue'
import ChatView from './ChatView.vue'

const { state, filteredPosts, logout, addPost, updatePost, openProfile, backHome, openChatList, isAdminUser, openNotif, closeNotif, markAllNotifRead } = useStore()

const showModal = ref(false)
const editing = ref(null) // 非 null 时为编辑模式
const keyword = ref('')
const toast = ref('')
let toastTimer = null

const me = computed(() => state.currentUser)
const titles = { all: '全部文章', mine: '我的文章', liked: '我点赞的', faved: '我的收藏' }

// ---------- 消息通知 ----------
const NOTIF_ICONS = { like: '💖', favorite: '🔖', comment: '💬', follow: '➕' }

function notifText(n) {
  switch (n.type) {
    case 'like':
      return `赞了你的文章《${n.postTitle || '未知文章'}》`
    case 'favorite':
      return `收藏了你的文章《${n.postTitle || '未知文章'}》`
    case 'comment':
      return `评论了你的文章《${n.postTitle || '未知文章'}》：${n.content || ''}`
    case 'follow':
      return '关注了你'
    default:
      return '和你互动了'
  }
}

function fmtNotifTime(v) {
  if (!v) return ''
  const d = new Date(v)
  return isNaN(d.getTime()) ? String(v) : d.toLocaleString('zh-CN', { hour12: false })
}

function toggleNotif() {
  if (state.notifOpen) closeNotif()
  else openNotif()
}

function clickNotifActor(actor) {
  closeNotif()
  openProfile(actor)
}

// “我的关注”展示的是人而不是文章，单独走一个分支
const isFollowingView = computed(() => state.filter === 'following' && state.view === 'home')

const followingList = computed(() => {
  const list = state.myProfile?.followings || []
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return list
  return list.filter(
    (u) =>
      (u.nickname || '').toLowerCase().includes(kw) ||
      u.username.toLowerCase().includes(kw) ||
      (u.slogan || '').toLowerCase().includes(kw)
  )
})

const displayPosts = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return filteredPosts.value
  return filteredPosts.value.filter(
    (p) =>
      p.title.toLowerCase().includes(kw) ||
      p.body.toLowerCase().includes(kw) ||
      p.author.toLowerCase().includes(kw)
  )
})

function showToast(text) {
  toast.value = text
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toast.value = ''), 1600)
}

// 切换栏目时清掉搜索词，避免上一栏的关键词还在过滤新栏目
watch(() => state.filter, () => (keyword.value = ''))

function onLogout() {
  logout()
  showToast('已退出，下次见喵～')
}

// 跳转到后台管理系统（未登录后台会先到登录页）
function openAdmin() {
  location.hash = '#/admin'
}

function openNew() {
  editing.value = null
  showModal.value = true
}

function openEdit(post) {
  editing.value = post
  showModal.value = true
}

async function handleSubmit(payload) {
  const isEdit = !!editing.value
  const res = isEdit ? await updatePost(editing.value.id, payload) : await addPost(payload)
  if (!res.ok) return showToast(res.msg)
  showModal.value = false
  editing.value = null
  showToast(isEdit ? '已更新 ✏️' : '发布成功 🚀')
}
</script>

<template>
  <div class="app-view">
    <header class="topbar">
      <div class="topbar-left">
        <span class="topbar-logo">🐱</span>
        <span class="topbar-name">喵喵博客</span>
      </div>
      <div class="topbar-right">
        <button v-if="isAdminUser" class="btn btn-ghost" @click="openAdmin">🛠 后台管理</button>
        <button class="btn btn-ghost" @click="openNew">✏️ 写新文章</button>
        <button class="btn btn-ghost dm-btn" @click="openChatList">
          💌 私信
          <span v-if="state.unread" class="dm-badge">{{ state.unread }}</span>
        </button>
        <div class="notif-wrap">
          <button class="btn btn-ghost dm-btn" @click="toggleNotif">
            🔔 通知
            <span v-if="state.notifUnread" class="dm-badge">{{ state.notifUnread }}</span>
          </button>
          <!-- 通知下拉面板 -->
          <div v-if="state.notifOpen" class="notif-panel">
            <div class="notif-head">
              <span>🔔 消息通知</span>
              <button class="notif-readall" @click="markAllNotifRead">全部已读</button>
            </div>
            <div class="notif-list">
              <div
                v-for="n in state.notifications"
                :key="n.id"
                class="notif-item"
                :class="{ unread: !n.isRead }"
              >
                <span class="notif-icon">{{ NOTIF_ICONS[n.type] || '✨' }}</span>
                <div class="notif-main">
                  <span class="notif-actor link" @click="clickNotifActor(n.actor)">{{ n.actor }}</span>
                  <span class="notif-text">{{ notifText(n) }}</span>
                  <span class="notif-time">{{ fmtNotifTime(n.createdAt) }}</span>
                </div>
              </div>
              <div v-if="!state.notifications.length" class="notif-empty">
                还没有通知，去和别人互动吧～
              </div>
            </div>
          </div>
        </div>
        <div class="avatar clickable" @click="openProfile(me)">
          {{ state.myProfile?.avatar || me?.[0] || '🐾' }}
        </div>
        <button class="btn btn-ghost" @click="onLogout">退出</button>
      </div>
    </header>

    <div class="layout">
      <Sidebar :user="me" :filter="state.filter" />

      <main class="content">
        <!-- 个人主页视图 -->
        <ProfileView
          v-if="state.view === 'profile'"
          :username="state.profileUser"
          @back="backHome"
          @edit="openEdit"
          @toast="showToast"
        />

        <!-- 我的关注：单独一栏，列出关注到的博主 -->
        <template v-else-if="isFollowingView">
          <div class="feed-head">
            <h2>💞 我的关注</h2>
            <span class="feed-count">{{ followingList.length }} 位小伙伴</span>
            <input class="search-input" v-model="keyword" placeholder="🔍 搜索昵称 / 用户名 / 标语" />
          </div>

          <div v-if="followingList.length" class="profile-list">
            <UserRow v-for="u in followingList" :key="u.username" :user="u" @toast="showToast" />
          </div>
          <div v-else class="empty">
            <span class="big">💞</span>
            {{ keyword.trim() ? '没有匹配的小伙伴，换个词试试～' : '还没有关注别人，去广场发现有趣的人吧～' }}
          </div>
        </template>

        <!-- 文章广场视图 -->
        <template v-else>
          <div class="feed-head">
            <h2>{{ titles[state.filter] }}</h2>
            <span class="feed-count">{{ displayPosts.length }} 篇</span>
            <input class="search-input" v-model="keyword" placeholder="🔍 搜索标题 / 内容 / 作者" />
          </div>

          <div v-if="displayPosts.length" class="feed">
            <PostCard v-for="p in displayPosts" :key="p.id" :post="p" @edit="openEdit" @toast="showToast" />
          </div>
          <div v-else class="empty">
            <span class="big">🐾</span>这里还空空的，去写点什么吧～
          </div>
        </template>
      </main>
    </div>

    <PostModal v-if="showModal" :post="editing" @close="showModal = false" @submit="handleSubmit" />

    <ChatView v-if="state.chatOpen" />

    <div v-if="toast" class="toast">{{ toast }}</div>
  </div>
</template>

<style scoped>
.search-input {
  margin-left: auto;
  font-family: inherit;
  font-size: 14px;
  color: var(--ink);
  border: 2px solid #f0e6f7;
  border-radius: 999px;
  padding: 9px 16px;
  background: #fdfbff;
  width: 220px;
  max-width: 50vw;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.search-input:focus {
  outline: none;
  border-color: var(--pink);
  box-shadow: 0 0 0 4px rgba(255, 158, 196, 0.18);
}
@media (max-width: 560px) {
  .feed-head { flex-wrap: wrap; }
  .search-input { margin-left: 0; width: 100%; max-width: 100%; }
}

.dm-btn {
  position: relative;
}
.dm-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ff5c8a;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: grid;
  place-items: center;
}

/* ---------- 消息通知面板 ---------- */
.notif-wrap {
  position: relative;
}
.notif-panel {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 330px;
  max-height: 420px;
  display: flex;
  flex-direction: column;
  background: #fffdf9;
  border: 2px solid #f0d7ee;
  border-radius: 18px;
  box-shadow: 0 12px 32px rgba(150, 110, 200, 0.18);
  z-index: 60;
  overflow: hidden;
}
.notif-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  font-weight: 700;
  color: var(--ink);
  border-bottom: 2px dashed #f0d7ee;
}
.notif-readall {
  border: none;
  background: none;
  cursor: pointer;
  font-size: 12px;
  color: #a06cd5;
  font-family: inherit;
  font-weight: 700;
  padding: 0;
}
.notif-readall:hover {
  color: var(--pink);
}
.notif-list {
  overflow-y: auto;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.notif-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 9px 10px;
  border-radius: 12px;
  background: #fff;
  transition: background 0.15s;
}
.notif-item:hover {
  background: #faf3ff;
}
.notif-item.unread {
  background: #fdf1f7;
}
.notif-item.unread .notif-text {
  font-weight: 700;
}
.notif-icon {
  width: 30px;
  height: 30px;
  flex: none;
  display: grid;
  place-items: center;
  background: #f6effb;
  border-radius: 50%;
  font-size: 15px;
}
.notif-main {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  line-height: 1.55;
  color: var(--ink);
}
.notif-actor {
  font-weight: 700;
  margin-right: 4px;
}
.notif-text {
  word-break: break-word;
}
.notif-time {
  display: block;
  font-size: 11px;
  color: #b3a8c9;
  margin-top: 2px;
}
.notif-empty {
  text-align: center;
  color: var(--ink-soft);
  font-size: 13px;
  padding: 24px 0;
}
</style>
