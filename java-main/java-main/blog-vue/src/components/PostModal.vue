<script setup>
import { ref, watch } from 'vue'
import { useStore } from '../store'

const props = defineProps({
  post: { type: Object, default: null }, // 传入则为编辑模式
})
const emit = defineEmits(['close', 'submit'])
const { EMOJIS } = useStore()

const title = ref('')
const body = ref('')
const pickEmoji = ref(EMOJIS[0])
const msg = ref('')

// 打开时根据是否传入 post 预填内容
watch(
  () => props.post,
  (p) => {
    if (p) {
      title.value = p.title
      body.value = p.body
      pickEmoji.value = p.emoji || EMOJIS[0]
    } else {
      title.value = ''
      body.value = ''
      pickEmoji.value = EMOJIS[0]
    }
    msg.value = ''
  },
  { immediate: true }
)

function pick(e) {
  pickEmoji.value = e
}

function submit() {
  if (!title.value.trim() || !body.value.trim()) {
    msg.value = '标题和正文都要写哦～'
    return
  }
  emit('submit', { title: title.value, body: body.value, emoji: pickEmoji.value })
}
</script>

<template>
  <div class="modal-mask" @click.self="emit('close')">
    <div class="modal-card">
      <button class="modal-close" @click="emit('close')">✕</button>
      <h2 class="form-title">{{ post ? '编辑文章' : '写一篇新文章' }}</h2>
      <label class="field">
        <span>标题</span>
        <input type="text" v-model="title" placeholder="今天发生了什么有趣的事？" />
      </label>
      <label class="field">
        <span>正文</span>
        <textarea v-model="body" rows="6" placeholder="在这里写写你的心情吧～"></textarea>
      </label>
      <div class="emoji-pick">
        <span>选个心情：</span>
        <div class="emoji-row">
          <div
            v-for="e in EMOJIS"
            :key="e"
            class="emoji-opt"
            :class="{ sel: pickEmoji === e }"
            @click="pick(e)"
          >
            {{ e }}
          </div>
        </div>
      </div>
      <p class="msg err">{{ msg }}</p>
      <button class="btn btn-primary" @click="submit">{{ post ? '保 存 ✏️' : '发 布 🚀' }}</button>
    </div>
  </div>
</template>
