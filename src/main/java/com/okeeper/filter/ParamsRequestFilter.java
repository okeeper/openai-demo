package com.okeeper.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@Order(1)
@WebFilter(filterName = "ParamsRequestFilter", urlPatterns = {"/*"})
@Slf4j
@Component
public class ParamsRequestFilter implements Filter {
 
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new ParamsRequestWrapper((HttpServletRequest) request), response);
    }
}