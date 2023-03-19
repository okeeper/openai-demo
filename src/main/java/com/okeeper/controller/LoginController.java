package com.okeeper.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.okeeper.constants.CacheKeys;
import com.okeeper.controller.dto.BaseReq;
import com.okeeper.controller.dto.Result;
import com.okeeper.entity.User;
import com.okeeper.service.UserService;
import com.okeeper.utils.CacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class LoginController {


  private static Cache<Object, Object> loginCache = CacheBuilder.newBuilder()
          //设置并发级别为8，并发级别是指可以同时写缓存的线程数
          .concurrencyLevel(20)
          //设置缓存容器的初始容量为10
          .initialCapacity(10)
          //设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
          .maximumSize(2000000)
          //是否需要统计缓存情况,该操作消耗一定的性能,生产环境应该去除
          //.recordStats()
          //设置写缓存后n秒钟过期
          .expireAfterWrite(24, TimeUnit.HOURS)
          //设置读写缓存后n秒钟过期,实际很少用到,类似于expireAfterWrite
          //.expireAfterAccess(17, TimeUnit.SECONDS)
          //只阻塞当前数据加载线程，其他线程返回旧值
          //.refreshAfterWrite(13, TimeUnit.SECONDS)
          //设置缓存的移除通知
          .removalListener(notification -> {
            log.info("login token expired. {} ,{}", notification.getKey(), notification.getValue());
          })
          .build();

  @Autowired
  private WxMaService wxService;

  @Autowired
  private UserService userService;

  @PostMapping("/getToken")
  public Result<String> getToken(@RequestParam("code") String code)  {
    // 调用微信API获取登录凭证
    try {
      WxMaJscode2SessionResult session = wxService.getUserService().getSessionInfo(code);
      // 将登录凭证存储在服务器端
      //String openid = session.getOpenid();
      String sessionKey = session.getSessionKey();
      String unionId = session.getOpenid();
//      String unionId = "test";
//      String sessionKey = "test";

      //注册或者登录
      User user = userService.loginOrRegisterUser(unionId);

      String token = UUID.randomUUID().toString();
      String unionIdKey = String.format(CacheKeys.UNIONID_CACHE, unionId);

      //清理上一次的token
      String lastToken = (String) CacheUtils.get(unionIdKey);
      if(StringUtils.isNotEmpty(lastToken)) {
        loginCache.invalidate(String.format(CacheKeys.LOGIN_TOKEN, lastToken));
      }
      //保存openId对应的token
      loginCache.put(unionIdKey, token);
      //保存token对应openId
      loginCache.put(String.format(CacheKeys.LOGIN_TOKEN, token), user);

      log.info("getToken success. userId={}, unionId={}, sessionKey={}, token={}", user.getId(), unionId, sessionKey, token);
      // 返回登录凭证
      return Result.success(token);
    }catch (Throwable e) {
      log.error("get Token error.", e);
      // 返回登录凭证
      return Result.fail("get Token error.");
    }
  }


  @PostMapping("/getLoginUserInfo")
  public Result<User> getLoginUserInfo(@Valid @RequestBody BaseReq baseReq)  {
    // 返回登录凭证
    return Result.success(userService.getById(baseReq.getUserId()));
  }


  public static User getLoginedUser(String token) {
    // 从Redis中获取token对应的openid和sessionKey
    return (User) loginCache.getIfPresent(String.format(CacheKeys.LOGIN_TOKEN, token));
  }
}
