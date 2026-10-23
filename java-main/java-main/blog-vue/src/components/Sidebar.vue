<script setup>
import { computed } from 'vue'
import { useStore } from '../store'

const props = defineProps({
  user: { type: String, default: '' },
  filter: { type: String, default: 'all' },
})
const { setFilter, state, openProfile } = useStore()

const profile = computed(() => state.myProfile)
const avatar = computed(() => profile.value?.avatar || '🐱')
const nickname = computed(() => profile.value?.nickname || props.user || '')
const slogan = computed(() => profile.value?.slogan || '今天也要元气满满！')

const favCount = computed(() => state.posts.filter((p) => (p.favorites || []).includes(props.user)).length)
const postCount = computed(() => state.posts.filter((p) => p.author === props.user).length)

const followingCount = computed(() => state.myProfile?.followingCount ?? 0)

const navs = [
  { key: 'all', label: '🌈 全部文章' },
  { key: 'mine', label: '⭐ 我的文章' },
  { key: 'liked', label: '💖 我点赞的' },
  { key: 'faved', label: '🔖 我的收藏' },
  { key: 'following', label: '💞 我的关注' },
]

function pick(key) {
  setFilter(key)
}
</script>

<template>
  <aside class="sidebar">
    <div class="profile-card">
      <div class="profile-avatar clickable" @click="openProfile(user)">{{ avatar }}</div>
      <h3 class="clickable" @click="openProfile(user)">{{ nickname }}</h3>
      <p class="profile-bio">「{{ slogan }}」</p>

      <div class="mini-stats">
        <span><b>{{ postCount }}</b>文章</span>
        <span><b>{{ favCount }}</b>收藏</span>
        <span><b>{{ followingCount }}</b>关注</span>
        <span><b>{{ profile?.followerCount ?? 0 }}</b>粉丝</span>
      </div>

      <div class="profile-links">
        <button class="link-btn" @click="openProfile(user)">🏠 我的主页</button>
        <button class="link-btn" @click="pick('following')">
          💞 我的关注 <em>{{ followingCount }}</em>
        </button>
      </div>
    </div>

    <nav class="side-nav">
      <button
        v-for="n in navs"
        :key="n.key"
        class="nav-item"
        :class="{ active: filter === n.key && state.view === 'home' }"
        @click="pick(n.key)"
      >
        {{ n.label }}
      </button>
    </nav>

    <div class="side-tip">
      💡 小提示：点赞、收藏、关注都保存在服务器数据库里，换浏览器也不会丢哦～
    </div>
  </aside>
</template>
