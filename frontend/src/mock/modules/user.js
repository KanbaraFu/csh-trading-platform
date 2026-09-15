// 用户与认证模块 Mock：注册 / 登录 / 验证码 / 个人资料 / 收货地址
import { TOKEN_KEY, USER_ID_KEY, db, nextId, setLogin, clearLogin } from '../index'
import { decorateProduct, fail, nowText, paginate, requireLogin, stripPassword } from '../helpers'
import { avatarUrl } from '@/utils/image'

const PHONE_RE = /^1\d{10}$/
// 模拟 Redis 键 captcha:register:{phone}
const captchaStore = new Map()

export function sendCaptcha({ body }) {
  const phone = String(body.phone || '').trim()
  if (!PHONE_RE.test(phone)) fail('请输入正确的 11 位手机号')
  const code = String(Math.floor(100000 + Math.random() * 900000))
  captchaStore.set(phone, { code, createTime: Date.now() })
  // 演示环境直接把验证码返回，便于快速体验
  return { phone, code, expireSeconds: 300 }
}

function verifyCaptcha(phone, code) {
  const record = captchaStore.get(phone)
  const valid = record && record.code === String(code || '')
  // 演示环境放行固定验证码，避免刷新后无法注册
  if (!valid && String(code) !== '123456') {
    fail('验证码错误或已过期，请重新获取')
  }
  captchaStore.delete(phone)
}

export function register({ body }) {
  const phone = String(body.phone || '').trim()
  const { password, code, nickname } = body
  if (!PHONE_RE.test(phone)) fail('请输入正确的 11 位手机号')
  if (!password || String(password).length < 6) fail('密码至少 6 位')
  verifyCaptcha(phone, code)
  if (db().users.some((user) => user.username === phone)) fail('该手机号已注册，请直接登录')

  const id = nextId('user')
  const user = {
    id,
    username: phone,
    password: String(password),
    nickname: nickname || `同学${phone.slice(-4)}`,
    avatar: avatarUrl(id),
    phone,
    gender: 0,
    status: 1,
    student_no: '',
    college: '',
    auth_status: 0,
    bio: '',
    create_time: nowText(),
    update_time: nowText(),
  }
  db().users.push(user)
  setLogin(user.id)
  return { token: localStorage.getItem(TOKEN_KEY), user: stripPassword(user) }
}

export function login({ body }) {
  const account = String(body.phone || '').trim()
  const user = db().users.find((item) => item.username === account || item.phone === account)
  if (!user) fail('账号未注册，请先注册或检查手机号')
  if (String(user.password) !== String(body.password || '')) fail('密码错误，请重新输入')
  setLogin(user.id)
  return { token: localStorage.getItem(TOKEN_KEY), user: stripPassword(user) }
}

export function logout() {
  clearLogin()
  return { userId: Number(localStorage.getItem(USER_ID_KEY) || 0) }
}

export function getMe() {
  const user = requireLogin()
  return stripPassword(user)
}

export function updateMe({ body }) {
  const user = requireLogin()
  const fields = ['nickname', 'avatar', 'phone', 'gender', 'bio', 'student_no', 'college']
  fields.forEach((field) => {
    if (body[field] !== undefined) user[field] = body[field]
  })
  user.update_time = nowText()
  return stripPassword(user)
}

export function getMyProducts({ query }) {
  const user = requireLogin()
  const list = db()
    .products.filter((product) => product.seller_id === user.id)
    .sort((a, b) => (a.create_time < b.create_time ? 1 : -1))
  const keyword = String(query.keyword || '').trim()
  const filtered = keyword ? list.filter((item) => item.title.includes(keyword)) : list
  const paged = paginate(filtered, query.pageNum || 1, query.pageSize || 5)
  return { ...paged, records: paged.records.map(decorateProduct) }
}

export function getAddresses({ query }) {
  const user = requireLogin()
  const list = db()
    .addresses.filter((item) => item.user_id === user.id)
    .sort((a, b) => b.is_default - a.is_default || (a.create_time < b.create_time ? 1 : -1))
  const selected = query.addressId ? list.find((item) => item.id === Number(query.addressId)) : null
  return { records: list, selected_id: selected?.id ?? list.find((item) => item.is_default === 1)?.id ?? list[0]?.id ?? 0 }
}

export function createAddress({ body }) {
  const user = requireLogin()
  if (!body.receiver_name) fail('请填写收货人姓名')
  if (!PHONE_RE.test(String(body.phone || ''))) fail('请填写正确的联系电话')
  if (!body.region) fail('请选择所在区域 / 楼栋')
  if (!body.detail) fail('请填写详细地址')
  const id = nextId('address')
  const address = {
    id,
    user_id: user.id,
    receiver_name: body.receiver_name,
    phone: String(body.phone),
    region: body.region,
    detail: body.detail,
    is_default: 0,
    create_time: nowText(),
  }
  const list = db().addresses
  if (body.is_default || list.filter((item) => item.user_id === user.id).length === 0) {
    list.filter((item) => item.user_id === user.id).forEach((item) => { item.is_default = 0 })
    address.is_default = 1
  }
  list.push(address)
  return address
}

export function updateAddress({ route, body }) {
  const user = requireLogin()
  const address = db().addresses.find((item) => item.id === Number(route.id) && item.user_id === user.id)
  if (!address) fail('地址不存在或无权修改')
  ;['receiver_name', 'phone', 'region', 'detail'].forEach((field) => {
    if (body[field] !== undefined) address[field] = body[field]
  })
  if (body.is_default) {
    db().addresses.filter((item) => item.user_id === user.id).forEach((item) => { item.is_default = 0 })
    address.is_default = 1
  }
  return address
}

export function deleteAddress({ route }) {
  const user = requireLogin()
  const list = db().addresses
  const index = list.findIndex((item) => item.id === Number(route.id) && item.user_id === user.id)
  if (index < 0) fail('地址不存在或无权删除')
  const [removed] = list.splice(index, 1)
  if (removed.is_default) {
    const rest = list.find((item) => item.user_id === user.id)
    if (rest) rest.is_default = 1
  }
  return { id: removed.id }
}
