package com.okeeper.filter;

import com.okeeper.controller.dto.Result;
import com.okeeper.utils.SentinelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@WebFilter(urlPatterns = "/*")
@Order(11)
public class SentinelLimitFilter extends HttpFilter {
	
	@Override
	protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
		if(!SentinelUtils.checkLimited(SentinelUtils.URI)) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.warn("limited by uri={}", req.getRequestURI());
			IOUtils.write(Result.fail("你操作太频繁了，休息一下吧").toJsonString(), res.getOutputStream());
			return;
		}
		String openId = (String) req.getAttribute("openId");
		if(openId != null && !SentinelUtils.checkLimited(SentinelUtils.URI_OPEN_ID, openId)) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.warn("limited by openId {}, uri={}", openId,  req.getRequestURI());
			IOUtils.write(Result.fail("你操作太频繁了，休息一下吧").toJsonString(), res.getOutputStream());
			return;
		}
		// 执行请求链
		super.doFilter(req, res, chain);
	}
}