package com.mohmk10.audittrail.admin.config;

import com.mohmk10.audittrail.admin.domain.User;
import com.mohmk10.audittrail.admin.service.AuthService;
import com.mohmk10.audittrail.admin.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final AuthService authService;
    private final JwtService jwtService;

    @Value("${app.oauth2.authorized-redirect-uri:https://audit-trail-dashboard.vercel.app/auth/oauth-callback}")
    private String redirectUri;

    public OAuth2SuccessHandler(@Lazy AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            log.info("OAuth2 authentication success, processing...");

            if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
                log.error("Authentication is not OAuth2AuthenticationToken: {}", authentication.getClass().getName());
                redirectWithError(response, "invalid_auth_type");
                return;
            }

            OAuth2User oAuth2User = oauthToken.getPrincipal();
            String provider = oauthToken.getAuthorizedClientRegistrationId();

            log.info("OAuth provider: {}", provider);
            log.info("OAuth2User attributes: {}", oAuth2User.getAttributes());

            String email = extractEmail(oAuth2User, provider);
            String name = extractName(oAuth2User, provider);
            String providerId = extractProviderId(oAuth2User, provider);

            log.info("Extracted - email: {}, name: {}, providerId: {}", email, name, providerId);

            if (email == null || email.isBlank()) {
                log.error("Could not extract email from OAuth2 response");
                redirectWithError(response, "no_email");
                return;
            }

            User user = authService.findOrCreateOAuthUser(email, name, provider, providerId);
            log.info("User found/created with ID: {}", user.getId());

            String token = jwtService.generateToken(user);
            log.info("JWT token generated successfully");

            String targetUrl = redirectUri + "?token=" + token;
            log.info("Redirecting to: {}", redirectUri + "?token=***");

            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("Error in OAuth2 success handler: {}", e.getMessage(), e);
            redirectWithError(response, e.getMessage());
        }
    }

    private void redirectWithError(HttpServletResponse response, String errorMessage) throws IOException {
        String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        String errorUrl = redirectUri + "?error=" + encodedError;
        response.sendRedirect(errorUrl);
    }

    private String extractEmail(OAuth2User oAuth2User, String provider) {
        if ("github".equals(provider)) {
            String email = oAuth2User.getAttribute("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
            String login = oAuth2User.getAttribute("login");
            if (login != null) {
                return login + "@github.user";
            }
            return null;
        }
        return oAuth2User.getAttribute("email");
    }

    private String extractName(OAuth2User oAuth2User, String provider) {
        if ("github".equals(provider)) {
            String name = oAuth2User.getAttribute("name");
            if (name != null && !name.isBlank()) {
                return name;
            }
            return oAuth2User.getAttribute("login");
        }
        String name = oAuth2User.getAttribute("name");
        return name != null ? name : "User";
    }

    private String extractProviderId(OAuth2User oAuth2User, String provider) {
        if ("github".equals(provider)) {
            Object id = oAuth2User.getAttribute("id");
            return id != null ? id.toString() : null;
        }
        return oAuth2User.getAttribute("sub");
    }
}
