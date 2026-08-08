package com.moustapha.tp.clients_api.config;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.moustapha.tp.clients_api.service.RateLimitService;
import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        // Le rate limit ne s'applique qu'aux routes /api/
        if (request.getRequestURI().startsWith("/api")) {
            String key = "rate-limit:api:" + request.getRemoteAddr();

            if (rateLimitService.isAllowed(key)) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(429);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Limite de requête atteinte. Réessayez plus tard.");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
