package com.challamani.filters.authz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class AuthzFilter implements Filter {

    private final List<ScopeResourceMap> scopeResourceMapList;
    @Autowired
    private AuthzFilter(ResourceLoader resourceLoader) throws IOException {
        byte[] bytes = resourceLoader.getResource("classpath:scopes-bindings.json")
                .getInputStream().readAllBytes();
        String jsonData = new String(bytes).toString();
        log.info("scope resource json data {}", jsonData);
        scopeResourceMapList = new ObjectMapper().readValue(jsonData, new TypeReference<>() {
        });
    }
    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        String token = getToken(servletRequest)
                .orElseThrow(() -> new AuthenticationServiceException("No token found!"));

        JWT jwt = JWTParser.parse(token);
        String scope = jwt.getJWTClaimsSet().getClaims().get("scope").toString();
        log.debug("scope {} ", scope);
        log.debug("scopeResourceMapList {}", scopeResourceMapList);

        Authentication authentication  = SecurityContextHolder.getContext().getAuthentication();
        if(Objects.isNull(authentication) || !authentication.isAuthenticated()){
            throw new AuthenticationException("Invalid credentials!");
        }
        if(!isAuthorized(Arrays.asList(scope.split(" ")), servletRequest)){
            throw new AuthorizationServiceException("access denied for the given scopes!");
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private Optional<String> getToken(ServletRequest request) {
        String bearerToken = ((HttpServletRequest)request).getHeader("Authorization");
        log.debug("bearer token {}",bearerToken); //this we should not enable for prod instance
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String jwt = bearerToken.substring(7, bearerToken.length());
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    private boolean isAuthorized(List<String> scopes, ServletRequest request) {
        HttpServletRequest httpServletRequest = ((HttpServletRequest) request);
        String method = httpServletRequest.getMethod();
        String resourceUri = httpServletRequest.getRequestURI();
        log.debug("token scopes {} method {} and resource URI {}", scopes, method, resourceUri);

        boolean anyMatch = scopeResourceMapList.stream()
                .filter(scopeResourceMap ->
                        Pattern.compile(scopeResourceMap.getResourceId()).matcher(resourceUri).find()
                        && scopeResourceMap.getMethods().contains(method))
                .anyMatch(scopeResourceMap ->
                        scopeResourceMap.getScopes().stream().anyMatch(scope -> scopes.contains(scope))
                        || scopeResourceMap.getScopes().contains("*"));
        return anyMatch;
    }
}
