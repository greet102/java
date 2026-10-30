<script setup>
import { computed } from 'vue'
import { useStore } from '../store'

const props = defineProps({
  user: { type: Object, required: true },
})
const emit = defineEmits(['toast'])

const { state, toggleFollow, openProfile, openChat } = useStore()

const isSelf = computed(() => state.currentUser === props.user.username)
const avatar = computed(() => props.user.avatar || '🐱')
const nickname = computed(() => props.user.nickname || props.user.username)
// 是否已关注：来自后端 UserSummary.isFollowing，列表刷新后自动同步
const followed = computed(() => !!props.user.isFollowing)

async function onFollow() {
  if (!state.currentUser) {
    emit('toast', '请先登录喵～')
    return
  }
  const was = followed.value
  const ok = await toggleFollow(props.user.username)
  emit('toast', ok ? (was ? '已取消关注' : '已关注 💞') : '操作失败，请稍后重试喵～')
}

async function onDM() {
  if (!state.currentUser) {
    emit('toast', '请先登录喵～')
    return
  }
  if (!followed.value) {
    emit('toast', '关注后才能给 TA 发私信喵～')
    return
  }
  openChat(props.user.username)
}
</script>

<template>
  <div class="user-row">
    <div class="ur-avatar clickable" @click="openProfile(user.username)">{{ avatar }}</div>
    <div class="ur-info clickable" @click="openProfile(user.username)">
      <div class="ur-name">{{ nickname }}</div>
      <div class="ur-sub">@{{ user.username }} · {{ user.slogan || '这个人很懒，什么也没写～' }}</div>
    </div>
    <button v-if="!isSelf" class="ur-dm" :class="{ disabled: !followed }" :title="followed ? '发私信' : '关注后才能私信'" @click="onDM">💌</button>
    <button v-if="!isSelf" class="ur-follow" :class="{ followed }" @click="onFollow">
      {{ followed ? '✓ 已关注' : '＋ 关注' }}
    </button>
    <span v-else class="ur-self">你</span>
  </div>
</template>

<style scoped>
.user-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fdfbff;
  border: 2px solid #f0e6f7;
  border-radius: 16px;
}
.ur-avatar {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  font-size: 24px;
  background: #fff0f6;
  border-radius: 50%;
  flex: 0 0 auto;
}
.ur-info {
  flex: 1 1 auto;
  min-width: 0;
}
.ur-name {
  font-weight: 700;
  color: var(--ink, #4a3b52);
}
.ur-sub {
  font-size: 13px;
  color: #9b8aa6;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ur-follow {
  flex: 0 0 auto;
  border: none;
  background: var(--pink, #ff9ec4);
  color: #fff;
  font-weight: 700;
  border-radius: 999px;
  padding: 8px 18px;
  cursor: pointer;
  transition: transform 0.12s, box-shadow 0.12s;
}
.ur-follow:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(255, 158, 196, 0.4);
}
.ur-follow.followed {
  background: #e9dcef;
  color: #8a7a95;
}
.ur-dm {
  flex: 0 0 auto;
  border: none;
  background: #fff0f6;
  color: #c98bb0;
  font-size: 16px;
  border-radius: 50%;
  width: 38px;
  height: 38px;
  cursor: pointer;
  transition: transform 0.12s, background 0.12s;
}
.ur-dm:hover {
  transform: translateY(-1px);
  background: #ffe3ef;
}
.ur-dm.disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
.ur-dm.disabled:hover {
  transform: none;
  background: #fff0f6;
}
.ur-self {
  flex: 0 0 auto;
  color: #b9a7c4;
  font-size: 13px;
}
</style>
