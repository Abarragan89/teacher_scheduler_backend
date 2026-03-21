package com.mathfactmissions.teacherscheduler.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private final Resend resend;
    
    public EmailService(@Value("${resend.api.key}") String resendApiKey) {
        this.resend = new Resend(resendApiKey);
    }
    
    public void sendEmail(String email, String link) {
        String html = String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; background: #f9f9f9; border-radius: 8px; border: 1px solid #ddd;">
                    <h2 style="color: #333; text-align: center;">Click the link to sign in</h2>
                    <p style="color: #555; font-size: 16px; text-align: center;">
                        We've generated a secure login link for you. Click the button below to access your account:
                    </p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s"
                           target="_blank"
                           rel="noopener noreferrer"
                           style="background-color: #4CAF50; color: #ffffff; padding: 14px 28px; text-decoration: none; font-size: 16px; border-radius: 6px; display: inline-block; font-weight: bold;">
                            Login to Website
                        </a>
                    </div>
                    <p style="color: #777; font-size: 14px; text-align: center;">
                        If the button above doesn't work, copy and paste this link into your browser:
                    </p>
                    <p style="word-break: break-all; color: #0066cc; text-align: center; font-size: 14px;">
                        <a href="%s" target="_blank" rel="noopener noreferrer" style="color: #0066cc;">%s</a>
                    </p>
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;" />
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        This link will expire soon for security reasons. If you didn't request it, you can safely ignore this email.
                    </p>
                </div>
            """, link, link, link);
        
        CreateEmailOptions params = CreateEmailOptions.builder()
            .from("Teach For Free <customer.team@math-fact-missions.com>")
            .to(email)
            .subject("Welcome To Teacher Scheduler")
            .html(html)
            .build();
        
        try {
            resend.emails().send(params);
            System.out.println("✅ Email sent to: " + email);
        } catch (ResendException e) {
            System.err.println("❌ Failed to send email to " + email + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email to " + email, e);
        }
    }
}