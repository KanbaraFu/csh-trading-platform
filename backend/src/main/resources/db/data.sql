-- =============================================================================
-- 校园二手交易平台 —— 种子数据（最小可演示数据集）
--
-- 说明：
--   1. 本文件由 SpringBoot 启动时自动执行（spring.sql.init），也可手动导入：
--        mysql -u root -p campus_secondhand < data.sql
--   2. 全部使用 INSERT IGNORE + 显式主键，重复执行不会重复插入、不会覆盖已有数据。
--      因此「启动自动灌数据」可以长期开着，你后续自己新增的数据不会被冲掉。
--   3. 所有时间用 DATE_SUB(NOW(), INTERVAL n MINUTE) 写成相对时间，
--      首次导入时看起来就是「刚刚 / 几小时前 / 几天前」，演示效果自然。
--   4. 演示账号（6 个）登录密码统一为 123456。
--      此处存的是 MD5('123456') = e10adc3949ba59abbe56e057f20f883e。
--      如果 A 同学最终采用 BCrypt 或 MD5+盐，请统一改这一列，例如：
--        UPDATE `user` SET password = '<新的密文>' WHERE id <= 6;
--      （用自己实现的加密工具算一次 123456 的密文即可）
--   5. 本数据集与前端 Mock（frontend/src/mock/data.js）完全对齐，
--      切到真实后端后页面展示效果一致，前端无需改代码。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 用户：登录账号 = 手机号，密码统一 123456（MD5 密文）
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO `user`
(`id`, `username`, `password`, `nickname`, `avatar`, `phone`, `gender`, `status`, `student_no`, `college`, `auth_status`, `bio`, `create_time`, `update_time`)
VALUES
(1, '13800000001', 'e10adc3949ba59abbe56e057f20f883e', '林小满', 'https://picsum.photos/seed/avatar-1/160/160', '13800000001', 2, 1, '2022010305', '计算机科学与技术学院', 1, '计科大四，毕业清仓～专业书、数码、生活用品都出', DATE_SUB(NOW(), INTERVAL 172800 MINUTE), DATE_SUB(NOW(), INTERVAL 43200 MINUTE)),
(2, '13800000002', 'e10adc3949ba59abbe56e057f20f883e', '陈子昂', 'https://picsum.photos/seed/avatar-2/160/160', '13800000002', 1, 1, '2021030112', '电子信息工程学院', 1, '数码发烧友，手里的设备都保养得很好', DATE_SUB(NOW(), INTERVAL 288000 MINUTE), DATE_SUB(NOW(), INTERVAL 72000 MINUTE)),
(3, '13800000003', 'e10adc3949ba59abbe56e057f20f883e', '苏晴', 'https://picsum.photos/seed/avatar-3/160/160', '13800000003', 2, 1, '2023050428', '外国语学院', 1, '爱读书也爱断舍离，书本都是九成新以上', DATE_SUB(NOW(), INTERVAL 230400 MINUTE), DATE_SUB(NOW(), INTERVAL 57600 MINUTE)),
(4, '13800000004', 'e10adc3949ba59abbe56e057f20f883e', '周牧', 'https://picsum.photos/seed/avatar-4/160/160', '13800000004', 1, 1, '2022040917', '体育学院', 0, '篮球队的，运动装备低价转让，可当面验货', DATE_SUB(NOW(), INTERVAL 259200 MINUTE), DATE_SUB(NOW(), INTERVAL 64800 MINUTE)),
(5, '13800000005', 'e10adc3949ba59abbe56e057f20f883e', '何知远', 'https://picsum.photos/seed/avatar-5/160/160', '13800000005', 1, 1, '2021060233', '数学与统计学院', 0, '考研上岸啦，全套资料打包出，可小刀', DATE_SUB(NOW(), INTERVAL 201600 MINUTE), DATE_SUB(NOW(), INTERVAL 50400 MINUTE)),
(6, '13800000006', 'e10adc3949ba59abbe56e057f20f883e', '王梓萱', 'https://picsum.photos/seed/avatar-6/160/160', '13800000006', 2, 1, '2023070755', '设计艺术学院', 0, '衣柜大清理，衣服鞋子基本都是冲动消费', DATE_SUB(NOW(), INTERVAL 216000 MINUTE), DATE_SUB(NOW(), INTERVAL 54000 MINUTE));

-- -----------------------------------------------------------------------------
-- 分类
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO `category` (`id`, `name`, `sort`, `status`, `create_time`)
VALUES
(1, '教材书籍', 1, 1, DATE_SUB(NOW(), INTERVAL 432000 MINUTE)),
(2, '数码电子', 2, 1, DATE_SUB(NOW(), INTERVAL 432000 MINUTE)),
(3, '生活用品', 3, 1, DATE_SUB(NOW(), INTERVAL 432000 MINUTE)),
(4, '服饰鞋包', 4, 1, DATE_SUB(NOW(), INTERVAL 432000 MINUTE)),
(5, '运动户外', 5, 1, DATE_SUB(NOW(), INTERVAL 432000 MINUTE)),
(6, '美妆护肤', 6, 1, DATE_SUB(NOW(), INTERVAL 432000 MINUTE)),
(7, '乐器文娱', 7, 1, DATE_SUB(NOW(), INTERVAL 432000 MINUTE)),
(8, '其他闲置', 8, 1, DATE_SUB(NOW(), INTERVAL 432000 MINUTE));

-- -----------------------------------------------------------------------------
-- 商品（32 件，覆盖 8 个分类、6 个卖家；id 27 已售出、id 30 已下架）
-- 注意：`condition` 是 MySQL 保留字，必须加反引号
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO `product`
(`id`, `seller_id`, `category_id`, `title`, `description`, `price`, `original_price`, `stock`, `cover`, `status`, `view_count`, `sales_count`, `condition`, `location`, `create_time`, `update_time`)
VALUES
(1, 5, 1, '考研英语一 历年真题（2010-2024）', '真题册只做过前两套，后面全新未翻阅，附赠自己整理的长难句笔记和作文模板。', 18.00, 45.00, 2, 'https://picsum.photos/seed/campus-1/720/720', 1, 1286, 32, '九成新', '图书馆一楼大厅', DATE_SUB(NOW(), INTERVAL 43200 MINUTE), DATE_SUB(NOW(), INTERVAL 40320 MINUTE)),
(2, 3, 1, '高等数学 同济第七版 上下册', '书角有轻微磨损，内页无笔记无划线，适合大一大二同学打基础。', 20.00, 79.00, 3, 'https://picsum.photos/seed/campus-2/720/720', 1, 942, 21, '八成新', '第一教学楼门口', DATE_SUB(NOW(), INTERVAL 41964 MINUTE), DATE_SUB(NOW(), INTERVAL 39084 MINUTE)),
(3, 5, 1, '考研政治 精讲精练 + 1000 题', '肖秀荣全家桶，1000 题用铅笔做过一遍已擦干净，可配合我的错题本一起复习。', 30.00, 59.00, 1, 'https://picsum.photos/seed/campus-3/720/720', 1, 768, 18, '八成新', '南苑食堂门口', DATE_SUB(NOW(), INTERVAL 40728 MINUTE), DATE_SUB(NOW(), INTERVAL 37848 MINUTE)),
(4, 3, 1, '数据结构与算法分析（C 语言版）', '计算机专业课指定教材，内页干净，附 Course 习题解答电子版，可发网盘链接。', 35.00, 68.00, 2, 'https://picsum.photos/seed/campus-4/720/720', 1, 654, 12, '九成新', '图书馆一楼大厅', DATE_SUB(NOW(), INTERVAL 39492 MINUTE), DATE_SUB(NOW(), INTERVAL 36612 MINUTE)),
(5, 4, 1, '大学物理实验报告册（全新空白）', '买重了，塑封都没拆，实验课必备，校门口随到随取。', 8.00, 22.00, 3, 'https://picsum.photos/seed/campus-5/720/720', 1, 421, 9, '全新未拆封', '西区快递驿站', DATE_SUB(NOW(), INTERVAL 38256 MINUTE), DATE_SUB(NOW(), INTERVAL 35376 MINUTE)),
(6, 2, 2, 'iPad Air 4 64G 深空灰 含原装笔', '大二买的，日常贴膜带壳，无磕碰无拆修，电池健康 92%，配 Apple Pencil 二代一起走。', 1980.00, 4399.00, 1, 'https://picsum.photos/seed/campus-6/720/720', 1, 3286, 5, '九成新', '东区宿舍 6 栋', DATE_SUB(NOW(), INTERVAL 37020 MINUTE), DATE_SUB(NOW(), INTERVAL 34140 MINUTE)),
(7, 2, 2, '罗技 K380 蓝牙键盘 白色', '三设备切换顺滑，键程舒适，电池刚换过新的，包装盒和说明书都在。', 89.00, 199.00, 2, 'https://picsum.photos/seed/campus-7/720/720', 1, 1874, 26, '九成新', '东区宿舍 6 栋', DATE_SUB(NOW(), INTERVAL 35784 MINUTE), DATE_SUB(NOW(), INTERVAL 32904 MINUTE)),
(8, 2, 2, '小米显示器 24 英寸 1080P 窄边框', '做作业外接笔记本刚好，屏幕无亮点无坏道，附 HDMI 线和支架，仅限校内自取。', 350.00, 799.00, 3, 'https://picsum.photos/seed/campus-8/720/720', 1, 1420, 8, '八成新', '东区宿舍 6 栋', DATE_SUB(NOW(), INTERVAL 34548 MINUTE), DATE_SUB(NOW(), INTERVAL 31668 MINUTE)),
(9, 2, 2, '索尼 WH-1000XM4 头戴降噪耳机', '图书馆自习神器，降噪依旧强悍，耳罩无起皮，配原装收纳包与充电线。', 1280.00, 2299.00, 1, 'https://picsum.photos/seed/campus-9/720/720', 1, 2765, 3, '九成新', '图书馆一楼大厅', DATE_SUB(NOW(), INTERVAL 33312 MINUTE), DATE_SUB(NOW(), INTERVAL 30432 MINUTE)),
(10, 1, 2, 'AirPods 二代 有线充电盒版', '左右耳音量正常，无杂音，已用酒精棉片消毒，充电盒有轻微使用痕。', 420.00, 1246.00, 2, 'https://picsum.photos/seed/campus-10/720/720', 1, 2103, 11, '九成新', '南苑食堂门口', DATE_SUB(NOW(), INTERVAL 32076 MINUTE), DATE_SUB(NOW(), INTERVAL 29196 MINUTE)),
(11, 1, 2, '绿联 65W 氮化镓充电器 双 C 口', '买来出差用，实际只用了三四次，能同时给笔记本和手机快充，出门带一个就够。', 45.00, 99.00, 3, 'https://picsum.photos/seed/campus-11/720/720', 1, 986, 34, '几乎全新', '校内可送达', DATE_SUB(NOW(), INTERVAL 30840 MINUTE), DATE_SUB(NOW(), INTERVAL 27960 MINUTE)),
(12, 1, 3, '宿舍护眼台灯 三档色温可调', 'USB 供电，不频闪，看书刷题不伤眼，灯臂可多角度弯折，还剩一学期毕业出掉。', 39.00, 89.00, 1, 'https://picsum.photos/seed/campus-12/720/720', 1, 1123, 17, '九成新', '东区宿舍 6 栋', DATE_SUB(NOW(), INTERVAL 29604 MINUTE), DATE_SUB(NOW(), INTERVAL 26724 MINUTE)),
(13, 6, 3, '折叠收纳箱 大号两只（可叠放）', '搬家剩下的神器，能装下一整个衣柜的衣物，折叠后不占地方。', 25.00, 69.00, 2, 'https://picsum.photos/seed/campus-13/720/720', 1, 632, 14, '九成新', '西区快递驿站', DATE_SUB(NOW(), INTERVAL 27648 MINUTE), DATE_SUB(NOW(), INTERVAL 24768 MINUTE)),
(14, 6, 3, '小米电热水壶 1.5L 食品级内胆', '烧水快，内胆无水垢（一直用净水），毕业搬家出，附原装底座。', 45.00, 99.00, 3, 'https://picsum.photos/seed/campus-14/720/720', 1, 874, 9, '八成新', '西区快递驿站', DATE_SUB(NOW(), INTERVAL 26412 MINUTE), DATE_SUB(NOW(), INTERVAL 23532 MINUTE)),
(15, 6, 3, '全棉四件套 1.5 米床 浅灰格纹', '洗过一次就收起来了，尺寸不合适宿舍床，无勾丝无褪色，拍照实物。', 80.00, 259.00, 1, 'https://picsum.photos/seed/campus-15/720/720', 1, 706, 6, '几乎全新', '西区快递驿站', DATE_SUB(NOW(), INTERVAL 25176 MINUTE), DATE_SUB(NOW(), INTERVAL 22296 MINUTE)),
(16, 6, 4, '优衣库 轻薄羽绒服 M 码 藏青', '南方冬天一件就够，蓬松度很好，袖口无起球，已干洗可放心穿。', 199.00, 599.00, 2, 'https://picsum.photos/seed/campus-16/720/720', 1, 1652, 7, '九成新', '南苑食堂门口', DATE_SUB(NOW(), INTERVAL 23940 MINUTE), DATE_SUB(NOW(), INTERVAL 21060 MINUTE)),
(17, 6, 4, '阿迪达斯 运动卫衣 男 L 码', '经典三道杠，穿过三四次，领口袖口无松垮无起球，送同款收纳袋。', 129.00, 399.00, 3, 'https://picsum.photos/seed/campus-17/720/720', 1, 1088, 10, '九成新', '南苑食堂门口', DATE_SUB(NOW(), INTERVAL 22704 MINUTE), DATE_SUB(NOW(), INTERVAL 19824 MINUTE)),
(18, 6, 4, '全新马丁靴 39 码（买大了一码）', '去年双十一冲动下单，试都没试过，鞋盒吊牌齐全，可当面试穿。', 260.00, 699.00, 1, 'https://picsum.photos/seed/campus-18/720/720', 1, 1436, 2, '全新未拆封', '东区宿舍 6 栋', DATE_SUB(NOW(), INTERVAL 21468 MINUTE), DATE_SUB(NOW(), INTERVAL 18588 MINUTE)),
(19, 3, 4, '帆布单肩包 米白色 大容量', '装得下 14 寸笔记本加两本书，内袋多，背带去店里加固过一次更结实。', 35.00, 129.00, 2, 'https://picsum.photos/seed/campus-19/720/720', 1, 592, 15, '八成新', '图书馆一楼大厅', DATE_SUB(NOW(), INTERVAL 20232 MINUTE), DATE_SUB(NOW(), INTERVAL 17352 MINUTE)),
(20, 4, 5, '捷安特 山地自行车 24 速 铝合金车架', '车况很好，刚换过前后刹车皮和内胎，车锁、车筐一起送，仅限校内交易。', 680.00, 1580.00, 3, 'https://picsum.photos/seed/campus-20/720/720', 1, 2384, 4, '八成新', '体育馆北门', DATE_SUB(NOW(), INTERVAL 18996 MINUTE), DATE_SUB(NOW(), INTERVAL 16116 MINUTE)),
(21, 4, 5, '李宁 5 号篮球 室内外通用', '球队换新球，这颗只打过几次，球感软弹，气密性正常，附送打气筒。', 60.00, 129.00, 1, 'https://picsum.photos/seed/campus-21/720/720', 1, 968, 13, '九成新', '体育馆北门', DATE_SUB(NOW(), INTERVAL 17760 MINUTE), DATE_SUB(NOW(), INTERVAL 14880 MINUTE)),
(22, 4, 5, '羽毛球拍 双拍套装（含手胶）', '入门级别很够用，拍框无变形，已更换新吸汗手胶，送三个训练球。', 75.00, 199.00, 2, 'https://picsum.photos/seed/campus-22/720/720', 1, 782, 11, '九成新', '体育馆北门', DATE_SUB(NOW(), INTERVAL 16524 MINUTE), DATE_SUB(NOW(), INTERVAL 13644 MINUTE)),
(23, 6, 5, '迪卡侬 瑜伽垫 加厚 8mm', '买来只铺过两三次，已用湿巾擦净晾干，附收纳背带，宿舍拉伸健身刚好。', 45.00, 99.00, 3, 'https://picsum.photos/seed/campus-23/720/720', 1, 534, 16, '几乎全新', '西区快递驿站', DATE_SUB(NOW(), INTERVAL 15288 MINUTE), DATE_SUB(NOW(), INTERVAL 12408 MINUTE)),
(24, 6, 6, '兰蔻小黑瓶精华 30ml（专柜小样）', '专柜满赠拿到的小样，全新未拆，生产日期 2026 年，介意小样勿拍。', 220.00, 780.00, 1, 'https://picsum.photos/seed/campus-24/720/720', 1, 1892, 3, '全新未拆封', '南苑食堂门口', DATE_SUB(NOW(), INTERVAL 14052 MINUTE), DATE_SUB(NOW(), INTERVAL 11172 MINUTE)),
(25, 3, 6, '珀莱雅 双抗精华 30ml 剩约八成', '用了一个月觉得不适合我的肤质，剩八成左右，滴管干净，附原盒。', 90.00, 239.00, 2, 'https://picsum.photos/seed/campus-25/720/720', 1, 654, 8, '九成新', '图书馆一楼大厅', DATE_SUB(NOW(), INTERVAL 12096 MINUTE), DATE_SUB(NOW(), INTERVAL 9216 MINUTE)),
(26, 6, 7, '民谣吉他 41 寸 单板（含包与变调夹）', '入门一直用它练，音准稳定无开裂，附琴包、变调夹、背带和备用琴弦。', 380.00, 899.00, 3, 'https://picsum.photos/seed/campus-26/720/720', 1, 1567, 2, '八成新', '东区宿舍 6 栋', DATE_SUB(NOW(), INTERVAL 10860 MINUTE), DATE_SUB(NOW(), INTERVAL 7980 MINUTE)),
(27, 6, 7, '尤克里里 23 寸 桃花芯木', '音色清亮，外观几乎无痕，适合零基础练手，送一本自学教材。', 150.00, 399.00, 1, 'https://picsum.photos/seed/campus-27/720/720', 2, 894, 6, '九成新', '东区宿舍 6 栋', DATE_SUB(NOW(), INTERVAL 9624 MINUTE), DATE_SUB(NOW(), INTERVAL 6744 MINUTE)),
(28, 1, 7, '索尼 PS4 原装手柄 黑色', '按键回弹正常，摇杆无漂移，接口无松动，附一条原装充电线。', 120.00, 349.00, 2, 'https://picsum.photos/seed/campus-28/720/720', 1, 1032, 5, '八成新', '南苑食堂门口', DATE_SUB(NOW(), INTERVAL 8388 MINUTE), DATE_SUB(NOW(), INTERVAL 5508 MINUTE)),
(29, 1, 8, '搬家折叠小推车（承重 50kg）', '毕业搬宿舍神器，折叠后能塞进柜子缝，拉杆顺滑，轮胎无破损。', 30.00, 79.00, 3, 'https://picsum.photos/seed/campus-29/720/720', 1, 476, 19, '九成新', '东区宿舍 6 栋', DATE_SUB(NOW(), INTERVAL 7152 MINUTE), DATE_SUB(NOW(), INTERVAL 4272 MINUTE)),
(30, 6, 8, '全新保温杯 500ml 不锈钢（未使用）', '社团活动发的纪念款，一直没拆，保温效果很好，颜色是雾霾蓝。', 25.00, 89.00, 1, 'https://picsum.photos/seed/campus-30/720/720', 0, 388, 22, '全新未拆封', '西区快递驿站', DATE_SUB(NOW(), INTERVAL 5916 MINUTE), DATE_SUB(NOW(), INTERVAL 3036 MINUTE)),
(31, 1, 2, '小米无线鼠标 静音版 白色', '写代码一直在用，左右键声音很小，滚轮顺滑，附 USB 接收器和原装电池。', 29.00, 69.00, 2, 'https://picsum.photos/seed/campus-31/720/720', 1, 640, 12, '九成新', '校内可送达', DATE_SUB(NOW(), INTERVAL 4680 MINUTE), DATE_SUB(NOW(), INTERVAL 1800 MINUTE)),
(32, 1, 3, '宿舍折叠椅 承重 120kg', '椅面完好无破损，折叠后厚度只有 10cm，塞床底就行，坐着很稳。', 55.00, 149.00, 3, 'https://picsum.photos/seed/campus-32/720/720', 1, 512, 5, '八成新', '东区宿舍 6 栋', DATE_SUB(NOW(), INTERVAL 3444 MINUTE), DATE_SUB(NOW(), INTERVAL 564 MINUTE));

-- -----------------------------------------------------------------------------
-- 商品图片（每件商品 4 张，id = 商品id * 10 + 序号）
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO `product_image` (`id`, `product_id`, `url`, `sort`)
VALUES
( 11,  1, 'https://picsum.photos/seed/campus-1-1/720/720', 1), ( 12,  1, 'https://picsum.photos/seed/campus-1-2/720/720', 2), ( 13,  1, 'https://picsum.photos/seed/campus-1-3/720/720', 3), ( 14,  1, 'https://picsum.photos/seed/campus-1-4/720/720', 4),
( 21,  2, 'https://picsum.photos/seed/campus-2-1/720/720', 1), ( 22,  2, 'https://picsum.photos/seed/campus-2-2/720/720', 2), ( 23,  2, 'https://picsum.photos/seed/campus-2-3/720/720', 3), ( 24,  2, 'https://picsum.photos/seed/campus-2-4/720/720', 4),
( 31,  3, 'https://picsum.photos/seed/campus-3-1/720/720', 1), ( 32,  3, 'https://picsum.photos/seed/campus-3-2/720/720', 2), ( 33,  3, 'https://picsum.photos/seed/campus-3-3/720/720', 3), ( 34,  3, 'https://picsum.photos/seed/campus-3-4/720/720', 4),
( 41,  4, 'https://picsum.photos/seed/campus-4-1/720/720', 1), ( 42,  4, 'https://picsum.photos/seed/campus-4-2/720/720', 2), ( 43,  4, 'https://picsum.photos/seed/campus-4-3/720/720', 3), ( 44,  4, 'https://picsum.photos/seed/campus-4-4/720/720', 4),
( 51,  5, 'https://picsum.photos/seed/campus-5-1/720/720', 1), ( 52,  5, 'https://picsum.photos/seed/campus-5-2/720/720', 2), ( 53,  5, 'https://picsum.photos/seed/campus-5-3/720/720', 3), ( 54,  5, 'https://picsum.photos/seed/campus-5-4/720/720', 4),
( 61,  6, 'https://picsum.photos/seed/campus-6-1/720/720', 1), ( 62,  6, 'https://picsum.photos/seed/campus-6-2/720/720', 2), ( 63,  6, 'https://picsum.photos/seed/campus-6-3/720/720', 3), ( 64,  6, 'https://picsum.photos/seed/campus-6-4/720/720', 4),
( 71,  7, 'https://picsum.photos/seed/campus-7-1/720/720', 1), ( 72,  7, 'https://picsum.photos/seed/campus-7-2/720/720', 2), ( 73,  7, 'https://picsum.photos/seed/campus-7-3/720/720', 3), ( 74,  7, 'https://picsum.photos/seed/campus-7-4/720/720', 4),
( 81,  8, 'https://picsum.photos/seed/campus-8-1/720/720', 1), ( 82,  8, 'https://picsum.photos/seed/campus-8-2/720/720', 2), ( 83,  8, 'https://picsum.photos/seed/campus-8-3/720/720', 3), ( 84,  8, 'https://picsum.photos/seed/campus-8-4/720/720', 4),
( 91,  9, 'https://picsum.photos/seed/campus-9-1/720/720', 1), ( 92,  9, 'https://picsum.photos/seed/campus-9-2/720/720', 2), ( 93,  9, 'https://picsum.photos/seed/campus-9-3/720/720', 3), ( 94,  9, 'https://picsum.photos/seed/campus-9-4/720/720', 4),
(101, 10, 'https://picsum.photos/seed/campus-10-1/720/720', 1), (102, 10, 'https://picsum.photos/seed/campus-10-2/720/720', 2), (103, 10, 'https://picsum.photos/seed/campus-10-3/720/720', 3), (104, 10, 'https://picsum.photos/seed/campus-10-4/720/720', 4),
(111, 11, 'https://picsum.photos/seed/campus-11-1/720/720', 1), (112, 11, 'https://picsum.photos/seed/campus-11-2/720/720', 2), (113, 11, 'https://picsum.photos/seed/campus-11-3/720/720', 3), (114, 11, 'https://picsum.photos/seed/campus-11-4/720/720', 4),
(121, 12, 'https://picsum.photos/seed/campus-12-1/720/720', 1), (122, 12, 'https://picsum.photos/seed/campus-12-2/720/720', 2), (123, 12, 'https://picsum.photos/seed/campus-12-3/720/720', 3), (124, 12, 'https://picsum.photos/seed/campus-12-4/720/720', 4),
(131, 13, 'https://picsum.photos/seed/campus-13-1/720/720', 1), (132, 13, 'https://picsum.photos/seed/campus-13-2/720/720', 2), (133, 13, 'https://picsum.photos/seed/campus-13-3/720/720', 3), (134, 13, 'https://picsum.photos/seed/campus-13-4/720/720', 4),
(141, 14, 'https://picsum.photos/seed/campus-14-1/720/720', 1), (142, 14, 'https://picsum.photos/seed/campus-14-2/720/720', 2), (143, 14, 'https://picsum.photos/seed/campus-14-3/720/720', 3), (144, 14, 'https://picsum.photos/seed/campus-14-4/720/720', 4),
(151, 15, 'https://picsum.photos/seed/campus-15-1/720/720', 1), (152, 15, 'https://picsum.photos/seed/campus-15-2/720/720', 2), (153, 15, 'https://picsum.photos/seed/campus-15-3/720/720', 3), (154, 15, 'https://picsum.photos/seed/campus-15-4/720/720', 4),
(161, 16, 'https://picsum.photos/seed/campus-16-1/720/720', 1), (162, 16, 'https://picsum.photos/seed/campus-16-2/720/720', 2), (163, 16, 'https://picsum.photos/seed/campus-16-3/720/720', 3), (164, 16, 'https://picsum.photos/seed/campus-16-4/720/720', 4),
(171, 17, 'https://picsum.photos/seed/campus-17-1/720/720', 1), (172, 17, 'https://picsum.photos/seed/campus-17-2/720/720', 2), (173, 17, 'https://picsum.photos/seed/campus-17-3/720/720', 3), (174, 17, 'https://picsum.photos/seed/campus-17-4/720/720', 4),
(181, 18, 'https://picsum.photos/seed/campus-18-1/720/720', 1), (182, 18, 'https://picsum.photos/seed/campus-18-2/720/720', 2), (183, 18, 'https://picsum.photos/seed/campus-18-3/720/720', 3), (184, 18, 'https://picsum.photos/seed/campus-18-4/720/720', 4),
(191, 19, 'https://picsum.photos/seed/campus-19-1/720/720', 1), (192, 19, 'https://picsum.photos/seed/campus-19-2/720/720', 2), (193, 19, 'https://picsum.photos/seed/campus-19-3/720/720', 3), (194, 19, 'https://picsum.photos/seed/campus-19-4/720/720', 4),
(201, 20, 'https://picsum.photos/seed/campus-20-1/720/720', 1), (202, 20, 'https://picsum.photos/seed/campus-20-2/720/720', 2), (203, 20, 'https://picsum.photos/seed/campus-20-3/720/720', 3), (204, 20, 'https://picsum.photos/seed/campus-20-4/720/720', 4),
(211, 21, 'https://picsum.photos/seed/campus-21-1/720/720', 1), (212, 21, 'https://picsum.photos/seed/campus-21-2/720/720', 2), (213, 21, 'https://picsum.photos/seed/campus-21-3/720/720', 3), (214, 21, 'https://picsum.photos/seed/campus-21-4/720/720', 4),
(221, 22, 'https://picsum.photos/seed/campus-22-1/720/720', 1), (222, 22, 'https://picsum.photos/seed/campus-22-2/720/720', 2), (223, 22, 'https://picsum.photos/seed/campus-22-3/720/720', 3), (224, 22, 'https://picsum.photos/seed/campus-22-4/720/720', 4),
(231, 23, 'https://picsum.photos/seed/campus-23-1/720/720', 1), (232, 23, 'https://picsum.photos/seed/campus-23-2/720/720', 2), (233, 23, 'https://picsum.photos/seed/campus-23-3/720/720', 3), (234, 23, 'https://picsum.photos/seed/campus-23-4/720/720', 4),
(241, 24, 'https://picsum.photos/seed/campus-24-1/720/720', 1), (242, 24, 'https://picsum.photos/seed/campus-24-2/720/720', 2), (243, 24, 'https://picsum.photos/seed/campus-24-3/720/720', 3), (244, 24, 'https://picsum.photos/seed/campus-24-4/720/720', 4),
(251, 25, 'https://picsum.photos/seed/campus-25-1/720/720', 1), (252, 25, 'https://picsum.photos/seed/campus-25-2/720/720', 2), (253, 25, 'https://picsum.photos/seed/campus-25-3/720/720', 3), (254, 25, 'https://picsum.photos/seed/campus-25-4/720/720', 4),
(261, 26, 'https://picsum.photos/seed/campus-26-1/720/720', 1), (262, 26, 'https://picsum.photos/seed/campus-26-2/720/720', 2), (263, 26, 'https://picsum.photos/seed/campus-26-3/720/720', 3), (264, 26, 'https://picsum.photos/seed/campus-26-4/720/720', 4),
(271, 27, 'https://picsum.photos/seed/campus-27-1/720/720', 1), (272, 27, 'https://picsum.photos/seed/campus-27-2/720/720', 2), (273, 27, 'https://picsum.photos/seed/campus-27-3/720/720', 3), (274, 27, 'https://picsum.photos/seed/campus-27-4/720/720', 4),
(281, 28, 'https://picsum.photos/seed/campus-28-1/720/720', 1), (282, 28, 'https://picsum.photos/seed/campus-28-2/720/720', 2), (283, 28, 'https://picsum.photos/seed/campus-28-3/720/720', 3), (284, 28, 'https://picsum.photos/seed/campus-28-4/720/720', 4),
(291, 29, 'https://picsum.photos/seed/campus-29-1/720/720', 1), (292, 29, 'https://picsum.photos/seed/campus-29-2/720/720', 2), (293, 29, 'https://picsum.photos/seed/campus-29-3/720/720', 3), (294, 29, 'https://picsum.photos/seed/campus-29-4/720/720', 4),
(301, 30, 'https://picsum.photos/seed/campus-30-1/720/720', 1), (302, 30, 'https://picsum.photos/seed/campus-30-2/720/720', 2), (303, 30, 'https://picsum.photos/seed/campus-30-3/720/720', 3), (304, 30, 'https://picsum.photos/seed/campus-30-4/720/720', 4),
(311, 31, 'https://picsum.photos/seed/campus-31-1/720/720', 1), (312, 31, 'https://picsum.photos/seed/campus-31-2/720/720', 2), (313, 31, 'https://picsum.photos/seed/campus-31-3/720/720', 3), (314, 31, 'https://picsum.photos/seed/campus-31-4/720/720', 4),
(321, 32, 'https://picsum.photos/seed/campus-32-1/720/720', 1), (322, 32, 'https://picsum.photos/seed/campus-32-2/720/720', 2), (323, 32, 'https://picsum.photos/seed/campus-32-3/720/720', 3), (324, 32, 'https://picsum.photos/seed/campus-32-4/720/720', 4);

-- -----------------------------------------------------------------------------
-- 评论 / 回复（parent_id = 0 为一级评论，否则指向被回复的评论 id）
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO `comment` (`id`, `product_id`, `user_id`, `content`, `parent_id`, `reply_user_id`, `create_time`)
VALUES
(1, 1, 3, '请问笔记是手写的还是电子版呀？如果是电子版想要一份～', 0, NULL, DATE_SUB(NOW(), INTERVAL 8640 MINUTE)),
(2, 1, 1, '笔记是手写的长难句整理，随书一起给你，电子版作文模板我也可以发网盘。', 1, 3, DATE_SUB(NOW(), INTERVAL 7260 MINUTE)),
(3, 1, 4, '真题册还在吗？我明天想去图书馆当面看一下可以吗', 0, NULL, DATE_SUB(NOW(), INTERVAL 3000 MINUTE)),
(4, 2, 2, '请问上下册都有吗，有没有老师的课件配套', 0, NULL, DATE_SUB(NOW(), INTERVAL 5940 MINUTE)),
(5, 2, 1, '上下册都有的，课件我没有哦，不过书上的例题讲解已经很全了。', 4, 2, DATE_SUB(NOW(), INTERVAL 4560 MINUTE)),
(6, 6, 5, 'iPad 电池健康还在 92% 的话很值了，请问支持当面验机吗', 0, NULL, DATE_SUB(NOW(), INTERVAL 4620 MINUTE)),
(7, 6, 2, '当然可以，约在宿舍楼下或者图书馆都行，当面看电池和外观。', 6, 5, DATE_SUB(NOW(), INTERVAL 3240 MINUTE)),
(8, 7, 4, '键盘支持 Mac 的 Fn 组合键吗，想配平板用', 0, NULL, DATE_SUB(NOW(), INTERVAL 1860 MINUTE)),
(9, 8, 3, '显示器能出到 320 吗，我今天可以去拿', 0, NULL, DATE_SUB(NOW(), INTERVAL 7680 MINUTE)),
(10, 12, 6, '台灯还剩吗？宿舍晚上熄灯后能不能用充电宝供电', 0, NULL, DATE_SUB(NOW(), INTERVAL 10080 MINUTE)),
(11, 12, 1, '还在的，它本来就是 USB 供电，接充电宝完全没问题。', 10, 6, DATE_SUB(NOW(), INTERVAL 8700 MINUTE)),
(12, 20, 5, '自行车有发票吗，怕过不了校门安保', 0, NULL, DATE_SUB(NOW(), INTERVAL 5880 MINUTE)),
(13, 20, 4, '发票找不到了，但是车架号我能拍给你，校内骑了两年没被查过。', 12, 5, DATE_SUB(NOW(), INTERVAL 4500 MINUTE)),
(14, 26, 3, '吉他弦是原装的吗，可以顺便教两个和弦吗哈哈', 0, NULL, DATE_SUB(NOW(), INTERVAL 3120 MINUTE)),
(15, 26, 6, '弦上个月刚换的新弦，教和弦没问题，当面教到你会为止。', 14, 3, DATE_SUB(NOW(), INTERVAL 1740 MINUTE)),
(16, 1, 2, '请问真题册是英语一还是英语二？我想确认下版本', 0, NULL, DATE_SUB(NOW(), INTERVAL 10440 MINUTE)),
(17, 1, 6, '书有点厚，能帮忙送到女生宿舍楼下吗', 0, NULL, DATE_SUB(NOW(), INTERVAL 9060 MINUTE)),
(18, 1, 5, '已经下单啦，麻烦帮我留着，周末来取', 0, NULL, DATE_SUB(NOW(), INTERVAL 6240 MINUTE)),
(19, 1, 4, '笔记部分有多少页呀，主要想看长难句整理', 0, NULL, DATE_SUB(NOW(), INTERVAL 2880 MINUTE));

-- -----------------------------------------------------------------------------
-- 站内消息（全部归属于演示账号 user 1，含未读 4 条）
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO `message` (`id`, `user_id`, `type`, `title`, `content`, `biz_id`, `is_read`, `create_time`)
VALUES
(1, 1, 'trade', '订单待支付提醒', '你有一笔订单尚未支付，请在 30 分钟内完成支付，逾期订单将自动取消。', 1, 0, DATE_SUB(NOW(), INTERVAL 144 MINUTE)),
(2, 1, 'comment', '苏晴 回复了你的评论', '“笔记是手写的长难句整理，随书一起给你……”', 1, 0, DATE_SUB(NOW(), INTERVAL 492 MINUTE)),
(3, 1, 'trade', '买家已付款', '订单 2026091315218 买家已付款，请尽快安排发货并联系买家约定交易时间。', 2, 1, DATE_SUB(NOW(), INTERVAL 1848 MINUTE)),
(4, 1, 'system', '校园认证通过', '恭喜，你的学生身份认证已通过审核，现在可以发布商品并参与交易啦。', 0, 1, DATE_SUB(NOW(), INTERVAL 3060 MINUTE)),
(5, 1, 'comment', '陈子昂 回复了你的评论', '“当然可以，约在宿舍楼下或者图书馆都行，当面看电池和外观。”', 6, 0, DATE_SUB(NOW(), INTERVAL 3696 MINUTE)),
(6, 1, 'trade', '卖家已发货', '订单 2026091188734 卖家已交付商品，收到货后请及时确认收货。', 3, 0, DATE_SUB(NOW(), INTERVAL 4764 MINUTE)),
(7, 1, 'system', '平台交易规范更新', '为保障同学权益，本学期起所有交易建议在校内公共区域当面验货，请知悉。', 0, 1, DATE_SUB(NOW(), INTERVAL 7560 MINUTE)),
(8, 1, 'trade', '交易完成', '订单 2026090591207 已完成交易，欢迎在商品详情页留下你的评价。', 4, 1, DATE_SUB(NOW(), INTERVAL 11520 MINUTE)),
(9, 1, 'comment', '周牧 回复了你的评论', '“发票找不到了，但是车架号我能拍给你，校内骑了两年没被查过。”', 20, 1, DATE_SUB(NOW(), INTERVAL 13020 MINUTE)),
(10, 1, 'system', '闲置清理活动开启', '「毕业季清仓」专题已上线，发布闲置即可获得首页推荐位曝光。', 0, 0, DATE_SUB(NOW(), INTERVAL 17400 MINUTE));

-- -----------------------------------------------------------------------------
-- 收货地址（演示账号 user 1 的 3 条地址）
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO `address` (`id`, `user_id`, `receiver_name`, `phone`, `region`, `detail`, `is_default`, `create_time`)
VALUES
(1, 1, '林小满', '13800000001', '东区宿舍 6 栋', '301 室 3 号床（楼下有快递暂存架，可直接放架上发消息）', 1, DATE_SUB(NOW(), INTERVAL 129600 MINUTE)),
(2, 1, '林小满', '13800000001', '计算机学院实验楼', 'B302 实验室（工作日 9:00-18:00 在，周末勿送）', 0, DATE_SUB(NOW(), INTERVAL 64800 MINUTE)),
(3, 1, '林建国', '13900000009', '南苑家属区', '12 栋 502，假期在家时使用', 0, DATE_SUB(NOW(), INTERVAL 28800 MINUTE));

-- -----------------------------------------------------------------------------
-- 订单（覆盖 待支付 / 已支付 / 已发货 / 已完成 / 已取消 五种状态）
-- 注意：order 是 MySQL 保留字，必须加反引号
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO `order`
(`id`, `order_no`, `buyer_id`, `seller_id`, `total_amount`, `pay_amount`, `status`, `address_snapshot`, `pay_time`, `remark`, `pay_method`, `create_time`, `update_time`)
VALUES
(1, '2026901100137', 1, 3, 30.00, 30.00, 0, '林小满 13800000001 东区宿舍 6 栋 301 室 3 号床（楼下有快递暂存架，可直接放架上发消息）', NULL, '希望明天中午在校门口当面交易，谢谢！', NULL, DATE_SUB(NOW(), INTERVAL 432 MINUTE), DATE_SUB(NOW(), INTERVAL 252 MINUTE)),
(2, '2026902100274', 1, 2, 1280.00, 1280.00, 1, '林小满 13800000001 东区宿舍 6 栋 301 室 3 号床（楼下有快递暂存架，可直接放架上发消息）', DATE_SUB(NOW(), INTERVAL 1848 MINUTE), '周末在图书馆一楼大厅碰面可以吗？', 'campus_card', DATE_SUB(NOW(), INTERVAL 2088 MINUTE), DATE_SUB(NOW(), INTERVAL 468 MINUTE)),
(3, '2026903100411', 1, 2, 1980.00, 1980.00, 2, '林小满 13800000001 东区宿舍 6 栋 301 室 3 号床（楼下有快递暂存架，可直接放架上发消息）', DATE_SUB(NOW(), INTERVAL 4584 MINUTE), '发到东区宿舍 6 栋就行，麻烦提前联系。', 'campus_card', DATE_SUB(NOW(), INTERVAL 4824 MINUTE), DATE_SUB(NOW(), INTERVAL 3204 MINUTE)),
(4, '2026904100548', 1, 4, 135.00, 130.00, 3, '林小满 13800000001 东区宿舍 6 栋 301 室 3 号床（楼下有快递暂存架，可直接放架上发消息）', DATE_SUB(NOW(), INTERVAL 11640 MINUTE), '篮球帮我打好气，谢谢师兄！', 'campus_card', DATE_SUB(NOW(), INTERVAL 11880 MINUTE), DATE_SUB(NOW(), INTERVAL 10260 MINUTE)),
(5, '2026905100685', 1, 5, 18.00, 18.00, 3, '林小满 13800000001 东区宿舍 6 栋 301 室 3 号床（楼下有快递暂存架，可直接放架上发消息）', DATE_SUB(NOW(), INTERVAL 21720 MINUTE), '书收到啦，笔记很有用，五星好评～', 'campus_card', DATE_SUB(NOW(), INTERVAL 21960 MINUTE), DATE_SUB(NOW(), INTERVAL 20340 MINUTE)),
(6, '2026906100822', 1, 6, 25.00, 25.00, 4, '林小满 13800000001 东区宿舍 6 栋 301 室 3 号床（楼下有快递暂存架，可直接放架上发消息）', DATE_SUB(NOW(), INTERVAL 9480 MINUTE), '拍错了，不好意思先取消吧。', 'campus_card', DATE_SUB(NOW(), INTERVAL 9720 MINUTE), DATE_SUB(NOW(), INTERVAL 8100 MINUTE)),
(7, '2026907100959', 2, 1, 39.00, 39.00, 1, '林小满 13800000001 东区宿舍 6 栋 301 室 3 号床（楼下有快递暂存架，可直接放架上发消息）', DATE_SUB(NOW(), INTERVAL 1272 MINUTE), '同学你好，我下午三点到东区宿舍 6 栋楼下取，方便吗？', 'campus_card', DATE_SUB(NOW(), INTERVAL 1512 MINUTE), DATE_SUB(NOW(), INTERVAL 1332 MINUTE));

-- -----------------------------------------------------------------------------
-- 订单明细（价格 / 标题 / 封面均为下单时的快照）
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO `order_item` (`id`, `order_id`, `product_id`, `product_title`, `product_cover`, `price`, `quantity`, `total_amount`)
VALUES
(11, 1, 3, '考研政治 精讲精练 + 1000 题', 'https://picsum.photos/seed/campus-3/720/720', 30.00, 1, 30.00),
(21, 2, 9, '索尼 WH-1000XM4 头戴降噪耳机', 'https://picsum.photos/seed/campus-9/720/720', 1280.00, 1, 1280.00),
(31, 3, 6, 'iPad Air 4 64G 深空灰 含原装笔', 'https://picsum.photos/seed/campus-6/720/720', 1980.00, 1, 1980.00),
(41, 4, 21, '李宁 5 号篮球 室内外通用', 'https://picsum.photos/seed/campus-21/720/720', 60.00, 1, 60.00),
(42, 4, 22, '羽毛球拍 双拍套装（含手胶）', 'https://picsum.photos/seed/campus-22/720/720', 75.00, 1, 75.00),
(51, 5, 1, '考研英语一 历年真题（2010-2024）', 'https://picsum.photos/seed/campus-1/720/720', 18.00, 1, 18.00),
(61, 6, 30, '全新保温杯 500ml 不锈钢（未使用）', 'https://picsum.photos/seed/campus-30/720/720', 25.00, 1, 25.00),
(71, 7, 12, '宿舍护眼台灯 三档色温可调', 'https://picsum.photos/seed/campus-12/720/720', 39.00, 1, 39.00);

-- -----------------------------------------------------------------------------
-- 收藏（演示账号 user 1 的 12 条收藏）
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO `favorite` (`id`, `user_id`, `product_id`, `create_time`)
VALUES
(1, 1, 6, DATE_SUB(NOW(), INTERVAL 17280 MINUTE)),
(2, 1, 20, DATE_SUB(NOW(), INTERVAL 15840 MINUTE)),
(3, 1, 7, DATE_SUB(NOW(), INTERVAL 14400 MINUTE)),
(4, 1, 9, DATE_SUB(NOW(), INTERVAL 12960 MINUTE)),
(5, 1, 13, DATE_SUB(NOW(), INTERVAL 11520 MINUTE)),
(6, 1, 16, DATE_SUB(NOW(), INTERVAL 10080 MINUTE)),
(7, 1, 21, DATE_SUB(NOW(), INTERVAL 8640 MINUTE)),
(8, 1, 23, DATE_SUB(NOW(), INTERVAL 7200 MINUTE)),
(9, 1, 26, DATE_SUB(NOW(), INTERVAL 5760 MINUTE)),
(10, 1, 2, DATE_SUB(NOW(), INTERVAL 4320 MINUTE)),
(11, 1, 4, DATE_SUB(NOW(), INTERVAL 2880 MINUTE)),
(12, 1, 14, DATE_SUB(NOW(), INTERVAL 1440 MINUTE));

-- -----------------------------------------------------------------------------
-- 购物车（演示账号 user 1 的 2 条已选中商品）
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO `cart` (`id`, `user_id`, `product_id`, `quantity`, `selected`, `create_time`, `update_time`)
VALUES
(1, 1, 7, 1, 1, DATE_SUB(NOW(), INTERVAL 1440 MINUTE), DATE_SUB(NOW(), INTERVAL 288 MINUTE)),
(2, 1, 8, 1, 1, DATE_SUB(NOW(), INTERVAL 1296 MINUTE), DATE_SUB(NOW(), INTERVAL 432 MINUTE));

-- -----------------------------------------------------------------------------
-- 浏览记录（给 F 模块的浏览量统计提供一点初始数据）
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO `browse_log` (`id`, `user_id`, `product_id`, `create_time`)
VALUES
(1, 1, 6, DATE_SUB(NOW(), INTERVAL 200 MINUTE)),
(2, 1, 9, DATE_SUB(NOW(), INTERVAL 190 MINUTE)),
(3, 1, 20, DATE_SUB(NOW(), INTERVAL 1750 MINUTE)),
(4, 1, 7, DATE_SUB(NOW(), INTERVAL 1400 MINUTE)),
(5, 2, 1, DATE_SUB(NOW(), INTERVAL 900 MINUTE)),
(6, 2, 12, DATE_SUB(NOW(), INTERVAL 880 MINUTE)),
(7, 3, 6, DATE_SUB(NOW(), INTERVAL 4600 MINUTE)),
(8, 3, 26, DATE_SUB(NOW(), INTERVAL 3100 MINUTE)),
(9, 4, 20, DATE_SUB(NOW(), INTERVAL 5800 MINUTE)),
(10, 5, 1, DATE_SUB(NOW(), INTERVAL 6200 MINUTE)),
(11, 6, 24, DATE_SUB(NOW(), INTERVAL 300 MINUTE)),
(12, 1, 21, DATE_SUB(NOW(), INTERVAL 60 MINUTE));
