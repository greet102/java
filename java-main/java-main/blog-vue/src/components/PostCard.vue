<script setup>
import { computed } from 'vue'
import { useStore } from '../store'

const props = defineProps({
  post: { type: Object, required: true },
})

const emit = defineEmits(['edit', 'toast'])
const { state, toggleLike, toggleFavorite, deletePost, timeAgo, colorOf, openProfile } = useStore()

const likes = computed(() => props.post.likes || [])
const favs = computed(() => props.post.favorites || [])

const liked = computed(() => likes.value.includes(state.currentUser))
const faved = computed(() => favs.value.includes(state.currentUser))
const canDel = computed(() => props.post.author === state.currentUser)
const borderColor = computed(() => colorOf(props.post.id))

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
      <button v-if="canDel" class="act-btn" @click="onEdit">✏️ 编辑</button>
      <button v-if="canDel" class="act-btn del" @click="onDel">🗑️ 删除</button>
    </div>
  </article>
</template>
