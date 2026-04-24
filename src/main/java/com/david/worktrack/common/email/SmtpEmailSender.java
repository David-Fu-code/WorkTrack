package com.david.worktrack.common.email;


import com.david.worktrack.common.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender{

    private final JavaMailSender emailSender;

    @Override
    public void send(String to, String subject, String content) {

        try {
           MimeMessage mimeMessage = emailSender.createMimeMessage();
           MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

           helper.setTo(to);
           helper.setSubject(subject);
           helper.setText(content, true);
           helper.setFrom("example@example.com");

           emailSender.send(mimeMessage);

       }catch (MessagingException e) {
           throw new EmailSendingException("Failed to send email", e);
       }

    }
}
