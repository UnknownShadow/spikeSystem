-- 数据库初始化脚本

-- 创建数据库
CREATE DATABASE spike;
-- 使用数据库
use spike;
-- 创建秒杀数据库
CREATE TABLE spike(
`spike_id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
`name` varchar(120) NOT NULL COMMENT '商品名称',
`number` int NOT NULL COMMENT '库存数量',
`create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`start_time` TIMESTAMP NOT NULL COMMENT '秒杀开始时间',
`end_time` TIMESTAMP NOT NULL COMMENT '秒杀结束时间',
PRIMARY KEY (spike_id),
key idx_start_time(start_time),
key idx_end_time(end_time),
key idx_create_time(create_time)
)ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8 COMMENT='秒杀库存表';


-- 初始化数据
INSERT INTO
  spike(name, number, start_time, end_time)
VALUES
  ('1000元秒杀iphone 7', 100, '2017-08-16 00:00:00', '2017-08-17 00:00:00'),
  ('800元秒杀iphone 6s', 200, '2017-08-16 00:00:00', '2017-08-17 00:00:00'),
  ('500元秒杀ipad air', 300, '2017-08-16 00:00:00', '2017-08-17 00:00:00'),
  ('300元秒杀小米6', 400, '2017-08-16 00:00:00', '2017-08-17 00:00:00'),
  ('100元秒杀红米4x', 500, '2017-08-16 00:00:00', '2017-08-17 00:00:00');

-- 秒杀成功明细表
CREATE TABLE success_spiked(
`spike_id` bigint NOT NULL COMMENT '商品ID',
`phone_number` bigint NOT NULL COMMENT '用户手机号',
`state` tinyint NOT NULL DEFAULT 0 COMMENT '秒杀状态： -1 失败， 0 成功',
`create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
PRIMARY KEY (spike_id, phone_number),
key idx_create_time(create_time)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='秒杀成功明细表';

-- 连接数据库
mysql -uroot -p123456