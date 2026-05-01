package com.tunesharehub.config;

import com.tunesharehub.auth.AuthUserArgumentResolver;
import com.tunesharehub.auth.JwtInterceptor;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final AuthUserArgumentResolver authUserArgumentResolver;

    public WebConfig(JwtInterceptor jwtInterceptor, AuthUserArgumentResolver authUserArgumentResolver) {
        this.jwtInterceptor = jwtInterceptor;
        this.authUserArgumentResolver = authUserArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/health",
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/auth/logout",
                        "/api/spotify/search/**"
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authUserArgumentResolver);
    }
}
