package kz.axelrod.finalproject.service.impl;

import kz.axelrod.finalproject.config.EmailConfig;
import kz.axelrod.finalproject.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    private final String sender;
    private final String subject;
    private final List<String> defaultAdminEmails;

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender, EmailConfig emailConfig) {
        this.emailSender = emailSender;
        this.sender = emailConfig.getSender();
        this.subject = emailConfig.getSubject();
        this.defaultAdminEmails = emailConfig.getDefaultAdminEmails();
    }

    public void sendEmail(String certainEmail, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        List<String> recipients = certainEmail != null ? Collections.singletonList(certainEmail) : defaultAdminEmails;
        message.setTo(recipients.toArray(new String[0]));
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
        log.info("Mail notification was successfully sent to recipients[{}]", recipients);
    }

    public void sendEmailWithAttachment(String certainEmail, String text, String fileContent, String filename) throws MessagingException, IOException {
        var filePath = getCreatedFilePath(filename, fileContent);
        sendEmailWithAttachment(certainEmail, text, filePath);
    }

    private void sendEmailWithAttachment(String certainEmail, String text, String filePath) throws MessagingException {
        List<String> recipients = certainEmail != null ? Collections.singletonList(certainEmail) : defaultAdminEmails;
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(recipients.toArray(new String[0]));
        helper.setFrom(sender);
        helper.setSubject(subject);
        helper.setText(text, true);
        FileSystemResource file = new FileSystemResource(new File(filePath));
        helper.addAttachment(Objects.requireNonNull(file.getFilename()), file);
        emailSender.send(message);
        log.info("Mail notification was successfully sent to recipients[{}]", recipients);
    }

    private String getCreatedFilePath(String filename, String content) throws IOException {
        String directoryPath = Objects.requireNonNull(getClass().getClassLoader().getResource("files")).getPath();
        File directory = new File(directoryPath);
        if (!directory.exists()) directory.mkdirs();
        File file = new File(directory, filename);
        var fileState = file.createNewFile() ? "File created: " + file.getAbsolutePath() : "File already exists";
        log.info("{} with file-name {}", fileState, filename);
        var filePath = file.getAbsolutePath();

        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();
        return filePath;
    }
}
