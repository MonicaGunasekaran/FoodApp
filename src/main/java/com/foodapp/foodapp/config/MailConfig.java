package com.foodapp.foodapp.config;

import java.util.Properties;

public class MailConfig {
    public static Properties getMailProperties(){

        Properties props = new Properties();

        props.put("mail.smtp.host","smtp.gmail.com");
        props.put("mail.smtp.port","587");
        props.put("mail.smtp.auth","true");
        props.put("mail.smtp.starttls.enable","true");

        return props;
    }

    public static String MAIL_USERNAME =  "monica934541@gmail.com";
    public static String MAIL_PASSWORD = "oypwnfoyswbuaetn";
}
