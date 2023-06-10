package kz.axelrod.finalproject.service;

import javax.mail.MessagingException;
import java.io.IOException;

public interface EmailService {

    void sendEmailWithAttachment(String certainEmail, String text, String fileContent, String filename) throws MessagingException, IOException;

    void sendEmail(String to, String text);
}
