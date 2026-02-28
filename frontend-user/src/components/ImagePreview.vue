<script setup lang="ts">
import { ref, computed, reactive, toRefs, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { getApiBaseUrl } from '@/utils/imageUrl'
import type { ImageInfo, ImagePreviewCloseTrigger, ImagePreviewCloseContext } from './ImagePreview.types'

// =============== Props ===============
interface Props {
  images: (string | ImageInfo)[]
  initialIndex?: number
  visible?: boolean
  closeBtn?: boolean
  deleteBtn?: boolean
  maxZoom?: number
  showIndex?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  initialIndex: 0,
  visible: false,
  closeBtn: true,
  deleteBtn: false,
  maxZoom: 3,
  showIndex: true
})

const emit = defineEmits<{
  close: [context: ImagePreviewCloseContext]
  delete: [index: number]
  indexChange: [index: number, context: { trigger: 'prev' | 'next' }]
}>()

// =============== 常量 ===============
const TAP_TIME = 300

// =============== 状态管理 ===============
const state = reactive({
  scale: 1,
  dragging: false,
  draggedX: 0,
  draggedY: 0,
  dblTapZooming: false,
  zooming: false,
  touchIndex: 0,
  extraDraggedX: 0
})

const { scale, dragging, draggedX, draggedY, dblTapZooming, zooming, touchIndex, extraDraggedX } = toRefs(state)

// 图片尺寸缓存
const imagesSize = reactive<Record<number, { height: number }>>({})

// 当前索引（受控）
const currentIndex = ref(props.initialIndex)

// 是否可见
const isVisible = ref(props.visible)

// 图片信息列表
const imageInfoList = computed(() => {
  return props.images.map((image, index) => {
    let imageInfo: ImageInfo
    if (typeof image === 'string') {
      imageInfo = { url: image, align: 'center' }
    } else {
      imageInfo = image
    }
    return {
      image: imageInfo,
      preload: preloadImageIndex.value.includes(index)
    }
  })
})

// 预加载图片索引
const preloadImageIndex = computed(() => {
  const lastIndex = props.images.length - 1
  if ([undefined, 0].includes(currentIndex.value)) {
    return [0, 1, lastIndex]
  }
  if (currentIndex.value === lastIndex) {
    return [lastIndex, lastIndex - 1, 0]
  }
  const prev = currentIndex.value - 1 >= 0 ? currentIndex.value - 1 : lastIndex
  const next = currentIndex.value + 1 <= lastIndex ? currentIndex.value + 1 : 0
  return [currentIndex.value, prev, next]
})

// =============== 计算属性 ===============
const imageTransform = computed(() => {
  return `matrix(${scale.value}, 0, 0, ${scale.value}, ${draggedX.value}, ${draggedY.value})`
})

const imageTransitionDuration = computed(() => {
  return zooming.value || dragging.value ? '0s' : '0.3s'
})

// 容器滑动偏移量（用于滑动切换）
const containerTransform = computed(() => {
  if (state.scale > 1) return 'none'
  return `translateX(${-currentIndex.value * window.innerWidth + containerOffset}px)`
})

const showIndexIndicator = computed(() => props.showIndex && props.images.length > 1)

const isSingleImage = computed(() => props.images.length === 1)

// =============== 图片加载 ===============
function onImgLoad(e: Event, index: number) {
  const height = (e.target as HTMLImageElement).height
  imagesSize[index] = { height }
}

// =============== 关闭处理 ===============
function beforeClose() {
  state.dblTapZooming = false
  state.zooming = false
  state.scale = 1
  state.dragging = false
  state.draggedX = 0
  state.draggedY = 0
  state.extraDraggedX = 0
}

function handleClose(trigger: ImagePreviewCloseTrigger, e?: Event) {
  beforeClose()
  isVisible.value = false
  emit('close', { trigger, e, index: currentIndex.value })
}

function handleOverlayClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (target.tagName === 'IMG') {
    handleClose('image', e)
  } else {
    handleClose('overlay', e)
  }
}

function handleCloseBtnClick(e: MouseEvent) {
  handleClose('close-btn', e)
}

// =============== 删除处理 ===============
function handleDelete() {
  emit('delete', currentIndex.value)
}

// =============== 缩放处理 ===============
function setImageScale(newScale: number) {
  newScale = Math.min(newScale, props.maxZoom + 1)
  if (newScale !== state.scale) {
    state.scale = newScale
    if (newScale === 1) {
      state.draggedX = 0
      state.draggedY = 0
    }
  }
}

function getMaxDraggedY(index: number) {
  const rootOffsetHeight = window.innerHeight
  const currentImageHeight = imagesSize[index]?.height || 0
  const currentImageScaledHeight = state.scale * currentImageHeight
  const halfScaleHeight = (currentImageScaledHeight - currentImageHeight) / 2
  
  if (currentImageScaledHeight <= rootOffsetHeight) {
    return { top: 0, bottom: 0 }
  }
  
  const diffHeight = currentImageScaledHeight - rootOffsetHeight
  const centerDraggedY = diffHeight / 2
  const alignment = imageInfoList.value[index]?.image.align || 'center'
  
  const alignmentDraggedY: Record<string, { top: number; bottom: number }> = {
    start: { top: -diffHeight + halfScaleHeight, bottom: halfScaleHeight },
    center: { top: -centerDraggedY, bottom: centerDraggedY },
    end: { top: -halfScaleHeight, bottom: diffHeight - halfScaleHeight }
  }
  
  return alignmentDraggedY[alignment]
}

function getMaxDraggedX() {
  const rootOffsetWidth = window.innerWidth
  const scaledWidth = state.scale * rootOffsetWidth
  return Math.max(0, (scaledWidth - rootOffsetWidth) / 2)
}

// =============== 拖拽状态 ===============
let dragStartX = 0
let dragStartY = 0
let dragStartTime = 0
let containerOffset = 0 // 容器偏移量
let dblTapTimer: ReturnType<typeof setTimeout> | null = null

function checkTap(e: MouseEvent | TouchEvent) {
  const now = Date.now()
  const deltaTime = now - dragStartTime
  
  if (deltaTime < TAP_TIME) {
    if (dblTapTimer) {
      clearTimeout(dblTapTimer)
      dblTapTimer = setTimeout(() => {
        clearTimeout(dblTapTimer)
        state.dragging = false
        // 切换缩放
        const newScale = state.scale > 1 ? 1 : 2
        setImageScale(newScale)
      }, TAP_TIME)
    } else {
      dblTapTimer = setTimeout(() => {
        handleClose('overlay', e instanceof MouseEvent ? e : undefined)
        dblTapTimer = null
      }, TAP_TIME)
    }
  }
}

function handleTouchStart(e: TouchEvent) {
  const touch = e.touches[0]
  dragStartX = touch.clientX
  dragStartY = touch.clientY
  dragStartTime = Date.now()
  state.touchIndex = currentIndex.value
}

function handleTouchMove(e: TouchEvent) {
  const touch = e.touches[0]
  const deltaX = touch.clientX - dragStartX
  const deltaY = touch.clientY - dragStartY
  
  // 判断是否是水平滑动
  const isHorizontalSwipe = Math.abs(deltaX) > Math.abs(deltaY)
  
  if (state.scale === 1 && isHorizontalSwipe) {
    // 原始大小时，水平滑动切换图片
    containerOffset = deltaX
  } else if (state.scale > 1) {
    // 放大后，拖拽移动
    handleDrag(deltaX, deltaY)
  }
}

function handleTouchEnd(e: TouchEvent) {
  const deltaX = containerOffset
  
  // 检查单击（快速点击且移动距离小）
  const now = Date.now()
  const deltaTime = now - dragStartTime
  
  if (deltaTime < TAP_TIME && Math.abs(deltaX) < 10) {
    // 单击，关闭
    handleClose('overlay')
    containerOffset = 0
    return
  }
  
  // 如果是水平滑动切换图片
  if (state.scale === 1) {
    const threshold = 60
    if (Math.abs(deltaX) > threshold) {
      if (deltaX > 0) {
        // 向右滑，上一张
        if (currentIndex.value > 0) {
          switchImage(currentIndex.value - 1, 'prev')
        }
      } else {
        // 向左滑，下一张
        if (currentIndex.value < props.images.length - 1) {
          switchImage(currentIndex.value + 1, 'next')
        }
      }
    }
    // 重置偏移
    containerOffset = 0
  } else {
    // 放大状态，边界回弹
    handleDragEnd(deltaX)
  }
  
  // 缩放结束处理
  if (state.scale < 1) {
    setImageScale(1)
  }
  if (state.scale > props.maxZoom) {
    state.scale = props.maxZoom
  }
}

function handlePinch(e: TouchEvent) {
  if (state.dblTapZooming) return
  
  const t1 = e.touches[0]
  const t2 = e.touches[1]
  
  const distance = Math.sqrt(
    Math.pow(t2.clientX - t1.clientX, 2) +
    Math.pow(t2.clientY - t1.clientY, 2)
  )
  
  // 简单缩放比例计算
  const centerX = (t1.clientX + t2.clientX) / 2
  const centerY = (t1.clientY + t2.clientY) / 2
  
  // 基于初始距离计算缩放（简化版本）
  const minDistance = 100
  const scaleRatio = distance / minDistance
  let newScale = 1 + (scaleRatio - 1) * 0.5
  
  state.zooming = true
  setImageScale(newScale)
}

function handleDrag(deltaX: number, deltaY: number) {
  state.touchIndex = currentIndex.value
  
  if (state.scale === 1) {
    // 在原始大小时不允许拖拽
    return
  }
  
  state.dragging = true
  state.draggedY = deltaY
  
  // 限制垂直拖拽范围
  const maxY = getMaxDraggedY(currentIndex.value)
  state.draggedY = Math.max(maxY.top, Math.min(maxY.bottom, state.draggedY))
  
  state.draggedX = deltaX
}

function handleDragEnd(deltaX: number) {
  state.dragging = false
  
  // 滑动切换图片
  if (state.extraDraggedX !== 0 && Math.abs(state.extraDraggedX) > 50) {
    state.extraDraggedX = 0
    if (deltaX < 0) {
      // 向左滑，下一张
      if (currentIndex.value < props.images.length - 1) {
        switchImage(currentIndex.value + 1, 'next')
      }
    } else {
      // 向右滑，上一张
      if (currentIndex.value > 0) {
        switchImage(currentIndex.value - 1, 'prev')
      }
    }
    return
  }
  
  state.extraDraggedX = 0
  boundTransform()
}

function boundTransform() {
  const maxY = getMaxDraggedY(currentIndex.value)
  const maxX = getMaxDraggedX()
  
  state.draggedY = Math.max(maxY.top, Math.min(maxY.bottom, state.draggedY))
  state.draggedX = Math.max(-maxX, Math.min(maxX, state.draggedX))
}

// =============== 图片切换 ===============
function switchImage(index: number, trigger: 'prev' | 'next') {
  // 重置状态
  state.scale = 1
  state.draggedX = 0
  state.draggedY = 0
  state.dblTapZooming = false
  
  currentIndex.value = index
  emit('indexChange', index, { trigger })
}

function onSwiperChange(index: number) {
  if (currentIndex.value !== index) {
    const trigger = currentIndex.value < index ? 'next' : 'prev'
    switchImage(index, trigger)
  }
}

// =============== 键盘事件 ===============
function handleKeyDown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    handleClose('close-btn')
  } else if (e.key === 'ArrowLeft') {
    if (currentIndex.value > 0) {
      switchImage(currentIndex.value - 1, 'prev')
    }
  } else if (e.key === 'ArrowRight') {
    if (currentIndex.value < props.images.length - 1) {
      switchImage(currentIndex.value + 1, 'next')
    }
  }
}

// =============== 监听器 ===============
watch(() => props.visible, (newVal) => {
  isVisible.value = newVal
  if (!newVal) {
    beforeClose()
  }
})

watch(() => props.initialIndex, (newVal) => {
  if (newVal !== undefined && newVal !== currentIndex.value) {
    currentIndex.value = newVal
  }
})

// =============== 生命周期 ===============
onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
  if (dblTapTimer) {
    clearTimeout(dblTapTimer)
  }
})

// =============== 模板辅助函数 ===============
function getImageUrl(url: string): string {
  if (url.startsWith('http')) return url
  const baseUrl = getApiBaseUrl()
  if (url.startsWith('/')) return `${baseUrl}${url}`
  return `${baseUrl}/${url}`
}

function handleImageError() {
  handleClose('image')
}
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="isVisible"
        class="preview-overlay"
        @click="handleOverlayClick"
        @touchstart="handleTouchStart"
        @touchmove="handleTouchMove"
        @touchend="handleTouchEnd"
      >
        <!-- 遮罩 -->
        <div class="preview-mask"></div>
        
        <!-- 图片容器 -->
        <div 
          class="preview-content"
          :style="{ 
            touchAction: 'none',
            transform: containerTransform,
            transition: state.scale > 1 ? 'none' : 'transform 0.3s ease-out'
          }"
        >
          <div 
            v-for="(info, index) in imageInfoList" 
            :key="index"
            class="preview-item"
          >
            <img
              v-if="info.preload"
              :src="getImageUrl(info.image.url)"
              class="preview-image"
              :style="{
                transform: index === touchIndex && state.scale > 1 ? imageTransform : 'none',
                transition: index === touchIndex ? `transform ${imageTransitionDuration}` : 'none'
              }"
              draggable="false"
              @load="onImgLoad($event, index)"
              @error="handleImageError"
            />
          </div>
        </div>
        
        <!-- 导航栏 -->
        <div class="preview-nav">
          <!-- 关闭按钮 -->
          <div 
            v-if="closeBtn" 
            class="preview-nav-close"
            @click="handleCloseBtnClick"
          >
            ✕
          </div>
          
          <!-- 索引指示器 -->
          <div v-if="showIndexIndicator" class="preview-nav-index">
            {{ currentIndex + 1 }} / {{ images.length }}
          </div>
          
          <!-- 删除按钮 -->
          <div 
            v-if="deleteBtn" 
            class="preview-nav-delete"
            @click="handleDelete"
          >
            删除
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.preview-overlay {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.preview-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.95);
  z-index: 0;
}

.preview-content {
  flex: 1;
  display: flex;
  width: 100%;
  height: 100%;
  will-change: transform;
}

.preview-item {
  flex-shrink: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  user-select: none;
  -webkit-user-drag: none;
  will-change: transform;
  transform-origin: center center;
}

.preview-nav {
  position: absolute;
  bottom: max(env(safe-area-inset-bottom), 24px);
  left: 16px;
  right: 16px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 2;
  color: white;
}

.preview-nav-close,
.preview-nav-delete {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.2s;
}

.preview-nav-close:active,
.preview-nav-delete:active {
  background: rgba(255, 255, 255, 0.2);
}

.preview-nav-close svg,
.preview-nav-delete svg {
  width: 24px;
  height: 24px;
}

.preview-nav-index {
  padding: 8px 16px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 20px;
  font-size: 14px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
