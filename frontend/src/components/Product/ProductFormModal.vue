<template>
  <el-dialog
    :model-value="visible"
    :title="isEditMode ? '编辑商品' : '添加商品'"
    width="700px"
    @update:model-value="handleUpdateVisible"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <el-form 
      ref="productForm" 
      :model="productForm" 
      :rules="rules" 
      label-width="100px"
    >
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="productForm.name" placeholder="请输入商品名称"></el-input>
      </el-form-item>
      
      <el-form-item label="商品价格" prop="price">
        <el-input 
          v-model.number="productForm.price" 
          type="number" 
          placeholder="请输入商品价格"
          min="0"
          step="0.01"
        ></el-input>
      </el-form-item>
      
      <el-form-item label="库存数量" prop="stock">
        <el-input 
          v-model.number="productForm.stock" 
          type="number" 
          placeholder="请输入库存数量"
          min="0"
        ></el-input>
      </el-form-item>
      
      <el-form-item label="商品描述" prop="description">
        <el-input 
          v-model="productForm.description" 
          type="textarea" 
          placeholder="请输入商品描述"
          rows="4"
        ></el-input>
      </el-form-item>
      
      <el-form-item label="商品图片">
        <el-upload
          :headers="uploadHeaders"
          :action="uploadUrl"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          list-type="picture"
          :limit="1"
          :on-exceed="handleExceed"
        >
          <el-button type="primary">上传图片</el-button>
          <template #tip>
            <div class="el-upload__tip">只能上传一张图片，且不超过2MB</div>
          </template>
        </el-upload>
        <img v-if="productForm.imageUrl" :src="productForm.imageUrl" alt="商品图片" class="preview-image">
      </el-form-item>
      
      <el-form-item label="商品分类" prop="categoryId">
        <el-select 
          v-model="productForm.categoryId" 
          placeholder="请选择商品分类" 
          class="w-full"
        >
          <el-option 
            v-for="category in categories" 
            :key="category.id" 
            :value="category.id" 
            :label="category.name"
          ></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEditMode ? '更新' : '添加' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: 'ProductFormModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    product: {
      type: Object,
      default: () => ({})
    },
    categories: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:visible', 'submit'],
  data() {
    return {
      productForm: {
        id: '',
        name: '',
        price: 0,
        stock: 0,
        description: '',
        imageUrl: '',
        categoryId: ''
      },
      uploadUrl: '/api/files/upload/product',
      uploadHeaders: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      rules: {
        name: [
          { required: true, message: '请输入商品名称', trigger: 'blur' },
          { min: 2, max: 100, message: '商品名称长度应在2-100个字符之间', trigger: 'blur' }
        ],
        price: [
          { required: true, message: '请输入商品价格', trigger: 'blur' },
          { type: 'number', min: 0, message: '商品价格不能为负数', trigger: 'blur' }
        ],
        stock: [
          { required: true, message: '请输入库存数量', trigger: 'blur' },
          { type: 'number', min: 0, message: '库存数量不能为负数', trigger: 'blur' }
        ],
        description: [
          { required: true, message: '请输入商品描述', trigger: 'blur' },
          { min: 5, message: '商品描述至少5个字符', trigger: 'blur' }
        ],
        categoryId: [
          { required: true, message: '请选择商品分类', trigger: 'change' }
        ]
      }
    };
  },
  computed: {
    isEditMode() {
      return !!this.product.id;
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.initForm();
      } else {
        this.resetForm();
      }
    },
    product: {
      handler(newVal) {
        if (this.visible) {
          this.initForm();
        }
      },
      deep: true
    }
  },
  methods: {
    handleUpdateVisible(value) {
      this.$emit('update:visible', value);
      if (!value) {
        this.handleCancel();
      }
    },
    handleClose() {
      this.handleCancel();
    },
    initForm() {
      if (this.product && this.product.id) {
        this.productForm = { ...this.product };
      } else {
        this.resetForm();
      }
    },
    resetForm() {
      this.productForm = {
        id: '',
        name: '',
        price: 0,
        stock: 0,
        description: '',
        imageUrl: '',
        categoryId: ''
      };
      if (this.$refs.productForm) {
        this.$refs.productForm.resetFields();
      }
    },
    handleCancel() {
      this.resetForm();
      this.$emit('update:visible', false);
    },
    async handleSubmit() {
      try {
        await this.$refs.productForm.validate();
        this.$emit('submit', { ...this.productForm });
      } catch (error) {
        // 表单验证失败
      }
    },
    handleUploadSuccess(response) {
      console.log('上传响应:', response);
      // 检查响应是否包含fileUrl字段（这是后端返回的实际格式）
      if (response && response.fileUrl) {
        this.productForm.imageUrl = response.fileUrl;
        this.$message.success(response.message || '图片上传成功');
      } else if (response && response.code === 200) {
        // 保留原有逻辑作为兼容
        this.productForm.imageUrl = response.data?.url || '';
        this.$message.success('图片上传成功');
      } else {
        const errorMsg = response ? `图片上传失败: ${response.message || '未知错误'}` : '图片上传失败: 无效响应';
        console.error(errorMsg);
        this.$message.error(errorMsg);
      }
    },
    handleUploadError(err, file, fileList) {
      console.error('上传错误:', err);
      let errorMsg = '图片上传失败，请重试';
      if (err && err.response) {
        errorMsg = `图片上传失败: ${err.response.status} ${err.response.statusText}`;
        console.error('错误响应:', err.response);
      }
      this.$message.error(errorMsg);
    },
    handleExceed() {
      this.$message.warning('只能上传一张图片');
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.preview-image {
  max-width: 200px;
  max-height: 200px;
  margin-top: var(--spacing-md);
  border-radius: var(--border-radius);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
}
</style>