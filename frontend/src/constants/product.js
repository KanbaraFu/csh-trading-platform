// 商品领域常量：状态、成色、排序方式、价格区间

// 商品状态：0 已下架 / 1 在售 / 2 已售
export const PRODUCT_STATUS = {
  0: { label: '已下架', type: 'info' },
  1: { label: '在售', type: 'success' },
  2: { label: '已售出', type: 'warning' },
}

export function productStatusLabel(status) {
  return PRODUCT_STATUS[status]?.label ?? '未知'
}

export function productStatusType(status) {
  return PRODUCT_STATUS[status]?.type ?? 'info'
}

// 成色选项（商品发布 / 详情展示）
export const CONDITION_OPTIONS = [
  '全新未拆封',
  '几乎全新',
  '九成新',
  '八成新',
  '七成新',
  '有使用痕迹',
]

// 排序方式
export const SORT_OPTIONS = [
  { label: '最新发布', value: 'new' },
  { label: '价格从低到高', value: 'price_asc' },
  { label: '价格从高到低', value: 'price_desc' },
  { label: '热度优先', value: 'hot' },
]

// 价格区间筛选
export const PRICE_RANGES = [
  { label: '全部价格', min: null, max: null },
  { label: '20 元以下', min: 0, max: 20 },
  { label: '20 - 50 元', min: 20, max: 50 },
  { label: '50 - 100 元', min: 50, max: 100 },
  { label: '100 - 500 元', min: 100, max: 500 },
  { label: '500 - 1000 元', min: 500, max: 1000 },
  { label: '1000 元以上', min: 1000, max: null },
]

// 交易地点候选
export const TRADE_PLACES = [
  '第一教学楼门口',
  '图书馆一楼大厅',
  '南苑食堂门口',
  '东区宿舍 6 栋',
  '西区快递驿站',
  '体育馆北门',
  '校内可送达',
]

// 搜索热词默认兜底（真实数据来自 /api/search/hot-words）
export const DEFAULT_HOT_WORDS = [
  '考研英语真题',
  'iPad',
  '机械键盘',
  '自行车',
  '台灯',
  '高等数学教材',
  '蓝牙耳机',
  '羽绒服',
  '吉他',
  '显示器',
]
