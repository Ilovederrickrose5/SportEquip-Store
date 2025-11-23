<template>
  <div v-if="visible" class="modal-overlay" @click.self="$emit('cancel')">
    <div class="modal-content confirm-modal">
      <h3>{{ title }}</h3>
      <p>{{ message }}</p>
      <div class="modal-footer">
        <button @click="$emit('confirm')" class="btn btn-primary">确认退出</button>
        <button @click="$emit('cancel')" class="btn btn-secondary">取消</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LogoutModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: '确认退出登录'
    },
    message: {
      type: String,
      default: '确定要退出登录吗？'
    }
  }
}
</script>

<style scoped>
@import '../../assets/css/variables.scss';

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 90%;
}

.confirm-modal {
  text-align: center;
}

.modal-footer {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.btn {
  display: inline-block;
  padding: 8px 16px;
  border: none;
  border-radius: var(--border-radius);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
  text-decoration: none;
}

.btn-primary {
  background-color: var(--primary-color);
  color: white;
}

.btn-primary:hover {
  background-color: var(--primary-dark);
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #545b62;
}
</style>