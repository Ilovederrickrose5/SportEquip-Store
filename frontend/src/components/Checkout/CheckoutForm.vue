<template>
  <el-dialog
    v-model="dialogVisible"
    title="确认订单信息"
    width="500px"
    center
    @close="handleClose"
  >
    <el-form ref="checkoutForm" :model="form" :rules="rules" label-width="100px">
      <!-- 个人信息 -->
      <el-form-item label="收货人姓名" prop="recipientName">
        <el-input v-model="form.recipientName" placeholder="请输入收货人姓名" />
      </el-form-item>
      
      <el-form-item label="手机号码" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号码" />
      </el-form-item>
      
      <el-form-item label="收货地址" prop="address">
        <el-input
          v-model="form.address"
          type="textarea"
          placeholder="请输入详细收货地址"
          rows="3"
        />
      </el-form-item>
      
      <!-- 支付方式 -->
      <el-form-item label="支付方式" prop="paymentMethod">
        <el-radio-group v-model="form.paymentMethod">
          <el-radio label="WECHAT">微信支付</el-radio>
          <el-radio label="ALIPAY">支付宝</el-radio>
          <el-radio label="BANKCARD">银行卡</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <!-- 订单备注 -->
      <el-form-item label="订单备注">
        <el-input
          v-model="form.remark"
          type="textarea"
          placeholder="如有特殊要求请在此备注"
          rows="2"
        />
      </el-form-item>
      
      <!-- 订单摘要 -->
      <div class="order-summary">
        <h3>订单摘要</h3>
        <div class="summary-item">
          <span>商品数量:</span>
          <span>{{ cartItems.length }} 件</span>
        </div>
        <div class="summary-item">
          <span>商品总价:</span>
          <span class="price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <div class="summary-item">
          <span>运费:</span>
          <span>¥0.00</span>
        </div>
        <div class="summary-total">
          <span>应付金额:</span>
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
      </div>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="loading">
          提交订单
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import OrderService from '../../services/OrderService';

export default {
  name: 'CheckoutForm',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    cartItems: {
      type: Array,
      required: true
    },
    totalPrice: {
      type: Number,
      required: true
    }
  },
  emits: ['close', 'orderCreated'],
  data() {
    return {
      dialogVisible: this.visible,
      loading: false,
      form: {
        recipientName: '',
        phone: '',
        address: '',
        paymentMethod: 'WECHAT',
        remark: ''
      },
      rules: {
        recipientName: [
          { required: true, message: '请输入收货人姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号码', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        address: [
          { required: true, message: '请输入收货地址', trigger: 'blur' },
          { min: 5, message: '地址长度至少 5 个字符', trigger: 'blur' }
        ],
        paymentMethod: [
          { required: true, message: '请选择支付方式', trigger: 'change' }
        ]
      }
    };
  },
  watch: {
    visible: {
      handler(val) {
        this.dialogVisible = val;
      },
      immediate: true
    },
    dialogVisible(val) {
      if (!val) {
        this.handleClose();
      }
    }
  },
  methods: {
    handleClose() {
      this.$refs.checkoutForm?.resetFields();
      this.$emit('close');
    },
    async submitForm() {
      try {
        await this.$refs.checkoutForm.validate();
        this.loading = true;
        
        // 直接构建完整的订单数据对象
        const orderData = {
          "shippingAddress": this.form.address,
          "phone": this.form.phone,
          "recipientName": this.form.recipientName,
          "paymentMethod": this.form.paymentMethod,
          "remark": this.form.remark,
          "totalAmount": String(this.totalPrice),
          "status": "PENDING",
          "orderItems": [] // 确保orderItems是一个数组
        };
        
        // 手动添加订单项，确保每个字段都正确设置
        if (Array.isArray(this.cartItems)) {
          this.cartItems.forEach(item => {
            // 使用正确的productId字段，而不是cart item的id
            orderData.orderItems.push({
              "productId": Number(item.productId), // 确保使用商品ID而不是购物车项ID
              "productName": item.productName,
              "price": String(item.price),
              "quantity": Number(item.quantity)
            });
          });
        }
        
        // 确保orderItems至少有一个商品
        if (orderData.orderItems.length === 0) {
          throw new Error('购物车为空，无法创建订单');
        }
        
        // 创建订单
        const result = await OrderService.createOrder(orderData);
        this.$message.success('订单创建成功！');
        this.$emit('orderCreated', result);
        // 跳转到订单列表页
        this.$router.push('/orders');
        this.handleClose();
      } catch (error) {
        this.$message.error(error.message || '订单创建失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped lang="scss">
.order-summary {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.order-summary h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 600;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e4e7ed;
  font-size: 16px;
  font-weight: 600;
}

.price {
  color: #ff6700;
  font-weight: 500;
}

.total-price {
  color: #ff6700;
  font-size: 18px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>