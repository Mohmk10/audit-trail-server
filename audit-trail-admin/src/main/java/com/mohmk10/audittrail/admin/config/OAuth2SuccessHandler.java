package com.mohmk10.audittrail.admin.config;

import com.mohmk10.audittrail.admin.domain.User;
import com.mohmk10.audittrail.admin.service.AuthService;
import com.mohmk10.audittrail.admin.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

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
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            String provider = oauthToken.getAuthorizedClientRegistrationId();

            String email = extractEmail(oAuth2User, provider);
            String name = extractName(oAuth2User, provider);
            String providerId = extractProviderId(oAuth2User, provider);

            User user = authService.findOrCreateOAuthUser(email, name, provider, providerId);
            String token = jwtService.generateToken(user);

            String targetUrl = redirectUri + "?token=" + token;
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }

    private String extractEmail(OAuth2User oAuth2User, String provider) {
        if ("github".equals(provider)) {
            String email = oAuth2User.getAttribute("email");
            if (email != null) {
                return email;
            }
            String login = oAuth2User.getAttribute("login");
            return login != null ? login + "@github.com" : null;
        }
        return oAuth2User.getAttribute("email");
    }

    private String extractName(OAuth2User oAuth2User, String provider) {
        if ("github".equals(provider)) {
            String name = oAuth2User.getAttribute("name");
            if (name != null) {
                return name;
            }
            return oAuth2User.getAttribute("login");
        }
        return oAuth2User.getAttribute("name");
    }

    private String extractProviderId(OAuth2User oAuth2User, String provider) {
        if ("github".equals(provider)) {
            Object id = oAuth2User.getAttribute("id");
            return id != null ? id.toString() : null;
        }
        return oAuth2User.getAttribute("sub");
    }
}
