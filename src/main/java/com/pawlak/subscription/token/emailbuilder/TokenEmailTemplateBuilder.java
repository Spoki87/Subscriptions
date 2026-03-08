package com.pawlak.subscription.token.emailbuilder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class TokenEmailTemplateBuilder {

    private final TemplateEngine templateEngine;

    @Value("${spring.app.base_url}")
    private String basePath;

    public String buildConfirmationEmail(String token) {
        Context context = new Context();
        context.setVariable("URL",basePath+"/api/user/confirm?token="+token);
        return templateEngine.process("confirm-account", context);
    }

    public String buildResetPasswordEmail(String token) {
        Context context = new Context();
        context.setVariable("TOKEN", token);
        return templateEngine.process("reset-password", context);
    }
}
