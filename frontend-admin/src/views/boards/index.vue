<template>
  <div class="board-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>板块管理</span>
          <el-button type="primary" @click="handleCreate">新建板块</el-button>
        </div>
      </template>

      <div class="table-container">
        <el-table
          :data="boardList"
          v-loading="loading"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="板块名称" width="150" />
          <el-table-column
            prop="description"
            label="描述"
            min-width="200"
            show-overflow-tooltip
          />
          <el-table-column prop="sort" label="排序" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? "启用" : "禁用" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" min-width="160" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" size="small" @click="handleEdit(row)"
                  >编辑</el-button
                >
                <el-button type="danger" size="small" @click="handleDelete(row)"
                  >删除</el-button
                >
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入板块名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入板块描述"
          />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标URL" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  createBoard,
  deleteBoard,
  getBoardList,
  updateBoard,
  type Board,
  type CreateBoardRequest,
  type UpdateBoardRequest,
} from "@/api/admin/board";
import type { FormInstance, FormRules } from "element-plus";
import { ElMessage, ElMessageBox } from "element-plus";
import { onMounted, reactive, ref } from "vue";

const loading = ref(false);
const boardList = ref<Board[]>([]);
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);
const dialogVisible = ref(false);
const dialogTitle = ref("新建板块");
const editingId = ref<number | null>(null);
const formRef = ref<FormInstance>();

const form = reactive<CreateBoardRequest>({
  name: "",
  description: "",
  icon: "",
  sort: 0,
});

const rules: FormRules = {
  name: [{ required: true, message: "请输入板块名称", trigger: "blur" }],
};

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getBoardList({
      page: currentPage.value,
      size: pageSize.value,
    });
    boardList.value = res.data.records;
    total.value = res.data.total;
  } catch (error) {
    ElMessage.error("获取板块列表失败");
  } finally {
    loading.value = false;
  }
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchData();
};

const handleCreate = () => {
  dialogTitle.value = "新建板块";
  editingId.value = null;
  form.name = "";
  form.description = "";
  form.icon = "";
  form.sort = 0;
  dialogVisible.value = true;
};

const handleEdit = (board: Board) => {
  dialogTitle.value = "编辑板块";
  editingId.value = board.id;
  form.name = board.name;
  form.description = board.description || "";
  form.icon = board.icon || "";
  form.sort = board.sort;
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (!valid) return;

    try {
      if (editingId.value) {
        await updateBoard(editingId.value, form as UpdateBoardRequest);
        ElMessage.success("更新成功");
      } else {
        await createBoard(form);
        ElMessage.success("创建成功");
      }
      dialogVisible.value = false;
      fetchData();
    } catch (error) {
      ElMessage.error("操作失败");
    }
  });
};

const handleDelete = async (board: Board) => {
  try {
    await ElMessageBox.confirm("确定要删除该板块吗？", "警告", {
      type: "error",
    });
    await deleteBoard(board.id);
    ElMessage.success("删除成功");
    fetchData();
  } catch (error: any) {
    if (error !== "cancel") ElMessage.error("删除失败");
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="scss">
.board-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;

    @media (max-width: 480px) {
      flex-direction: column;
      align-items: stretch;
      gap: 12px;
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;

    @media (max-width: 480px) {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;
    }
  }

  // 表格容器 - 允许水平滚动
  .table-container {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  // 操作按钮容器
  .action-buttons {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    align-items: center;

    @media (max-width: 768px) {
      flex-direction: column;
      gap: 4px;
      align-items: flex-start;
      
      .el-button {
        width: 100%;
        margin-left: 0;
      }
    }
  }

  :deep(.el-card) {
    background: #ffffff;
    border: 1px solid #e5e7eb;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  }

  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #f3f4f6;
    background: #fafafa;
    color: #374151;
    font-size: 15px;
    font-weight: 500;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }

  :deep(.el-table) {
    background: transparent;

    &::before {
      display: none;
    }

    tr {
      background: transparent;
    }

    th.el-table__cell {
      background: #f9fafb;
      color: #374151;
      font-weight: 600;
      border-bottom: 1px solid #e5e7eb;
    }

    td.el-table__cell {
      border-bottom: 1px solid #f3f4f6;
      color: #374151;
    }

    .el-table__row:hover > td.el-table__cell {
      background: #f3f4f6 !important;
    }

    .el-table__row:nth-child(even) > td.el-table__cell {
      background: #fafafa;
    }
  }

  :deep(.el-button--primary) {
    background: #1e3a8a;
    border: none;
    border-radius: 8px;
    font-weight: 500;

    &:hover {
      background: #1e40af;
    }
  }

  :deep(.el-button--danger) {
    background: #fee2e2;
    border: 1px solid #fecaca;
    color: #991b1b;
    border-radius: 6px;

    &:hover {
      background: #fecaca;
    }
  }

  :deep(.el-tag) {
    border-radius: 6px;
    border: none;
    font-weight: 500;
  }

  :deep(.el-tag--success) {
    background: #dcfce7;
    color: #166534;
  }

  :deep(.el-tag--danger) {
    background: #fee2e2;
    color: #991b1b;
  }

  :deep(.el-pagination) {
    color: #6b7280;

    .el-pager li {
      background: #ffffff;
      border: 1px solid #e5e7eb;
      color: #374151;
      border-radius: 6px;
      margin: 0 2px;

      &:hover {
        color: #1e3a8a;
        border-color: #1e3a8a;
      }

      &.is-active {
        background: #1e3a8a;
        border-color: #1e3a8a;
        color: #ffffff;
      }
    }

    .btn-prev,
    .btn-next {
      background: #ffffff;
      border: 1px solid #e5e7eb;
      color: #374151;
      border-radius: 6px;

      &:hover {
        color: #1e3a8a;
        border-color: #1e3a8a;
      }
    }
  }

  :deep(.el-dialog) {
    border-radius: 16px;
    background: #ffffff;
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);

    .el-dialog__header {
      padding: 20px 24px;
      border-bottom: 1px solid #e5e7eb;
    }

    .el-dialog__title {
      color: #111827;
      font-weight: 600;
    }

    .el-dialog__body {
      padding: 24px;
      color: #374151;
    }

    .el-dialog__footer {
      padding: 16px 24px;
      border-top: 1px solid #e5e7eb;
    }
  }

  :deep(.el-form-item__label) {
    color: #374151;
    font-weight: 500;
  }

  :deep(.el-input__wrapper) {
    background: #ffffff !important;
    border: 1px solid #d1d5db !important;
    box-shadow: none !important;
    border-radius: 8px;

    &:hover {
      border-color: #9ca3af !important;
    }

    &.is-focus {
      border-color: #1e3a8a !important;
      box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1) !important;
    }
  }

  :deep(.el-input__inner) {
    color: #111827 !important;

    &::placeholder {
      color: #9ca3af !important;
    }
  }

  :deep(.el-textarea__inner) {
    background: #ffffff !important;
    border: 1px solid #d1d5db !important;
    color: #111827 !important;
    border-radius: 8px;

    &::placeholder {
      color: #9ca3af !important;
    }
  }

  :deep(.el-input-number) {
    .el-input-number__decrease,
    .el-input-number__increase {
      background: #f9fafb;
      border: 1px solid #e5e7eb;
      color: #6b7280;

      &:hover {
        color: #1e3a8a;
      }
    }
  }

  :deep(.el-button--default) {
    background: #ffffff;
    border: 1px solid #d1d5db;
    color: #374151;
    border-radius: 8px;

    &:hover {
      background: #f9fafb;
      border-color: #9ca3af;
    }
  }
}
</style>
