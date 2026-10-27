package com.cet6.config;

import com.cet6.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 开发期跨域(Vite 5173 → 后端 8080)+ 登录拦截 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
    }

    static class LoginInterceptor implements HandlerInterceptor {
        private final ObjectMapper json = new ObjectMapper();

        @Override
        public boolean preHandle(HttpServletRequest req, HttpServletResponse resp,
                                 Object handler) throws Exception {
            HttpSession s = req.getSession(false);
            if (s != null && s.getAttribute("uid") != null) return true;
            resp.setStatus(200);
            resp.setContentType("application/json;charset=utf-8");
            resp.getWriter().write(json.writeValueAsString(Result.fail("未登录")));
            return false;
        }
    }
}
