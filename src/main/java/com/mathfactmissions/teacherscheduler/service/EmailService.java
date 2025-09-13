package com.mathfactmissions.teacherscheduler.service;

import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.resend.*;

@Service
public class EmailService {

    private final String resendApiKey;

    @Autowired
    public EmailService(@Value("${resend.api.key}") String resendApiKey) {
        this.resendApiKey = resendApiKey;
    }


    public void sendEmail(String email, String link) {
        Resend resend = new Resend(resendApiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Acme <customer.team@math-fact-missions.com>")
                .to(email)
                .subject("it works!")
                .html("<strong>hello world, click</strong>")
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            System.out.println(data.getId());
        } catch (ResendException e) {
            System.out.println("error sending email " + e);
        }

    }

}
