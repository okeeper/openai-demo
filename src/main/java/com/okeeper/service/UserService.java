package com.okeeper.service;

import com.okeeper.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zy
 * @since 2023-03-09
 */
public interface UserService extends IService<User> {

    User queryUserByUnionId(String unionId);

    User registerUser(String unionId);

    User loginOrRegisterUser(String unionId);

    void decreaseRemainTimes(Long userId, int i);

    void rewardRemainTimes(Long userId, int i);
}
