<script setup>
import { ref, computed, watch } from 'vue'
import { useStore } from '../store'
import Sidebar from './Sidebar.vue'
import PostCard from './PostCard.vue'
import PostModal from './PostModal.vue'
import ProfileView from './ProfileView.vue'
import UserRow from './UserRow.vue'

const { state, filteredPosts, logout, addPost, updatePost, openProfile, backHome } = useStore()

const showModal = ref(false)
const editing = ref(null) // 非 null 时为编辑模式
const keyword = ref('')
const toast = ref('')
let toastTimer = null

const me = computed(() => state.currentUser)
const titles = { all: '全部文章', mine: '我的文章', liked: '我点赞的', faved: '我的收藏' }

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
        <button class="btn btn-ghost" @click="openNew">✏️ 写新文章</button>
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
</style>
