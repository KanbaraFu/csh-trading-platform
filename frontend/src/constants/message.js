// 消息领域常量：消息类型映射与分类 Tab

// 消息类型：system 系统通知 / trade 交易消息 / comment 评论回复
export const MESSAGE_TYPES = {
  system: { label: '系统通知', color: '#3b82f6', icon: 'Bell' },
  trade: { label: '交易消息', color: '#14b8a6', icon: 'ShoppingCart' },
  comment: { label: '评论回复', color: '#ff7a45', icon: 'ChatLineSquare' },
}

export function messageTypeLabel(type) {
  return MESSAGE_TYPES[type]?.label ?? '通知'
}

export function messageTypeColor(type) {
  return MESSAGE_TYPES[type]?.color ?? '#6b7280'
}

export function messageTypeIcon(type) {
  return MESSAGE_TYPES[type]?.icon ?? 'Bell'
}

// 消息中心分类 Tab
export const MESSAGE_TABS = [
  { label: '全部消息', value: 'all' },
  { label: '系统通知', value: 'system' },
  { label: '交易消息', value: 'trade' },
  { label: '评论回复', value: 'comment' },
]

// 分类图标（分类表 name 与图标对应，用于首页分类导航）
export const CATEGORY_ICONS = {
  教材书籍: 'Reading',
  数码电子: 'Monitor',
  生活用品: 'House',
  服饰鞋包: 'Handbag',
  运动户外: 'Basketball',
  美妆护肤: 'MagicStick',
  乐器文娱: 'Headset',
  其他闲置: 'Box',
}

export function categoryIcon(name) {
  return CATEGORY_ICONS[name] ?? 'Grid'
}
