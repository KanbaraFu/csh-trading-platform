// 图片工具：网络图片加载失败时回退到本地生成的渐变 SVG，保证页面永不出现空白图

const PALETTES = [
  ['#5eead4', '#0d9488'],
  ['#fdba74', '#ea580c'],
  ['#93c5fd', '#2563eb'],
  ['#fda4af', '#e11d48'],
  ['#c4b5fd', '#7c3aed'],
  ['#86efac', '#16a34a'],
  ['#a5f3fc', '#0891b2'],
  ['#fde68a', '#d97706'],
]

function hashSeed(seed) {
  const str = String(seed ?? '')
  let hash = 0
  for (let i = 0; i < str.length; i += 1) {
    hash = (hash * 31 + str.charCodeAt(i)) % 100000
  }
  return hash
}

/**
 * 生成渐变 SVG 占位封面（data URI，无需网络）
 */
export function svgCover(text = '淘学二手', seed = 0, size = 640) {
  const index = hashSeed(seed) % PALETTES.length
  const [from, to] = PALETTES[index]
  const label = String(text).replace(/[<>&"]/g, '').slice(0, 10)
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 ${size} ${size}">
<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
<stop offset="0%" stop-color="${from}"/><stop offset="100%" stop-color="${to}"/>
</linearGradient></defs>
<rect width="${size}" height="${size}" fill="url(#g)"/>
<circle cx="${size * 0.82}" cy="${size * 0.2}" r="${size * 0.16}" fill="#ffffff" opacity="0.16"/>
<circle cx="${size * 0.18}" cy="${size * 0.78}" r="${size * 0.22}" fill="#ffffff" opacity="0.12"/>
<text x="50%" y="52%" text-anchor="middle" font-family="PingFang SC, Microsoft YaHei, sans-serif"
font-size="${Math.round(size * 0.11)}" font-weight="600" fill="#ffffff" opacity="0.95">${label}</text>
</svg>`
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}

/**
 * 商品主图地址：使用 picsum 稳定随机图，失败时由组件回退到 svgCover
 */
export function productCover(id) {
  return `https://picsum.photos/seed/campus-${id}/720/720`
}

/**
 * 用户头像地址，失败时回退到首字母圆形头像
 */
export function avatarUrl(seed) {
  return `https://picsum.photos/seed/avatar-${seed}/160/160`
}

export function svgAvatar(name = '同学', seed = 0, size = 160) {
  const index = hashSeed(seed) % PALETTES.length
  const [from, to] = PALETTES[index]
  const label = String(name).slice(0, 1)
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 ${size} ${size}">
<defs><linearGradient id="a" x1="0" y1="0" x2="1" y2="1">
<stop offset="0%" stop-color="${from}"/><stop offset="100%" stop-color="${to}"/>
</linearGradient></defs>
<rect width="${size}" height="${size}" fill="url(#a)"/>
<text x="50%" y="56%" text-anchor="middle" font-family="PingFang SC, Microsoft YaHei, sans-serif"
font-size="${Math.round(size * 0.46)}" font-weight="600" fill="#ffffff">${label}</text>
</svg>`
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}
