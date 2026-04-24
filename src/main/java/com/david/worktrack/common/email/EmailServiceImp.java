package com.david.worktrack.common.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImp {

    private final EmailSender emailSender;

    public void sendConfirmationEmail(String email, String name, String link) {
        emailSender.send(email, buildConfirmationEmail(name, link));
    }

    public void sendResetPasswordEmail(String email, String name, String link) {
        emailSender.send(email, buildResetPasswordEmail(name, link));
    }

    private String buildConfirmationEmail(String name, String link) {
        return "<p>Hello " + name + ",</p>"
                + "<p>Thank you for registering. Please click on the below link to activate your account:</p>"
                + "<a href=\"" + link + "\">Confirm Account</a>"
                + "<p>The link will expire in 15 minutes.</p>"
                + "<p>See you soon!</p>";
    }

    private String buildResetPasswordEmail(String name, String link) {
        return "<p>Hello " + name + ",</p>"
                + "<p>You requested to reset your password. Please click on the link below to reset it:</p>"
                + "<a href=\"" + link + "\">" + link + "</a>"  // Show full link
                + "<p>This link will expire in 15 minutes.</p>"
                + "<p>If you did not request this, you can ignore this email.</p>";
    }
}
