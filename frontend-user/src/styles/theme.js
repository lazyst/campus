/**
 * 校园互助平台 - 共享组件库
 * 包含所有UI设计规范和可复用组件
 */

// ==================== 颜色系统 ====================
export const colors = {
  primary: {
    50: '#EEF2FF',
    100: '#E0E7FF',
    200: '#C7D2FE',
    300: '#A5B4FC',
    400: '#818CF8',
    500: '#6366F1', // 主色
    600: '#4F46E5',
    700: '#4338CA',
    800: '#3730A3',
    900: '#312E81',
  },
  secondary: {
    50: '#FDF2F8',
    100: '#FCE7F3',
    200: '#FBCFE8',
    300: '#F9A8D4',
    400: '#F472B6',
    500: '#EC4899', // 粉色
    600: '#DB2777',
  },
  success: {
    light: '#D1FAE5',
    main: '#10B981',
    dark: '#059669',
  },
  warning: {
    light: '#FEF3C7',
    main: '#F59E0B',
    dark: '#D97706',
  },
  error: {
    light: '#FEE2E2',
    main: '#EF4444',
    dark: '#DC2626',
  },
  neutral: {
    0: '#FFFFFF',
    50: '#F8FAFC',
    100: '#F1F5F9',
    200: '#E2E8F0',
    300: '#CBD5E1',
    400: '#94A3B8',
    500: '#64748B',
    600: '#475569',
    700: '#334155',
    800: '#1E293B',
    900: '#0F172A',
  }
};

// ==================== 字体系统 ====================
export const fontSize = {
  xs: 12,
  sm: 13,
  base: 14,
  lg: 15,
  xl: 16,
  '2xl': 18,
  '3xl': 20,
  '4xl': 24,
  '5xl': 28,
  '6xl': 32,
};

export const fontWeight = {
  normal: '400',
  medium: '500',
  semibold: '600',
  bold: '700',
};

export const lineHeight = {
  tight: 1.25,
  normal: 1.5,
  relaxed: 1.75,
};

// ==================== 间距系统 ====================
export const spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  '2xl': 24,
  '3xl': 32,
  '4xl': 40,
};

// ==================== 圆角系统 ====================
export const borderRadius = {
  none: 0,
  sm: 4,
  md: 8,
  lg: 12,
  xl: 16,
  '2xl': 20,
  '3xl': 24,
  full: 9999,
};

// ==================== 阴影系统 ====================
export const shadows = {
  sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
  md: '0 4px 6px -1px rgba(99, 102, 241, 0.1), 0 2px 4px -1px rgba(99, 102, 241, 0.06)',
  lg: '0 10px 15px -3px rgba(99, 102, 241, 0.1), 0 4px 6px -2px rgba(99, 102, 241, 0.05)',
  xl: '0 20px 25px -5px rgba(99, 102, 241, 0.1), 0 10px 10px -5px rgba(99, 102, 241, 0.04)',
};

// ==================== 过渡动画 ====================
export const transitions = {
  fast: '150ms ease',
  normal: '250ms ease',
  slow: '350ms ease',
};

// ==================== 设备规格 ====================
export const device = {
  width: 375,
  height: 812,
  statusBar: 44,
  bottomNav: 60,
};

// ==================== 分类标签颜色 ====================
export const categoryColors = {
  '交流': { bg: colors.primary[50], text: colors.primary[500] },
  '学习': { bg: colors.warning.light, text: colors.warning.main },
  '搭子': { bg: colors.secondary[100], text: colors.secondary[500] },
  '闲置': { bg: colors.success.light, text: colors.success.main },
  '电脑数码': { bg: colors.primary[50], text: colors.primary[500] },
  '学习资料': { bg: colors.warning.light, text: colors.warning.main },
  '生活用品': { bg: colors.neutral[100], text: colors.neutral[600] },
  '出行工具': { bg: colors.success.light, text: colors.success.main },
};

// ==================== 组件样式工厂 ====================
export const styles = {
  // 卡片样式
  card: {
    backgroundColor: colors.neutral[0],
    borderRadius: borderRadius.lg,
    padding: spacing.lg,
    boxShadow: shadows.md,
  },

  // 按钮样式
  button: {
    primary: {
      backgroundColor: colors.primary[500],
      color: colors.neutral[0],
      borderRadius: borderRadius.xl,
      height: 52,
      fontSize: fontSize.lg,
      fontWeight: fontWeight.semibold,
    },
    secondary: {
      backgroundColor: colors.neutral[100],
      color: colors.neutral[600],
      borderRadius: borderRadius.xl,
      height: 52,
      fontSize: fontSize.lg,
    },
  },

  // 输入框样式
  input: {
    backgroundColor: colors.neutral[50],
    borderRadius: borderRadius.md,
    height: 52,
    paddingHorizontal: spacing.md,
    fontSize: fontSize.base,
  },

  // 标签样式
  tag: {
    paddingHorizontal: spacing.sm,
    paddingVertical: 2,
    borderRadius: borderRadius.sm,
    fontSize: fontSize.xs,
    fontWeight: fontWeight.medium,
  },
};

// ==================== 导出的样式对象 ====================
export const theme = {
  colors,
  fontSize,
  fontWeight,
  lineHeight,
  spacing,
  borderRadius,
  shadows,
  transitions,
  device,
  categoryColors,
  styles,
};

export default theme;
