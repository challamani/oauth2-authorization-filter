package com.challamani.filters.authz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Configuration
@Slf4j
public class UnprotectedWebMethodConfig implements ApplicationContextAware {

    protected static List<String> unprotectedEndpoints = new ArrayList<>();

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            if (!beanName.equalsIgnoreCase("unprotectedWebMethodConfig")) {
                Object obj = applicationContext.getBean(beanName);
                Class<?> objClass = obj.getClass();
                if (org.springframework.aop.support.AopUtils.isAopProxy(obj)) {
                    objClass = org.springframework.aop.support.AopUtils.getTargetClass(obj);
                }

                Arrays.stream(objClass.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(Unprotected.class)).forEach(method -> {
                            log.info("annotation details {}", method.getDeclaredAnnotations(), method);
                            Arrays.stream(method.getDeclaredAnnotations())
                                    .forEach(annotation -> {
                                        if (annotation instanceof GetMapping) {
                                            unprotectedEndpoints.add(((GetMapping) annotation).value()[0]);
                                        } else if (annotation instanceof RequestMapping) {
                                            unprotectedEndpoints.add(((RequestMapping) annotation).value()[0]);
                                        }
                                    });
                        });
            }
        }
        log.info("unprotected endpoints {}", unprotectedEndpoints);
    }
}
