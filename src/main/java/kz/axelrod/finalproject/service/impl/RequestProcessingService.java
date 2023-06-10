package kz.axelrod.finalproject.service.impl;

import kz.axelrod.finalproject.exception.InternalServerError;
import kz.axelrod.finalproject.model.RequestGasPumpingUnit;
import kz.axelrod.finalproject.model.ResponsiblePerson;
import kz.axelrod.finalproject.model.Status;
import kz.axelrod.finalproject.repository.RequestGasPumpingUnitRepository;
import kz.axelrod.finalproject.service.EmailService;
import kz.axelrod.finalproject.utils.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestProcessingService {

    private final RequestGasPumpingUnitRepository requestGasPumpingUnitRepo;
    private final EmailService emailService;
    private final MessageSource messageSource;

    public void process(RequestGasPumpingUnit request, ResponsiblePerson responsiblePerson, Locale locale, IProcess process) throws InternalServerError {
        try {
            process.exec();
        } catch (Exception ex) {
            request.setStatus(Status.ERROR.toString());
            request.setTimeOfMailMessageReport(LocalDateTime.now());
            emailService.sendEmail(responsiblePerson.getResponsiblePersonMail(), Messages.GENERAL_ERROR.i18n(messageSource, locale));
            log.error("An error occurred while solving the problem.", ex);
            throw new InternalServerError("An error occurred while solving the problem");
        } finally {
            requestGasPumpingUnitRepo.save(request);
            requestGasPumpingUnitRepo.flush();
        }
    }

    @FunctionalInterface
    public interface IProcess {
        void exec() throws InternalServerError;
    }
}
