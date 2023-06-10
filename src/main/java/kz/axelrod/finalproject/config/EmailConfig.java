package kz.axelrod.finalproject.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "email")
public class EmailConfig {

    private String sender;
    private String subject;
    List<String> defaultAdminEmails;
}
