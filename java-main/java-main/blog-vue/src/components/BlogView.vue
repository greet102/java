<script setup>
import { ref, computed } from 'vue'
import { useStore } from '../store'
import Sidebar from './Sidebar.vue'
import PostCard from './PostCard.vue'
import PostModal from './PostModal.vue'

const { state, filteredPosts, logout, addPost, updatePost } = useStore()

const showModal = ref(false)
const editing = ref(null) // 非 null 时为编辑模式
const keyword = ref('')
const toast = ref('')
let toastTimer = null

const me = computed(() => state.currentUser)
const titles = { all: '全部文章', mine: '我的文章', liked: '我点赞的' }

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
  toastTimer = setTimeout(() => (toast.value = ''), 1800)
}

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
        <div class="avatar">{{ me ? me[0].toUpperCase() : '🐾' }}</div>
        <button class="btn btn-ghost" @click="onLogout">退出</button>
      </div>
    </header>

    <div class="layout">
      <Sidebar :user="me" :filter="state.filter" />

      <main class="content">
        <div class="feed-head">
          <h2>{{ titles[state.filter] }}</h2>
          <span class="feed-count">{{ displayPosts.length }} 篇</span>
          <input class="search-input" v-model="keyword" placeholder="🔍 搜索标题 / 内容 / 作者" />
        </div>

        <div v-if="displayPosts.length" class="feed">
          <PostCard v-for="p in displayPosts" :key="p.id" :post="p" @edit="openEdit" />
        </div>
        <div v-else class="empty">
          <span class="big">🐾</span>这里还空空的，去写点什么吧～
        </div>
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
