<script setup>
import { ref, computed } from 'vue'
import { useStore } from '../store'

const props = defineProps({
  post: { type: Object, required: true },
})

const emit = defineEmits(['edit', 'toast'])
const {
  state,
  toggleLike,
  toggleFavorite,
  deletePost,
  timeAgo,
  colorOf,
  openProfile,
  fetchComments,
  addComment,
  deleteComment,
} = useStore()

const likes = computed(() => props.post.likes || [])
const favs = computed(() => props.post.favorites || [])

const liked = computed(() => likes.value.includes(state.currentUser))
const faved = computed(() => favs.value.includes(state.currentUser))
const canDel = computed(() => props.post.author === state.currentUser)
const borderColor = computed(() => colorOf(props.post.id))

// ---------- 评论区 ----------
const showComments = ref(false)
const comments = ref([])
const commentText = ref('')
const loadingComments = ref(false)
const submitting = ref(false)

function canDelComment(c) {
  return c.username === state.currentUser || state.isAdmin
}

function fmtCommentTime(v) {
  if (!v) return ''
  const d = new Date(v)
  return isNaN(d.getTime()) ? String(v) : d.toLocaleString('zh-CN', { hour12: false })
}

async function loadComments() {
  loadingComments.value = true
  comments.value = await fetchComments(props.post.id)
  loadingComments.value = false
}

async function toggleComments() {
  showComments.value = !showComments.value
  if (showComments.value && !comments.value.length) await loadComments()
}

async function submitComment() {
  const text = commentText.value.trim()
  if (!text || submitting.value) return
  submitting.value = true
  const res = await addComment(props.post.id, text)
  submitting.value = false
  if (!res.ok) return emit('toast', res.msg || '评论失败，请稍后重试喵～')
  commentText.value = ''
  await loadComments()
}

async function removeComment(c) {
  if (!confirm('确定删除这条评论吗？')) return
  const res = await deleteComment(props.post.id, c.id)
  if (!res.ok) return emit('toast', res.msg || '删除失败，请稍后重试喵～')
  await loadComments()
}

function onDel() {
  if (confirm('确定要删除这篇喵？')) deletePost(props.post.id)
}
function onEdit() {
  emit('edit', props.post)
}
async function onFav() {
  const ok = await toggleFavorite(props.post.id)
  emit('toast', ok ? (faved.value ? '已加入收藏 🔖' : '已取消收藏') : '操作失败，请稍后重试喵～')
}
</script>

<template>
  <article class="post" :style="{ borderLeftColor: borderColor }">
    <div class="post-top">
      <span class="post-emoji">{{ post.emoji || '🐱' }}</span>
      <div class="post-meta">
        <span class="post-author link" @click="openProfile(post.author)">{{ post.author }}</span>
        <span class="post-time">{{ timeAgo(post.ts) }}</span>
      </div>
      <button
        v-if="post.author !== state.currentUser"
        class="follow-mini"
        @click="openProfile(post.author)"
      >
        看主页 ›
      </button>
    </div>
    <h3>{{ post.title }}</h3>
    <p>{{ post.body }}</p>
    <div class="post-actions">
      <button class="act-btn" :class="{ liked }" @click="toggleLike(post.id)">
        💖 {{ likes.length }}
      </button>
      <button class="act-btn" :class="{ faved }" @click="onFav">🔖 {{ favs.length }}</button>
      <button class="act-btn" :class="{ active: showComments }" @click="toggleComments">
        💬 {{ post.commentCount || 0 }}
      </button>
      <button v-if="canDel" class="act-btn" @click="onEdit">✏️ 编辑</button>
      <button v-if="canDel" class="act-btn del" @click="onDel">🗑️ 删除</button>
    </div>

    <!-- 评论区 -->
    <div v-if="showComments" class="comment-box">
      <div class="comment-input">
        <input
          v-model="commentText"
          @keyup.enter="submitComment"
          maxlength="500"
          placeholder="友善评论，温暖喵喵博客～"
        />
        <button class="btn btn-primary btn-sm" :disabled="submitting" @click="submitComment">
          {{ submitting ? '发表中…' : '发表' }}
        </button>
      </div>
      <div v-if="loadingComments" class="comment-loading">加载评论中…</div>
      <div v-else-if="!comments.length" class="comment-empty">
        还没有评论，来抢沙发喵～
      </div>
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <span class="comment-avatar">{{ c.avatar || '🐱' }}</span>
        <div class="comment-main">
          <div class="comment-head">
            <span class="comment-author link" @click="openProfile(c.username)">
              {{ c.nickname || c.username }}
            </span>
            <span class="comment-time">{{ fmtCommentTime(c.createdAt) }}</span>
            <button v-if="canDelComment(c)" class="comment-del" @click="removeComment(c)">
              删除
            </button>
          </div>
          <p class="comment-content">{{ c.content }}</p>
        </div>
      </div>
    </div>
  </article>
</template>
