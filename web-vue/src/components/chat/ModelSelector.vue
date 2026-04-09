<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElSelect, ElOption } from 'element-plus'
import type { ChatModel } from '@/types/chat'
import { llmApi } from '@/api/chat'

// コンポーネントのプロパティ
const props = defineProps<{
  modelValue: string
}>()

// イベントの定義
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

// propsへ直接バインドする代わりに、v-model用のローカル算出プロパティを作成
const localModelValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// モデルリスト
const models = ref<ChatModel[]>([])
const loading = ref(false)

// モデルリストの読み込み
const loadModels = async () => {
  try {
    loading.value = true
    models.value = await llmApi.getModels()
  } catch (error) {
    console.error('モデルリストの読み込みに失敗しました:', error)
  } finally {
    loading.value = false
  }
}

// コンポーネントのマウント時にモデルリストを読み込む
onMounted(() => {
  loadModels()
})
</script>

<template>
  <div class="model-selector">
    <ElSelect 
      v-model="localModelValue" 
      placeholder="モデルを選択" 
      :loading="loading"
      size="small"
      style="width: 180px"
    >
      <ElOption
        v-for="model in models"
        :key="model.key"
        :label="model.name"
        :value="model.key"
      />
    </ElSelect>
  </div>
</template>

<style scoped>
.model-selector {
  margin-bottom: 8px;
  min-width: 180px;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select-dropdown__item) {
  padding-right: 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>