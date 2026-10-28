<script setup>
import { computed, watch, onMounted, ref } from 'vue'
import { useStore } from '../store'
import PostCard from './PostCard.vue'
import UserRow from './UserRow.vue'

const props = defineProps({
  username: { type: String, required: true },
})
const emit = defineEmits(['back', 'edit', 'toast'])

const { state, fetchProfile, toggleFollow, openProfile, updateProfile, timeAgo } = useStore()

const profile = computed(() => state.profile)
const isSelf = computed(() => !!state.currentUser && state.currentUser === props.username)
const posts = computed(() =>
  (state.posts || [])
    .filter((p) => p.author === props.username)
    .sort((a, b) => b.ts - a.ts)
)
const followings = computed(() => profile.value?.followings || [])
const followerCount = computed(() => profile.value?.followerCount ?? 0)
const followingCount = computed(() => profile.value?.followingCount ?? 0)
const isFollowing = computed(() => !!profile.value?.isFollowing)

// 进入时若主页数据尚未加载或不是当前用户，主动拉一次
async function ensure() {
  if (!profile.value || profile.value.username !== props.username) {
    await fetchProfile(props.username)
  }
}
onMounted(ensure)
watch(() => props.username, ensure)

async function onFollow() {
  if (!state.currentUser) {
    emit('toast', '请先登录喵～')
    return
  }
  const ok = await toggleFollow(props.username)
  emit('toast', ok ? (isFollowing.value ? '已关注 💞' : '已取消关注') : '操作失败，请稍后重试喵～')
}

// ---- 编辑资料 ----
const editing = ref(false)
const form = ref({ nickname: '', slogan: '', bio: '', avatar: '', location: '' })
function startEdit() {
  const p = profile.value || {}
  form.value = {
    nickname: p.nickname || '',
    slogan: p.slogan || '',
    bio: p.bio || '',
    avatar: p.avatar || '',
    location: p.location || '',
  }
  editing.value = true
}
async function saveEdit() {
  const res = await updateProfile({ ...form.value })
  if (res.ok) {
    editing.value = false
    emit('toast', '资料已更新 ✨')
  } else {
    emit('toast', res.msg || '更新失败')
  }
}
</script>

<template>
  <div class="profile-view">
    <div class="pv-head">
      <button class="pv-back" @click="emit('back')">‹ 返回</button>
      <div class="pv-avatar">{{ profile?.avatar || '🐱' }}</div>
      <h2 class="pv-name">{{ profile?.nickname || props.username }}</h2>
      <div class="pv-username">@{{ props.username }}</div>
      <div class="pv-slogan">「{{ profile?.slogan || '今天也要元气满满！' }}」</div>

      <div class="pv-actions">
        <template v-if="isSelf">
          <button class="pv-btn" @click="startEdit">✏️ 编辑资料</button>
        </template>
        <template v-else>
          <button class="pv-btn" :class="{ followed: isFollowing }" @click="onFollow">
            {{ isFollowing ? '✓ 已关注' : '＋ 关注' }}
          </button>
        </template>
      </div>

      <div class="pv-stats">
        <span><b>{{ posts.length }}</b> 文章</span>
        <span><b>{{ followerCount }}</b> 粉丝</span>
        <span><b>{{ followingCount }}</b> 关注</span>
      </div>

      <div v-if="profile?.bio" class="pv-bio">{{ profile.bio }}</div>
      <div class="pv-meta">
        <span v-if="profile?.location">📍 {{ profile.location }}</span>
        <span v-if="profile?.birthday">🎂 {{ profile.birthday }}</span>
      </div>
    </div>

    <!-- 编辑资料面板 -->
    <div v-if="editing" class="pv-edit">
      <h3>编辑资料</h3>
      <label>头像（一个表情）<input v-model="form.avatar" maxlength="8" placeholder="🐱" /></label>
      <label>昵称<input v-model="form.nickname" maxlength="50" placeholder="昵称" /></label>
      <label>标语<input v-model="form.slogan" maxlength="100" placeholder="一句话签名" /></label>
      <label>所在地<input v-model="form.location" maxlength="100" placeholder="城市" /></label>
      <label>简介<textarea v-model="form.bio" maxlength="500" rows="3" placeholder="介绍一下自己吧～"></textarea></label>
      <div class="pv-edit-actions">
        <button class="pv-btn" @click="saveEdit">保存</button>
        <button class="pv-btn ghost" @click="editing = false">取消</button>
      </div>
    </div>

    <!-- TA 关注的人 -->
    <div v-if="followings.length" class="pv-section">
      <h3>💞 关注的人（{{ followings.length }}）</h3>
      <UserRow v-for="u in followings" :key="u.username" :user="u" @toast="emit('toast', $event)" />
    </div>

    <!-- TA 的文章 -->
    <div class="pv-section">
      <h3>📝 文章（{{ posts.length }}）</h3>
      <div v-if="posts.length" class="pv-feed">
        <PostCard
          v-for="p in posts"
          :key="p.id"
          :post="p"
          @edit="emit('edit', $event)"
          @toast="emit('toast', $event)"
        />
      </div>
      <div v-else class="pv-empty">这里还空空的，去写点什么吧～</div>
    </div>
  </div>
</template>

<style scoped>
.profile-view {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.pv-head {
  text-align: center;
  background: #fdfbff;
  border: 2px solid #f0e6f7;
  border-radius: 20px;
  padding: 22px 18px 18px;
  position: relative;
}
.pv-back {
  position: absolute;
  left: 14px;
  top: 14px;
  border: none;
  background: #fff0f6;
  color: #c98bb0;
  border-radius: 999px;
  padding: 6px 14px;
  cursor: pointer;
  font-weight: 700;
}
.pv-avatar {
  width: 76px;
  height: 76px;
  margin: 0 auto 8px;
  display: grid;
  place-items: center;
  font-size: 40px;
  background: #fff0f6;
  border-radius: 50%;
}
.pv-name {
  margin: 4px 0 2px;
  color: var(--ink, #4a3b52);
}
.pv-username {
  color: #b9a7c4;
  font-size: 13px;
}
.pv-slogan {
  margin-top: 6px;
  color: #8a7a95;
}
.pv-actions {
  margin: 14px 0 10px;
}
.pv-btn {
  border: none;
  background: var(--pink, #ff9ec4);
  color: #fff;
  font-weight: 700;
  border-radius: 999px;
  padding: 9px 22px;
  cursor: pointer;
  transition: transform 0.12s, box-shadow 0.12s;
}
.pv-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(255, 158, 196, 0.4);
}
.pv-btn.followed {
  background: #e9dcef;
  color: #8a7a95;
}
.pv-btn.ghost {
  background: #fff0f6;
  color: #c98bb0;
}
.pv-stats {
  display: flex;
  justify-content: center;
  gap: 18px;
  color: #8a7a95;
  font-size: 14px;
}
.pv-stats b {
  color: var(--ink, #4a3b52);
}
.pv-bio {
  margin-top: 10px;
  color: #6f5f7a;
  white-space: pre-wrap;
}
.pv-meta {
  margin-top: 8px;
  display: flex;
  justify-content: center;
  gap: 16px;
  color: #9b8aa6;
  font-size: 13px;
}
.pv-edit {
  background: #fdfbff;
  border: 2px solid #f0e6f7;
  border-radius: 20px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.pv-edit h3 {
  margin: 0 0 4px;
  color: var(--ink, #4a3b52);
}
.pv-edit label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #8a7a95;
}
.pv-edit input,
.pv-edit textarea {
  font-family: inherit;
  font-size: 14px;
  color: var(--ink, #4a3b52);
  border: 2px solid #f0e6f7;
  border-radius: 12px;
  padding: 9px 14px;
  background: #fff;
  resize: vertical;
}
.pv-edit input:focus,
.pv-edit textarea:focus {
  outline: none;
  border-color: var(--pink, #ff9ec4);
}
.pv-edit-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
.pv-section h3 {
  margin: 0 0 10px;
  color: var(--ink, #4a3b52);
}
.pv-feed {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.pv-empty {
  text-align: center;
  color: #b9a7c4;
  padding: 24px;
}
</style>
