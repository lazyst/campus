/**
 * ImagePreview 组件类型定义
 * 
 * 基于 TDesign Mobile Vue ImageViewer 组件设计
 */
import { TNode } from 'vue'

// 图片信息接口
export interface ImageInfo {
  /** 图片地址 */
  url: string
  /** 图片对齐方式 */
  align?: 'start' | 'center' | 'end'
}

// 关闭触发源类型
export type ImagePreviewCloseTrigger = 
  | 'image'      // 点击图片
  | 'overlay'    // 点击遮罩
  | 'close-btn'  // 点击关闭按钮
  | 'delete'     // 删除图片
  | 'escape'     // 按下 ESC 键

// 关闭事件上下文
export interface ImagePreviewCloseContext {
  /** 触发源 */
  trigger: ImagePreviewCloseTrigger
  /** 事件对象 */
  e?: Event
  /** 当前图片索引 */
  index: number
}

// 索引变更事件上下文
export interface ImagePreviewIndexChangeContext {
  /** 触发方式 */
  trigger: 'prev' | 'next'
}

// 组件 Props 接口
export interface ImagePreviewProps {
  /** 图片列表，支持字符串数组或 ImageInfo 对象数组 */
  images: (string | ImageInfo)[]
  /** 初始显示的图片索引（受控） */
  initialIndex?: number
  /** 是否显示预览（受控） */
  visible?: boolean
  /** 是否显示关闭按钮 */
  closeBtn?: boolean
  /** 是否显示删除按钮 */
  deleteBtn?: boolean
  /** 最大缩放比例 */
  maxZoom?: number
  /** 是否显示索引指示器 */
  showIndex?: boolean
}

// 组件事件接口
export interface ImagePreviewEmits {
  /** 关闭事件 */
  close: [context: ImagePreviewCloseContext]
  /** 删除图片事件 */
  delete: [index: number]
  /** 索引变更事件 */
  indexChange: [index: number, context: ImagePreviewIndexChangeContext]
}
