-- 秒杀执行的存储过程

DELIMITER $$ -- console ； 转换为 $$

-- 定义存储过程
-- 参数： in 输入参数； out 输出参数
-- row_count(): 返回上一条修改类型sql（delete， insert， update）的影响行数
-- row_count(): 0 未修改数据 >0 表示修改行数 <0 sql错误/未执行修改sql
CREATE PROCEDURE `spike`.`execute_spike`
 (in v_spike_id bigint, in v_phone bigint,
  in v_spike_time TIMESTAMP, out r_result int)
  BEGIN
    DECLARE insert_count int DEFAULT 0;
    START TRANSACTION;
    insert ignore into success_spiked
      (spike_id, phone_number, create_time)
    values (v_spike_id, v_phone, v_spike_time);
    select row_count() into insert_count;
    IF (insert_count = 0) THEN
      ROLLBACK;
      set r_result = -1;
    ELSEIF (insert_count < 0) THEN
      ROLLBACK;
      set r_result = -2;
    ELSE
      update spike
      set number = number - 1
      where spike_id = v_spike_id
        and end_time > v_spike_time
        and start_time < v_spike_time
        and number > 0;
      select row_count() into insert_count;
      IF (insert_count = 0) THEN
        ROLLBACK;
        set r_result = 0;
      ELSEIF (insert_count < 0) THEN
        ROLLBACK;
        set r_result = -2;
      ELSE
        COMMIT;
        set r_result = 1;
      END IF;
    END IF;
  END;
$$
-- 存储过程定义结束

DELIMITER ;

set @r_result=-3;
-- 执行存储过程
call execute_spike(1006, 12547841254, now(), @r_result);

-- 获取结果
select @r_result;
