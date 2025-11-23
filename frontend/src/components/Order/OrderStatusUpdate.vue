<template>
  <div class="order-status-update">
    <div class="order-info">
      <p>订单号: {{ order?.id }}</p>
      <p>当前状态: <el-tag :type="getStatusType(order?.status)">{{ getStatusText(order?.status) }}</el-tag></p>
      <p>总金额: ¥{{ (order?.totalAmount || 0).toFixed(2) }}</p>
    </div>
    
    <el-form :model="formData" :rules="rules" ref="statusForm" label-width="100px">
      <el-form-item label="新状态" prop="newStatus">
        <el-select v-model="formData.newStatus" placeholder="请选择新状态" style="width: 100%">
          <el-option 
            v-for="option in availableStatuses" 
            :key="option.value" 
            :label="option.label" 
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          placeholder="请输入备注信息（可选）"
          :rows="3"
        ></el-input>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'OrderStatusUpdate',
  props: {
    order: {
      type: Object,
      default: () => ({})
    }
  },
  emits: ['confirm', 'cancel'],
  data() {
    return {
      formData: {
        newStatus: '',
        remark: ''
      },
      rules: {
        newStatus: [
          {
            required: true,
            message: '请选择新状态',
            trigger: 'change'
          }
        ]
      }
    };
  },
  computed: {
    statusOptions() {
      return [
        { label: '待支付', value: 'PENDING', type: 'warning' },
        { label: '已支付', value: 'PAID', type: 'primary' },
        { label: '已发货', value: 'SHIPPED', type: 'info' },
        { label: '已完成', value: 'DELIVERED', type: 'success' },
        { label: '已取消', value: 'CANCELED', type: 'danger' }
      ];
    },
    availableStatuses() {
      if (!this.order || !this.order.status) return [];
      
      // 根据当前状态筛选可切换的状态
      const currentStatus = this.order.status;
      const statusFlow = {
        'PENDING': ['PAID', 'CANCELED'],
        'PAID': ['SHIPPED', 'CANCELED'],
        'SHIPPED': ['DELIVERED'],
        'DELIVERED': [],
        'CANCELED': []
      };
      
      return this.statusOptions.filter(option => 
        statusFlow[currentStatus]?.includes(option.value)
      );
    }
  },
  methods: {
    getStatusText(status) {
      const option = this.statusOptions.find(opt => opt.value === status);
      return option ? option.label : status;
    },
    
    getStatusType(status) {
      const option = this.statusOptions.find(opt => opt.value === status);
      return option ? option.type : 'default';
    },
    
    handleConfirm() {
      this.$refs.statusForm.validate((valid) => {
        if (valid) {
          this.$emit('confirm', { ...this.formData });
        }
      });
    },
    
    handleCancel() {
      this.$emit('cancel');
    },
    
    resetForm() {
      this.formData.newStatus = '';
      this.formData.remark = '';
      if (this.$refs.statusForm) {
        this.$refs.statusForm.resetFields();
      }
    }
  },
  watch: {
    'order.id': function() {
      this.resetForm();
    }
  }
};
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.order-status-update {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  
  .order-info {
    padding: var(--spacing-md);
    background-color: var(--bg-secondary);
    border-radius: var(--border-radius);
    margin-bottom: var(--spacing-md);
    
    p {
      margin: var(--spacing-xs) 0;
    }
  }
}
</style>