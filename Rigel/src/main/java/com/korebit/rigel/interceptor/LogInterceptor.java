package com.korebit.rigel.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LogInterceptor implements HandlerInterceptor {

    @Bean
    public HandlerInterceptor handlerInterceptor() {
        return this;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        System.out.println("Interceptando antes del controlador");
        System.out.println("Controller: " + request.getRequestURI());
        System.out.println("Handler: " + request.getMethod());
        System.out.println("Contenido: " + request.getContentType());
        System.out.println("Contenido de la respuesta: " + response.getContentType());
        System.out.println(handler.getClass().getName());

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           @NonNull Object handler,
                           ModelAndView modelAndView) throws Exception {
        System.out.println("Interceptando después del controlador");
        System.out.println("Controller: " + request.getRequestURI());
        System.out.println("Handler: " + request.getMethod());
        System.out.println("Contenido: " + request.getContentType());
        System.out.println("Contenido de la respuesta: " + response.getContentType());
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) throws Exception {
        System.out.println("Interceptando al finalizar la petición");
    }
}