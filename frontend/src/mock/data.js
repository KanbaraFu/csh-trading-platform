// Mock 基准数据：分类 / 用户 / 商品 / 图片 / 评论 / 消息 / 地址 / 订单
import { formatDateTime } from '@/utils/format'
import { avatarUrl, productCover } from '@/utils/image'

const DAY = 86400000

function timeAgo(days, hours = 0) {
  return formatDateTime(new Date(Date.now() - days * DAY - hours * 3600000))
}

function futureDays(days) {
  return formatDateTime(new Date(Date.now() + days * DAY))
}

// ---------------------------------------------------------------- 用户
function createUsers() {
  const seeds = [
    [1, '13800000001', '林小满', '2022010305', '计算机科学与技术学院', 2, '计科大四，毕业清仓～专业书、数码、生活用品都出', 120],
    [2, '13800000002', '陈子昂', '2021030112', '电子信息工程学院', 1, '数码发烧友，手里的设备都保养得很好', 200],
    [3, '13800000003', '苏晴', '2023050428', '外国语学院', 2, '爱读书也爱断舍离，书本都是九成新以上', 160],
    [4, '13800000004', '周牧', '2022040917', '体育学院', 1, '篮球队的，运动装备低价转让，可当面验货', 180],
    [5, '13800000005', '何知远', '2021060233', '数学与统计学院', 1, '考研上岸啦，全套资料打包出，可小刀', 140],
    [6, '13800000006', '王梓萱', '2023070755', '设计艺术学院', 2, '衣柜大清理，衣服鞋子基本都是冲动消费', 150],
  ]
  return seeds.map(([id, username, nickname, studentNo, college, gender, bio, days]) => ({
    id,
    username,
    password: '123456',
    nickname,
    avatar: avatarUrl(id),
    phone: username,
    gender,
    status: 1,
    student_no: studentNo,
    college,
    auth_status: id <= 3 ? 1 : 0,
    bio,
    create_time: timeAgo(days),
    update_time: timeAgo(days / 4),
  }))
}

// ---------------------------------------------------------------- 分类
function createCategories() {
  const seeds = [
    [1, '教材书籍'], [2, '数码电子'], [3, '生活用品'], [4, '服饰鞋包'],
    [5, '运动户外'], [6, '美妆护肤'], [7, '乐器文娱'], [8, '其他闲置'],
  ]
  return seeds.map(([id, name]) => ({
    id,
    name,
    sort: id,
    status: 1,
    create_time: timeAgo(300),
  }))
}

// ---------------------------------------------------------------- 商品
// [分类, 卖家, 标题, 现价, 原价, 成色, 交易地点, 浏览量, 销量, 描述]
const PRODUCT_SEED = [
  [1, 5, '考研英语一 历年真题（2010-2024）', 18, 45, '九成新', '图书馆一楼大厅', 1286, 32, '真题册只做过前两套，后面全新未翻阅，附赠自己整理的长难句笔记和作文模板。'],
  [1, 3, '高等数学 同济第七版 上下册', 20, 79, '八成新', '第一教学楼门口', 942, 21, '书角有轻微磨损，内页无笔记无划线，适合大一大二同学打基础。'],
  [1, 5, '考研政治 精讲精练 + 1000 题', 30, 59, '八成新', '南苑食堂门口', 768, 18, '肖秀荣全家桶，1000 题用铅笔做过一遍已擦干净，可配合我的错题本一起复习。'],
  [1, 3, '数据结构与算法分析（C 语言版）', 35, 68, '九成新', '图书馆一楼大厅', 654, 12, '计算机专业课指定教材，内页干净，附 Course 习题解答电子版，可发网盘链接。'],
  [1, 4, '大学物理实验报告册（全新空白）', 8, 22, '全新未拆封', '西区快递驿站', 421, 9, '买重了，塑封都没拆，实验课必备，校门口随到随取。'],
  [2, 2, 'iPad Air 4 64G 深空灰 含原装笔', 1980, 4399, '九成新', '东区宿舍 6 栋', 3286, 5, '大二买的，日常贴膜带壳，无磕碰无拆修，电池健康 92%，配 Apple Pencil 二代一起走。'],
  [2, 2, '罗技 K380 蓝牙键盘 白色', 89, 199, '九成新', '东区宿舍 6 栋', 1874, 26, '三设备切换顺滑，键程舒适，电池刚换过新的，包装盒和说明书都在。'],
  [2, 2, '小米显示器 24 英寸 1080P 窄边框', 350, 799, '八成新', '东区宿舍 6 栋', 1420, 8, '做作业外接笔记本刚好，屏幕无亮点无坏道，附 HDMI 线和支架，仅限校内自取。'],
  [2, 2, '索尼 WH-1000XM4 头戴降噪耳机', 1280, 2299, '九成新', '图书馆一楼大厅', 2765, 3, '图书馆自习神器，降噪依旧强悍，耳罩无起皮，配原装收纳包与充电线。'],
  [2, 1, 'AirPods 二代 有线充电盒版', 420, 1246, '九成新', '南苑食堂门口', 2103, 11, '左右耳音量正常，无杂音，已用酒精棉片消毒，充电盒有轻微使用痕。'],
  [2, 1, '绿联 65W 氮化镓充电器 双 C 口', 45, 99, '几乎全新', '校内可送达', 986, 34, '买来出差用，实际只用了三四次，能同时给笔记本和手机快充，出门带一个就够。'],
  [3, 1, '宿舍护眼台灯 三档色温可调', 39, 89, '九成新', '东区宿舍 6 栋', 1123, 17, 'USB 供电，不频闪，看书刷题不伤眼，灯臂可多角度弯折，还剩一学期毕业出掉。'],
  [3, 6, '折叠收纳箱 大号两只（可叠放）', 25, 69, '九成新', '西区快递驿站', 632, 14, '搬家剩下的神器，能装下一整个衣柜的衣物，折叠后不占地方。'],
  [3, 6, '小米电热水壶 1.5L 食品级内胆', 45, 99, '八成新', '西区快递驿站', 874, 9, '烧水快，内胆无水垢（一直用净水），毕业搬家出，附原装底座。'],
  [3, 6, '全棉四件套 1.5 米床 浅灰格纹', 80, 259, '几乎全新', '西区快递驿站', 706, 6, '洗过一次就收起来了，尺寸不合适宿舍床，无勾丝无褪色，拍照实物。'],
  [4, 6, '优衣库 轻薄羽绒服 M 码 藏青', 199, 599, '九成新', '南苑食堂门口', 1652, 7, '南方冬天一件就够，蓬松度很好，袖口无起球，已干洗可放心穿。'],
  [4, 6, '阿迪达斯 运动卫衣 男 L 码', 129, 399, '九成新', '南苑食堂门口', 1088, 10, '经典三道杠，穿过三四次，领口袖口无松垮无起球，送同款收纳袋。'],
  [4, 6, '全新马丁靴 39 码（买大了一码）', 260, 699, '全新未拆封', '东区宿舍 6 栋', 1436, 2, '去年双十一冲动下单，试都没试过，鞋盒吊牌齐全，可当面试穿。'],
  [4, 3, '帆布单肩包 米白色 大容量', 35, 129, '八成新', '图书馆一楼大厅', 592, 15, '装得下 14 寸笔记本加两本书，内袋多，背带去店里加固过一次更结实。'],
  [5, 4, '捷安特 山地自行车 24 速 铝合金车架', 680, 1580, '八成新', '体育馆北门', 2384, 4, '车况很好，刚换过前后刹车皮和内胎，车锁、车筐一起送，仅限校内交易。'],
  [5, 4, '李宁 5 号篮球 室内外通用', 60, 129, '九成新', '体育馆北门', 968, 13, '球队换新球，这颗只打过几次，球感软弹，气密性正常，附送打气筒。'],
  [5, 4, '羽毛球拍 双拍套装（含手胶）', 75, 199, '九成新', '体育馆北门', 782, 11, '入门级别很够用，拍框无变形，已更换新吸汗手胶，送三个训练球。'],
  [5, 6, '迪卡侬 瑜伽垫 加厚 8mm', 45, 99, '几乎全新', '西区快递驿站', 534, 16, '买来只铺过两三次，已用湿巾擦净晾干，附收纳背带，宿舍拉伸健身刚好。'],
  [6, 6, '兰蔻小黑瓶精华 30ml（专柜小样）', 220, 780, '全新未拆封', '南苑食堂门口', 1892, 3, '专柜满赠拿到的小样，全新未拆，生产日期 2026 年，介意小样勿拍。'],
  [6, 3, '珀莱雅 双抗精华 30ml 剩约八成', 90, 239, '九成新', '图书馆一楼大厅', 654, 8, '用了一个月觉得不适合我的肤质，剩八成左右，滴管干净，附原盒。'],
  [7, 6, '民谣吉他 41 寸 单板（含包与变调夹）', 380, 899, '八成新', '东区宿舍 6 栋', 1567, 2, '入门一直用它练，音准稳定无开裂，附琴包、变调夹、背带和备用琴弦。'],
  [7, 6, '尤克里里 23 寸 桃花芯木', 150, 399, '九成新', '东区宿舍 6 栋', 894, 6, '音色清亮，外观几乎无痕，适合零基础练手，送一本自学教材。'],
  [7, 1, '索尼 PS4 原装手柄 黑色', 120, 349, '八成新', '南苑食堂门口', 1032, 5, '按键回弹正常，摇杆无漂移，接口无松动，附一条原装充电线。'],
  [8, 1, '搬家折叠小推车（承重 50kg）', 30, 79, '九成新', '东区宿舍 6 栋', 476, 19, '毕业搬宿舍神器，折叠后能塞进柜子缝，拉杆顺滑，轮胎无破损。'],
  [8, 6, '全新保温杯 500ml 不锈钢（未使用）', 25, 89, '全新未拆封', '西区快递驿站', 388, 22, '社团活动发的纪念款，一直没拆，保温效果很好，颜色是雾霾蓝。'],
  [2, 1, '小米无线鼠标 静音版 白色', 29, 69, '九成新', '校内可送达', 640, 12, '写代码一直在用，左右键声音很小，滚轮顺滑，附 USB 接收器和原装电池。'],
  [3, 1, '宿舍折叠椅 承重 120kg', 55, 149, '八成新', '东区宿舍 6 栋', 512, 5, '椅面完好无破损，折叠后厚度只有 10cm，塞床底就行，坐着很稳。'],
]

function imageUrl(id, n) {
  return `https://picsum.photos/seed/campus-${id}-${n}/720/720`
}

function createProducts() {
  return PRODUCT_SEED.map((seed, index) => {
    const [categoryId, sellerId, title, price, originalPrice, condition, location, viewCount, salesCount, description] = seed
    const id = index + 1
    return {
      id,
      seller_id: sellerId,
      category_id: categoryId,
      title,
      description,
      price,
      original_price: originalPrice,
      stock: (id % 3) + 1,
      cover: productCover(id),
      status: (id === 27 ? 2 : id === 30 ? 0 : 1),
      view_count: viewCount,
      sales_count: salesCount,
      condition,
      location,
      create_time: timeAgo(Number((30 - index * 0.9).toFixed(1)), index % 12),
      update_time: timeAgo(Number((28 - index * 0.9).toFixed(1)), index % 12),
    }
  })
}

function createProductImages(products) {
  const images = []
  products.forEach((product) => {
    for (let n = 1; n <= 4; n += 1) {
      images.push({
        id: product.id * 10 + n,
        product_id: product.id,
        url: imageUrl(product.id, n),
        sort: n,
      })
    }
  })
  return images
}

// ---------------------------------------------------------------- 评论
function createComments() {
  const seeds = [
    [1, 3, '请问笔记是手写的还是电子版呀？如果是电子版想要一份～', 0, null, 6],
    [1, 1, '笔记是手写的长难句整理，随书一起给你，电子版作文模板我也可以发网盘。', 1, 3, 5],
    [1, 4, '真题册还在吗？我明天想去图书馆当面看一下可以吗', 0, null, 2],
    [2, 2, '请问上下册都有吗，有没有老师的课件配套', 0, null, 4],
    [2, 1, '上下册都有的，课件我没有哦，不过书上的例题讲解已经很全了。', 2, 2, 3],
    [6, 5, 'iPad 电池健康还在 92% 的话很值了，请问支持当面验机吗', 0, null, 3],
    [6, 2, '当然可以，约在宿舍楼下或者图书馆都行，当面看电池和外观。', 6, 5, 2],
    [7, 4, '键盘支持 Mac 的 Fn 组合键吗，想配平板用', 0, null, 1],
    [8, 3, '显示器能出到 320 吗，我今天可以去拿', 0, null, 5],
    [12, 6, '台灯还剩吗？宿舍晚上熄灯后能不能用充电宝供电', 0, null, 7],
    [12, 1, '还在的，它本来就是 USB 供电，接充电宝完全没问题。', 12, 6, 6],
    [20, 5, '自行车有发票吗，怕过不了校门安保', 0, null, 4],
    [20, 4, '发票找不到了，但是车架号我能拍给你，校内骑了两年没被查过。', 20, 5, 3],
    [26, 3, '吉他弦是原装的吗，可以顺便教两个和弦吗哈哈', 0, null, 2],
    [26, 6, '弦上个月刚换的新弦，教和弦没问题，当面教到你会为止。', 26, 3, 1],
    [1, 2, '请问真题册是英语一还是英语二？我想确认下版本', 0, null, 7],
    [1, 6, '书有点厚，能帮忙送到女生宿舍楼下吗', 0, null, 6],
    [1, 5, '已经下单啦，麻烦帮我留着，周末来取', 0, null, 4],
    [1, 4, '笔记部分有多少页呀，主要想看长难句整理', 0, null, 2],
  ]
  return seeds.map(([productId, userId, content, parentId, replyUserId, days], index) => ({
    id: index + 1,
    product_id: productId,
    user_id: userId,
    content,
    parent_id: parentId,
    reply_user_id: replyUserId,
    create_time: timeAgo(days, index % 9),
  }))
}

// ---------------------------------------------------------------- 消息
function createMessages() {
  const seeds = [
    ['trade', '订单待支付提醒', '你有一笔订单尚未支付，请在 30 分钟内完成支付，逾期订单将自动取消。', 1, 0, 0.1],
    ['comment', '苏晴 回复了你的评论', '“笔记是手写的长难句整理，随书一起给你……”', 1, 0, 0.3],
    ['trade', '买家已付款', '订单 2026091315218 买家已付款，请尽快安排发货并联系买家约定交易时间。', 2, 1, 1.2],
    ['system', '校园认证通过', '恭喜，你的学生身份认证已通过审核，现在可以发布商品并参与交易啦。', 0, 1, 2],
    ['comment', '陈子昂 回复了你的评论', '“当然可以，约在宿舍楼下或者图书馆都行，当面看电池和外观。”', 6, 0, 2.4],
    ['trade', '卖家已发货', '订单 2026091188734 卖家已交付商品，收到货后请及时确认收货。', 3, 0, 3.1],
    ['system', '平台交易规范更新', '为保障同学权益，本学期起所有交易建议在校内公共区域当面验货，请知悉。', 0, 1, 5],
    ['trade', '交易完成', '订单 2026090591207 已完成交易，欢迎在商品详情页留下你的评价。', 4, 1, 8],
    ['comment', '周牧 回复了你的评论', '“发票找不到了，但是车架号我能拍给你，校内骑了两年没被查过。”', 20, 1, 9],
    ['system', '闲置清理活动开启', '「毕业季清仓」专题已上线，发布闲置即可获得首页推荐位曝光。', 0, 0, 12],
  ]
  return seeds.map(([type, title, content, bizId, isRead, days], index) => ({
    id: index + 1,
    user_id: 1,
    type,
    title,
    content,
    biz_id: bizId,
    is_read: isRead,
    create_time: timeAgo(days, index % 7),
  }))
}

// ---------------------------------------------------------------- 收货地址
function createAddresses() {
  return [
    {
      id: 1,
      user_id: 1,
      receiver_name: '林小满',
      phone: '13800000001',
      region: '东区宿舍 6 栋',
      detail: '301 室 3 号床（楼下有快递暂存架，可直接放架上发消息）',
      is_default: 1,
      create_time: timeAgo(90),
    },
    {
      id: 2,
      user_id: 1,
      receiver_name: '林小满',
      phone: '13800000001',
      region: '计算机学院实验楼',
      detail: 'B302 实验室（工作日 9:00-18:00 在，周末勿送）',
      is_default: 0,
      create_time: timeAgo(45),
    },
    {
      id: 3,
      user_id: 1,
      receiver_name: '林建国',
      phone: '13900000009',
      region: '南苑家属区',
      detail: '12 栋 502，假期在家时使用',
      is_default: 0,
      create_time: timeAgo(20),
    },
  ]
}

// ---------------------------------------------------------------- 订单
// [状态, 卖家, 备注, 下单距今天数, [[商品, 数量], ...], 买家(默认当前用户)]
const ORDER_SEED = [
  [0, 3, '希望明天中午在校门口当面交易，谢谢！', 0.05, [[3, 1]]],
  [1, 2, '周末在图书馆一楼大厅碰面可以吗？', 1.2, [[9, 1]]],
  [2, 2, '发到东区宿舍 6 栋就行，麻烦提前联系。', 3.1, [[6, 1]]],
  [3, 4, '篮球帮我打好气，谢谢师兄！', 8, [[21, 1], [22, 1]]],
  [3, 5, '书收到啦，笔记很有用，五星好评～', 15, [[1, 1]]],
  [4, 6, '拍错了，不好意思先取消吧。', 6.5, [[30, 1]]],
  [1, 1, '同学你好，我下午三点到东区宿舍 6 栋楼下取，方便吗？', 0.8, [[12, 1]], 2],
]

function buildAddressSnapshot() {
  return '林小满 13800000001 东区宿舍 6 栋 301 室 3 号床'
}

function createOrders(products, addresses) {
  const orders = []
  const items = []
  ORDER_SEED.forEach(([status, sellerId, remark, days, lines, buyerId = 1], index) => {
    const id = index + 1
    const orderNo = `2026${String(900 + id)}${String(100000 + id * 137).slice(0, 6)}`
    let total = 0
    lines.forEach(([productId, quantity], lineIndex) => {
      const product = products.find((item) => item.id === productId)
      if (!product) return
      const amount = Number((product.price * quantity).toFixed(2))
      total += amount
      items.push({
        id: id * 10 + lineIndex + 1,
        order_id: id,
        product_id: product.id,
        product_title: product.title,
        product_cover: product.cover,
        price: product.price,
        quantity,
        total_amount: amount,
      })
    })
    const address = addresses.find((item) => item.is_default === 1)
    orders.push({
      id,
      order_no: orderNo,
      buyer_id: buyerId,
      seller_id: sellerId,
      total_amount: Number(total.toFixed(2)),
      pay_amount: Number((total - (index === 3 ? 5 : 0)).toFixed(2)),
      status,
      address_snapshot: address ? `${address.receiver_name} ${address.phone} ${address.region} ${address.detail}` : buildAddressSnapshot(),
      remark,
      pay_method: status === 0 ? null : 'campus_card',
      pay_time: status === 0 ? null : timeAgo(days, 2),
      create_time: timeAgo(days, 6),
      update_time: timeAgo(days > 1 ? days - 1 : days, 3),
    })
  })
  return { orders, items }
}

// ---------------------------------------------------------------- 聚合
export function createSeedData() {
  const users = createUsers()
  const categories = createCategories()
  const products = createProducts()
  const productImages = createProductImages(products)
  const comments = createComments()
  const messages = createMessages()
  const addresses = createAddresses()
  const { orders, items } = createOrders(products, addresses)

  return {
    users,
    categories,
    products,
    productImages,
    comments,
    messages,
    addresses,
    orders,
    orderItems: items,
    favorites: [6, 20, 7, 9, 13, 16, 21, 23, 26, 2, 4, 14].map((productId, index) => ({
      id: index + 1,
      user_id: 1,
      product_id: productId,
      create_time: timeAgo(12 - index),
    })),
    carts: [
      { id: 1, user_id: 1, product_id: 7, quantity: 1, selected: 1, create_time: timeAgo(1), update_time: timeAgo(0.2) },
      { id: 2, user_id: 1, product_id: 8, quantity: 1, selected: 1, create_time: timeAgo(0.9), update_time: timeAgo(0.3) },
    ],
    hotWords: [
      { word: '考研英语真题', score: 186 },
      { word: 'iPad', score: 164 },
      { word: '机械键盘', score: 142 },
      { word: '自行车', score: 128 },
      { word: '护眼台灯', score: 96 },
      { word: '高等数学教材', score: 88 },
      { word: '蓝牙耳机', score: 76 },
      { word: '羽绒服', score: 64 },
      { word: '吉他', score: 52 },
      { word: '显示器', score: 45 },
      { word: '羽毛球拍', score: 38 },
      { word: '保温杯', score: 26 },
    ],
    nextIds: {
      user: users.length + 1,
      product: products.length + 1,
      comment: comments.length + 1,
      message: messages.length + 1,
      address: addresses.length + 1,
      order: orders.length + 1,
      favorite: 13,
      cart: 3,
    },
    expiryHint: futureDays(30),
  }
}
