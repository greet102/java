<script setup>
import { computed } from 'vue'
import { useStore } from '../store'

const props = defineProps({
  post: { type: Object, required: true },
})

const emit = defineEmits(['edit'])
const { state, toggleLike, deletePost, timeAgo, colorOf } = useStore()

const liked = computed(() => props.post.likes.includes(state.currentUser))
const canDel = computed(() => props.post.author === state.currentUser)
const borderColor = computed(() => colorOf(props.post.id))

function onDel() {
  if (confirm('确定要删除这篇喵？')) deletePost(props.post.id)
}
function onEdit() {
  emit('edit', props.post)
}
</script>

<template>
  <article class="post" :style="{ borderLeftColor: borderColor }">
    <div class="post-top">
      <span class="post-emoji">{{ post.emoji || '🐱' }}</span>
      <div class="post-meta">
        <span class="post-author">{{ post.author }}</span>
        <span class="post-time">{{ timeAgo(post.ts) }}</span>
      </div>
    </div>
    <h3>{{ post.title }}</h3>
    <p>{{ post.body }}</p>
    <div class="post-actions">
      <button class="act-btn" :class="{ liked }" @click="toggleLike(post.id)">
        💖 {{ post.likes.length }}
      </button>
      <button v-if="canDel" class="act-btn" @click="onEdit">✏️ 编辑</button>
      <button v-if="canDel" class="act-btn del" @click="onDel">🗑️ 删除</button>
    </div>
  </article>
</template>
