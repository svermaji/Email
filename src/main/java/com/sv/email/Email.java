package com.sv.email;

import com.sv.core.logger.MyLogger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

public class Email {

    private static MyLogger logger;
    private static Properties emailProp = new Properties();

    static {
        init();
    }

    private static void init() {
        logger = MyLogger.createLogger("email.log");
        emailProp = new Properties();
        try {
            emailProp.load(Email.class.getClassLoader().getResourceAsStream("email.properties"));
        } catch (IOException e) {
            logger.error("Unable to load email properties.", e);
        }
    }

    public static void main(String[] args) {
        String user = System.getenv("TEST_GMAIL_USER");
        EmailDetails details = new EmailDetails(user, user, "subject");
        setUserFromEnv(details);
        details.setBody("Test");
        send(details);
    }

    private static void setUserFromEnv(EmailDetails details) {
        details.setUser(System.getenv("TEST_GMAIL_USER"));
        details.setPwd(System.getenv("TEST_GMAIL_PWD"));
    }

    private static Session authenticate(String user, String pwd) {
        return Session.getInstance(emailProp,
                new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication(user, pwd);
                    }
                });
    }

    public static void send(EmailDetails details) {
        send(details, false);
    }

    public static void send(EmailDetails details, boolean useEnvCred) {
        try {
            if (useEnvCred) {
                setUserFromEnv(details);
            }

            Message message = new MimeMessage(authenticate(details.getUser(), details.getPwd()));
            message.setFrom(new InternetAddress(details.getFrom()));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(details.getTo())
            );
            message.setSubject(details.getSubject());
            message.setText(details.getBody());

            Transport.send(message);
            logger.log("Email send " + details);
        } catch (MessagingException e) {
            logger.error("Email failed!! " + details, e);
        }
    }
}
