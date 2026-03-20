package com.foodapp.foodapp.utils;

import com.foodapp.foodapp.config.DbConfig;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class MailUtil {

    public static void sendOtp(String email,String otp) throws Exception {

        Properties props = DbConfig.getMailProperties();

        Session session = Session.getInstance(
                props,
                new Authenticator(){
                    protected PasswordAuthentication getPasswordAuthentication(){
                        return new PasswordAuthentication(
                                DbConfig.MAIL_USERNAME,
                                DbConfig.MAIL_PASSWORD
                        );
                    }
                }
        );

        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(DbConfig.MAIL_USERNAME));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(email)
        );

        message.setSubject("FoodApp OTP Verification");
        message.setText("Your OTP is: " + otp);

        Transport.send(message);
    }
}