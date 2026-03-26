package com.foodapp.foodapp.utils;

import com.foodapp.foodapp.config.MailConfig;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class MailUtil {

    public static void sendOtp(String email,String otp) throws Exception {

        Properties props = MailConfig.getMailProperties();

        Session session = Session.getInstance(
                props,
                new Authenticator(){
                    protected PasswordAuthentication getPasswordAuthentication(){
                        return new PasswordAuthentication(
                                MailConfig.MAIL_USERNAME,
                                MailConfig.MAIL_PASSWORD
                        );
                    }
                }
        );

        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(MailConfig.MAIL_USERNAME));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(email)
        );

        message.setSubject("FoodApp OTP Verification");
        message.setText("Your OTP is: " + otp);

        Transport.send(message);
    }
}