<template>
  <el-form
    :model="formData"
    :rules="rules"
    ref="formRef"
    :label-position="labelPosition"
    :label-width="labelWidth"
    class="base-form"
  >
    <slot></slot>
  </el-form>
</template>

<script setup>
import { ref } from 'vue'

// Props
const props = defineProps({
  formData: {
    type: Object,
    required: true
  },
  rules: {
    type: Object,
    default: () => ({})
  },
  labelPosition: {
    type: String,
    default: 'right',
    validator: (value) => ['left', 'right', 'top'].includes(value)
  },
  labelWidth: {
    type: [String, Number],
    default: '80px'
  }
})

// Emits
const emit = defineEmits(['validate', 'submit'])

// Form reference
const formRef = ref(null)

// Methods
defineExpose({
  // 验证表单
  validate: (callback) => {
    if (!formRef.value) return false
    return formRef.value.validate(callback)
  },
  // 重置表单
  resetFields: () => {
    if (formRef.value) {
      formRef.value.resetFields()
    }
  },
  // 清除验证状态
  clearValidate: (props = []) => {
    if (formRef.value) {
      formRef.value.clearValidate(props)
    }
  }
})
</script>

<style scoped>
.base-form {
  width: 100%;
}

.el-form-item {
  margin-bottom: var(--spacing-lg, 20px);
}
</style>