package com.okeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.okeeper.entity.User;
import com.okeeper.mapper.UserMapper;
import com.okeeper.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zy
 * @since 2023-03-09
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${openai.defaultChatTimes:5}")
    private Integer defaultChatTimes;


    @Override
    public User queryUserByUnionId(String unionId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("union_id", unionId);
        return this.getBaseMapper().selectOne(queryWrapper);
    }

    @Override
    public User registerUser(String unionId) {
        User user = new User();
        user.setUserName(genUserName());
        user.setUnionId(unionId);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setRemainTimes(defaultChatTimes);
        this.save(user);
        return user;
    }

    @Override
    public User loginOrRegisterUser(String unionId) {
        synchronized (unionId) {
            User user = this.queryUserByUnionId(unionId);
            if(user == null) {
                user = registerUser(unionId);
                log.info("registerUser success unionId={}, userId={}", unionId, user.getId());
            }
            user.setUpdateTime(new Date());
            this.updateById(user);
            return user;
        }
    }

    @Override
    public void decreaseRemainTimes(Long userId, int i) {
        synchronized (userId) {
            User user = this.getById(userId);
            user.setUpdateTime(new Date());
            user.setRemainTimes(user.getRemainTimes() - i);
            this.updateById(user);
        }
    }

    @Override
    public void rewardRemainTimes(Long userId, int i) {
        synchronized (userId) {
            User user = this.getById(userId);
            user.setUpdateTime(new Date());
            user.setRemainTimes(user.getRemainTimes() + i);
            this.updateById(user);
        }
    }

    /**
     * 线程安全
     */
    private static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");
    /**
     * 生成订单号 21位
     * @return
     */
    public static String genUserName() {
        LocalDateTime date = LocalDateTime.now();
        return "AI" + DATE_FORMATTER.format(date) + RandomStringUtils.randomNumeric(4);
    }
}
