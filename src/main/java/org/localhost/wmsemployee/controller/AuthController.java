package org.localhost.wmsemployee.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling OAuth2 browser-based authentication and web pages.
 * This controller handles the OAuth2 login flow for web browsers.
 */
@Controller
public class AuthController {

    /**
     * Home page endpoint - displays user profile if authenticated via OAuth2.
     *
     * @param model     Spring MVC model for passing data to the view
     * @param principal The authenticated OIDC user (null if not authenticated via OAuth2)
     * @return The view name to render
     */
    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal OidcUser principal) {
        if (principal != null) {
            model.addAttribute("profile", principal.getClaims());
            model.addAttribute("authenticated", true);
        } else {
            model.addAttribute("authenticated", false);
        }
        return "index";
    }

    /**
     * Login page endpoint - shows login options.
     *
     * @param model Spring MVC model
     * @return The login view name
     */
    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }
}
