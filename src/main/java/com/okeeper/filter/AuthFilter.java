package com.okeeper.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.okeeper.controller.LoginController;
import com.okeeper.controller.dto.Result;
import com.okeeper.entity.User;
import com.okeeper.service.UserService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@Order(10)
@Component
public class AuthFilter implements Filter {

  private String NO_FILTER_URLS[] = new String[]{"/getToken"};

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    if(Arrays.asList(NO_FILTER_URLS).contains(((HttpServletRequest) request).getRequestURI())) {
      chain.doFilter(request, response);
      return;
    }

    // 获取Authorization头部
    String token = httpRequest.getHeader("Authorization");
    if (token == null) {
      // 如果Authorization头部不存在或不以"Bearer "开头，则认为未登录，返回401 Unauthorized响应
      httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      IOUtils.write(Result.fail("非法token").toJsonString(), response.getOutputStream());
      return;
    }

    // 从Redis中获取token对应的openid和sessionKey
    User user = LoginController.getLoginedUser(token);
    if (Objects.isNull(user)) {
      // 如果token无效，则认为未登录，返回401 Unauthorized响应
      httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      IOUtils.write(Result.fail("token实效").toJsonString(), response.getOutputStream());
      return;
    }

    ParamsRequestWrapper editableRequest = (ParamsRequestWrapper) request;
    JSONObject afterBody = JSON.parseObject(editableRequest.getBody());
    if(Objects.isNull(afterBody)) {
      afterBody = new JSONObject();
    }
    afterBody.put("unionId", user.getUnionId());
    afterBody.put("userId", user.getId());
    editableRequest.setBody(afterBody.toString());
    // 继续处理请求
    chain.doFilter(editableRequest, response);
  }
}
