<script setup>
import { useStore } from '../store'

const props = defineProps({
  user: { type: String, default: '' },
  filter: { type: String, default: 'all' },
})

const { setFilter } = useStore()

const navs = [
  { key: 'all', label: '🌈 全部文章' },
  { key: 'mine', label: '⭐ 我的文章' },
  { key: 'liked', label: '💖 我点赞的' },
]
</script>

<template>
  <aside class="sidebar">
    <div class="profile-card">
      <div class="profile-avatar">{{ user ? user[0].toUpperCase() : '🐾' }}</div>
      <h3>{{ user }}</h3>
      <p class="profile-bio">今天也要元气满满！</p>
    </div>

    <nav class="side-nav">
      <button
        v-for="n in navs"
        :key="n.key"
        class="nav-item"
        :class="{ active: filter === n.key }"
        @click="setFilter(n.key)"
      >
        {{ n.label }}
      </button>
    </nav>

    <div class="side-tip">
      💡 小提示：文章都保存在服务器数据库里，换浏览器也不会丢哦～
    </div>
  </aside>
</template>
