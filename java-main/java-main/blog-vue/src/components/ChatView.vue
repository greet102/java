<script setup>
import { ref, computed } from 'vue'
import { useStore } from '../store'

const { state, openChat, closeChat, sendDM, fetchConversations, timeAgo } = useStore()

const draft = ref('')

// 当前对话对方的展示名（优先昵称）
const otherName = computed(() => {
  const c = state.conversations.find((x) => x.username === state.chatWith)
  return c ? c.nickname || c.username : state.chatWith
})

// 回到会话列表
function backToList() {
  state.chatWith = ''
  state.thread = []
  fetchConversations()
}

async function doSend() {
  const text = draft.value.trim()
  if (!text) return
  const ok = await sendDM(state.chatWith, text)
  if (ok) draft.value = ''
}
</script>

<template>
  <div class="chat-mask" @click.self="closeChat">
    <div class="chat-panel">
      <div class="chat-header">
        <span v-if="!state.chatWith" class="chat-title">💌 私信</span>
        <span v-else class="chat-back clickable" @click="backToList">‹ {{ otherName }}</span>
        <button class="chat-close" @click="closeChat">✕</button>
      </div>

      <!-- 会话列表 -->
      <div v-if="!state.chatWith" class="chat-list">
        <div
          v-for="c in state.conversations"
          :key="c.username"
          class="conv"
          @click="openChat(c.username)"
        >
          <div class="conv-avatar">{{ c.avatar || '🐱' }}</div>
          <div class="conv-main">
            <div class="conv-top">
              <b>{{ c.nickname || c.username }}</b>
              <span class="conv-time">{{ timeAgo(c.lastTime) }}</span>
            </div>
            <div class="conv-last">{{ c.lastContent }}</div>
          </div>
          <span v-if="c.unread" class="conv-badge">{{ c.unread }}</span>
        </div>
        <div v-if="!state.conversations.length" class="chat-empty">
          还没有私信～<br />去“我的关注”里给小伙伴发消息吧 💞
        </div>
      </div>

      <!-- 对话线程 -->
      <div v-else class="chat-thread">
        <div
          v-for="m in state.thread"
          :key="m.id"
          class="bubble-row"
          :class="{ me: m.sender === state.currentUser }"
        >
          <div class="bubble">{{ m.content }}</div>
          <div class="bubble-time">{{ timeAgo(m.createdAt) }}</div>
        </div>
        <div v-if="!state.thread.length" class="chat-empty">
          还没有消息，发一句打个招呼吧～
        </div>
      </div>

      <!-- 输入框（仅在具体对话里显示） -->
      <div v-if="state.chatWith" class="chat-input">
        <textarea
          v-model="draft"
          rows="1"
          placeholder="写点什么…（Enter 发送）"
          @keyup.enter.exact.prevent="doSend"
        ></textarea>
        <button class="chat-send" @click="doSend">发送</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-mask {
  position: fixed;
  inset: 0;
  background: rgba(74, 59, 82, 0.28);
  display: flex;
  justify-content: flex-end;
  z-index: 50;
}
.chat-panel {
  width: 380px;
  max-width: 92vw;
  height: 100%;
  background: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: -8px 0 30px rgba(255, 158, 196, 0.25);
  animation: slideIn 0.18s ease;
}
@keyframes slideIn {
  from { transform: translateX(30px); opacity: 0.6; }
  to { transform: translateX(0); opacity: 1; }
}
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 2px solid #f0e6f7;
  background: #fdfbff;
}
.chat-title {
  font-weight: 800;
  color: var(--ink, #4a3b52);
}
.chat-back {
  font-weight: 700;
  color: var(--pink, #ff9ec4);
  cursor: pointer;
}
.chat-close {
  border: none;
  background: #fff0f6;
  color: #c98bb0;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  cursor: pointer;
  font-weight: 700;
}

.chat-list {
  flex: 1 1 auto;
  overflow-y: auto;
  padding: 8px;
}
.conv {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 14px;
  cursor: pointer;
  transition: background 0.12s;
}
.conv:hover {
  background: #fdf0f6;
}
.conv-avatar {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  font-size: 22px;
  background: #fff0f6;
  border-radius: 50%;
  flex: 0 0 auto;
}
.conv-main {
  flex: 1 1 auto;
  min-width: 0;
}
.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 8px;
}
.conv-top b {
  color: var(--ink, #4a3b52);
}
.conv-time {
  font-size: 12px;
  color: #b9a7c4;
  flex: 0 0 auto;
}
.conv-last {
  font-size: 13px;
  color: #9b8aa6;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.conv-badge {
  flex: 0 0 auto;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: var(--pink, #ff9ec4);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.chat-thread {
  flex: 1 1 auto;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #fdfbff;
}
.bubble-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 80%;
}
.bubble-row.me {
  align-self: flex-end;
  align-items: flex-end;
}
.bubble {
  padding: 9px 13px;
  border-radius: 16px;
  background: #fff;
  border: 2px solid #f0e6f7;
  color: var(--ink, #4a3b52);
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
}
.bubble-row.me .bubble {
  background: var(--pink, #ff9ec4);
  border-color: var(--pink, #ff9ec4);
  color: #fff;
}
.bubble-time {
  font-size: 11px;
  color: #b9a7c4;
  margin-top: 2px;
  padding: 0 4px;
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 2px solid #f0e6f7;
  background: #fff;
}
.chat-input textarea {
  flex: 1 1 auto;
  resize: none;
  font-family: inherit;
  font-size: 14px;
  color: var(--ink, #4a3b52);
  border: 2px solid #f0e6f7;
  border-radius: 14px;
  padding: 9px 12px;
  background: #fdfbff;
  max-height: 120px;
}
.chat-input textarea:focus {
  outline: none;
  border-color: var(--pink, #ff9ec4);
}
.chat-send {
  flex: 0 0 auto;
  border: none;
  background: var(--pink, #ff9ec4);
  color: #fff;
  font-weight: 700;
  border-radius: 14px;
  padding: 0 18px;
  cursor: pointer;
}

.chat-empty {
  text-align: center;
  color: #b9a7c4;
  padding: 40px 20px;
  line-height: 1.8;
}
</style>
