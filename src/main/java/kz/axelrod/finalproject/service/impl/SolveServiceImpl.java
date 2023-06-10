package kz.axelrod.finalproject.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import kz.axelrod.finalproject.exception.InternalServerError;
import kz.axelrod.finalproject.model.RequestGasPumpingUnit;
import kz.axelrod.finalproject.model.ResponsiblePerson;
import kz.axelrod.finalproject.model.Status;
import kz.axelrod.finalproject.model.dto.RequestDto;
import kz.axelrod.finalproject.model.dto.RequestGasPumpingUnitDto;
import kz.axelrod.finalproject.model.dto.ResultDto;
import kz.axelrod.finalproject.repository.GasPumpingUnitRepository;
import kz.axelrod.finalproject.repository.RequestGasPumpingUnitRepository;
import kz.axelrod.finalproject.repository.ResponsiblePersonRepository;
import kz.axelrod.finalproject.service.EmailService;
import kz.axelrod.finalproject.service.RedisService;
import kz.axelrod.finalproject.service.SolveService;
import kz.axelrod.finalproject.utils.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static kz.axelrod.finalproject.utils.PhysicalConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolveServiceImpl implements SolveService {

    private final RedisService redisService;
    private final RequestProcessingService requestProcessingService;
    private final EmailService emailService;
    private final GasPumpingUnitRepository gasPumpingUnitRepo;
    private final ResponsiblePersonRepository responsiblePersonRepo;
    private final RequestGasPumpingUnitRepository requestGasPumpingUnitRepo;
    private final MessageSource messageSource;

    public List<RequestGasPumpingUnitDto> getLastResultUsingGet(String labelName) {
        var results = requestGasPumpingUnitRepo.findLastRequestGasPumpingUnit();
        var requestGasPumpingUnitDtoList = new ArrayList<RequestGasPumpingUnitDto>();
        for (var i : results) {
            RequestGasPumpingUnitDto temp = RequestGasPumpingUnitDto.builder().requestId(i.getRequestId()).build();
            temp.setCertainValue(labelName.equals("temperature") ? i.getTemperature()
                    : labelName.equals("pressure") ? i.getPressure()
                        : i.getCommercialPerformance());
            requestGasPumpingUnitDtoList.add(temp);
        }
        return requestGasPumpingUnitDtoList;
    }

    public ResultDto getResultDto(MultipartFile file) {
        var requestDto = getFileData(file);
        log.info("RequestDto = {}", requestDto);
        return getResultDto(requestDto, Boolean.TRUE);
    }

    public List<ResultDto> getSeveralResultDto(MultipartFile file) {
        var requestDtoList = getMultipleFileData(file);
        log.info("RequestDtoList = {}", requestDtoList);
        return requestDtoList.stream().map(requestDto ->
                        getResultDto(requestDto, Boolean.FALSE))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ResultDto getResultDto(RequestDto requestDto) {
        log.info("RequestDto = {}", requestDto);
        return getResultDto(requestDto, Boolean.TRUE);
    }

    public ResultDto getResultDto(Long gpuId, Long actualSpeed, Long inputVolumeFlowUnderOperatingConditions,
                                  Double inputInletGasTemperature, Double inputGasPressureAtTheCompressorInlet,
                                  Double inputGasDensityAtStandardConditions, Double inputExternalAirPressure,
                                  Double inputOutdoorTemperature, String language) {
        var requestDto = RequestDto.builder()
                .gpuId(gpuId)
                .actualSpeed(actualSpeed)
                .inputVolumeFlowUnderOperatingConditions(inputVolumeFlowUnderOperatingConditions)
                .inputInletGasTemperature(inputInletGasTemperature)
                .inputGasPressureAtTheCompressorInlet(inputGasPressureAtTheCompressorInlet)
                .inputGasDensityAtStandardConditions(inputGasDensityAtStandardConditions)
                .inputExternalAirPressure(inputExternalAirPressure)
                .inputOutdoorTemperature(inputOutdoorTemperature)
                .language(language)
                .build();
        log.info("RequestDto = {}", requestDto);
        return getResultDto(requestDto, Boolean.TRUE);
    }

    private ResultDto getResultDto(RequestDto requestDto, Boolean isCertain) {
        if (redisService.hasKeyOfData(String.valueOf(requestDto.hashCode()))) {
            return redisService.getData(String.valueOf(requestDto.hashCode()));
        }
        log.info("Input-Locale={}", requestDto.getLanguage());
        Locale currentLocale = LocaleContextHolder.getLocale();
        if (requestDto.getLanguage() != null) {
            currentLocale = requestDto.getLanguage().equals("es") ? Locale.forLanguageTag("es-ES")
                    : requestDto.getLanguage().equals("ru") ? Locale.forLanguageTag("ru-RU")
                        : requestDto.getLanguage().equals("kk") ? Locale.forLanguageTag("kk-KK")
                            : Locale.ENGLISH;
        }
        final Locale finalLocale = currentLocale;
        log.info("CurrentFinalLocale={}", finalLocale);
        actualSpeed.setValue(Double.valueOf(requestDto.getActualSpeed()));                                                      // 4900
        volumeFlowUnderOperatingConditions.setValue(Double.valueOf(requestDto.getInputVolumeFlowUnderOperatingConditions()));   // 388000
        inletGasTemperature.setValue(requestDto.getInputInletGasTemperature());                                 // 4
        gasPressureAtTheCompressorInlet.setValue(requestDto.getInputGasPressureAtTheCompressorInlet());         // 30.5
        gasDensityAtStandardConditions.setValue(requestDto.getInputGasDensityAtStandardConditions());           // 0
        externalAirPressure.setValue(requestDto.getInputExternalAirPressure());                                 // 738
        outdoorTemperature.setValue(requestDto.getInputOutdoorTemperature());                                   // 4

        var requestGasPumpingUnit = buildRequestGasPumpingUnit(requestDto.getGpuId(), LocalDateTime.now());
        var responsiblePerson = getResponsiblePerson(requestGasPumpingUnit.getGpuId());
        var resultDtoWrapper = new ArrayList<ResultDto>();
        StringBuilder sb = new StringBuilder();
        requestProcessingService.process(requestGasPumpingUnit, responsiblePerson, currentLocale, () -> {
            initializeParams();

            sb.append(Messages.GENERAL_MESSAGE.i18n(messageSource, finalLocale));
            sb.append(Messages.GENERAL_VOLUME.i18n(messageSource, finalLocale).replaceAll("swapvalue",
                    String.format("%8.2f", volumeFlowUnderOperatingConditions.getValue())));
            sb.append(Messages.GENERAL_INLET_TEMPERATURE.i18n(messageSource, finalLocale).replaceAll("swapvalue",
                    String.format("%8.2f", inletGasTemperature.getValue())));
            sb.append(Messages.GENERAL_INLET_PRESSURE.i18n(messageSource, finalLocale).replaceAll("swapvalue",
                    String.format("%8.2f", gasPressureAtTheCompressorInlet.getValue())));
            sb.append(Messages.GENERAL_DENSITY.i18n(messageSource, finalLocale).replaceAll("swapvalue",
                    String.format("%8.2f", gasDensityAtStandardConditions.getValue())));
            sb.append(Messages.GENERAL_EXTERNAL.i18n(messageSource, finalLocale).replaceAll("swapvalue",
                    String.format("%8.2f", externalAirPressure.getValue())));
            sb.append(Messages.GENERAL_OUTDOOR.i18n(messageSource, finalLocale).replaceAll("swapvalue",
                    String.format("%8.2f", outdoorTemperature.getValue())));
            sb.append(Messages.GENERAL_SAKE.i18n(messageSource, finalLocale));
            sb.append(Messages.GENERAL_SAKE.i18n(messageSource, finalLocale));
            sb.append(Messages.GENERAL_SAKE.i18n(messageSource, finalLocale));

            double compressionRatio = getCompressionRatio();
            var roundedCompressionRation = BigDecimal.valueOf(compressionRatio).setScale(4, RoundingMode.HALF_UP);

            var gasPressureAtTheBlowerOutlet = getGasPressureAtTheBlowerOutlet(compressionRatio);
            var gasTemperatureAtTheBlowerOutlet = getGasTemperatureAtTheBlowerOutlet(compressionRatio);
            var requiredRevolutionDesiredRotorSpeed = getRequiredRevolutionDesiredRotorSpeed(compressionRatio);
            var coefficientOfDistanceFromTheSurgeZone = getCoefficientOfDistanceFromTheSurgeZone();
            var roundedCoefficientOfDistanceFromTheSurgeZone = BigDecimal.valueOf(CLUTCH_POWER_CONSUMED_BY_SUPERCHARGER.getValue()).setScale(2, RoundingMode.HALF_UP);
            var availableDrivePower = getAvailableDrivePower();
            var roundedAvailableDrivePower = BigDecimal.valueOf(availableDrivePower).setScale(2, RoundingMode.HALF_UP);
            var roundedReserveDrivePower = BigDecimal.valueOf(availableDrivePower - CLUTCH_POWER_CONSUMED_BY_SUPERCHARGER.getValue())
                    .setScale(2, RoundingMode.HALF_UP);     // Резерв мощность привода ГПА: кВт
            var roundedDriveLoadFactor = BigDecimal.valueOf(CLUTCH_POWER_CONSUMED_BY_SUPERCHARGER.getValue() / availableDrivePower)
                    .setScale(2, RoundingMode.HALF_UP);     // Коэффициент загрузки привода ГПА
            var roundedFuelGasConsumption = BigDecimal.valueOf(getFuelGasConsumption() * 1000D).setScale(2, RoundingMode.HALF_UP);
            var electricityConsumption = getElectricityConsumption();
            var roundedCommercialPerformance = BigDecimal.valueOf(getCommercialPerformance()).setScale(2, RoundingMode.HALF_UP);

            sb.append(Messages.OUTPUT_PRESSURE.i18n(messageSource, finalLocale).replaceAll("swapvalue", String.format("%8.2f", gasPressureAtTheBlowerOutlet)));
            sb.append(Messages.OUTPUT_TEMPERATURE.i18n(messageSource, finalLocale).replaceAll("swapvalue", String.format("%8.2f", gasTemperatureAtTheBlowerOutlet)));
            sb.append(Messages.OUTPUT_COMPRESSION.i18n(messageSource, finalLocale).replaceAll("swapvalue", String.format("%8.2f", roundedCompressionRation.doubleValue())));
            sb.append(Messages.OUTPUT_RPM.i18n(messageSource, finalLocale).replaceAll("swapvalue", String.format("%8.2f", requiredRevolutionDesiredRotorSpeed)));
            sb.append(Messages.OUTPUT_SURGE.i18n(messageSource, finalLocale).replaceAll("swapvalue", String.format("%8.2f", coefficientOfDistanceFromTheSurgeZone)));
            sb.append(Messages.OUTPUT_ROUNDED_SURGE.i18n(messageSource, finalLocale).replaceAll("swapvalue", String.format("%8.2f", roundedCoefficientOfDistanceFromTheSurgeZone.doubleValue())));
            sb.append(Messages.OUTPUT_POWER.i18n(messageSource, finalLocale).replaceAll("swapvalue", String.format("%8.2f", roundedAvailableDrivePower.doubleValue())));
            sb.append(Messages.OUTPUT_RESERVE.i18n(messageSource, finalLocale).replaceAll("swapvalue", String.format("%8.2f", roundedReserveDrivePower.doubleValue())));
            sb.append(Messages.OUTPUT_LOAD.i18n(messageSource, finalLocale).replaceAll("swapvalue", String.format("%8.2f", roundedDriveLoadFactor.doubleValue())));
            sb.append(Messages.OUTPUT_FUEL.i18n(messageSource, finalLocale).replaceAll("swapvalue", String.format("%8.2f", roundedFuelGasConsumption.doubleValue())));
            sb.append(Messages.OUTPUT_ELECTRICITY.i18n(messageSource, finalLocale).replaceAll("swapvalue", String.format("%8.2f", electricityConsumption)));
            sb.append(Messages.OUTPUT_COMMERCIAL.i18n(messageSource, finalLocale).replaceAll("swapvalue", String.format("%8.2f", roundedCommercialPerformance.doubleValue())));

            log.info("FileContent: {}", sb);
            requestGasPumpingUnit.setStatus(Status.OK.toString());
            requestGasPumpingUnit.setTimeOfMailMessageReport(LocalDateTime.now());
            requestGasPumpingUnit.setIsTemperatureDifference(TEMPERATURE_DIFFERENCE.getValue() != 0D);
            requestGasPumpingUnit.setTemperature(gasTemperatureAtTheBlowerOutlet);
            requestGasPumpingUnit.setPressure(gasPressureAtTheBlowerOutlet);
            requestGasPumpingUnit.setCommercialPerformance(roundedCommercialPerformance.doubleValue());
            resultDtoWrapper.add(ResultDto.builder()
                    .volumeFlowUnderOperatingConditions(volumeFlowUnderOperatingConditions.getValue())
                    .inletGasTemperature(inletGasTemperature.getValue())
                    .gasPressureAtTheCompressorInlet(gasPressureAtTheCompressorInlet.getValue())
                    .gasDensityAtStandardConditions(gasDensityAtStandardConditions.getValue())
                    .externalAirPressure(externalAirPressure.getValue())
                    .outdoorTemperature(outdoorTemperature.getValue())
                    .gasPressureAtTheBlowerOutlet(gasPressureAtTheBlowerOutlet)
                    .gasTemperatureAtTheBlowerOutlet(gasTemperatureAtTheBlowerOutlet)
                    .compressionRatio(roundedCompressionRation.doubleValue())
                    .requiredRevolutionDesiredRotorSpeed(requiredRevolutionDesiredRotorSpeed)
                    .coefficientOfDistanceFromTheSurgeZone(coefficientOfDistanceFromTheSurgeZone)
                    .clutchPower(roundedCoefficientOfDistanceFromTheSurgeZone.doubleValue())
                    .availableDrivePower(roundedAvailableDrivePower.doubleValue())
                    .reserveDrivePower(roundedReserveDrivePower.doubleValue())
                    .driveLoadFactor(roundedDriveLoadFactor.doubleValue())
                    .fuelGasConsumption(roundedFuelGasConsumption.doubleValue())
                    .electricityConsumption(electricityConsumption)
                    .commercialPerformance(roundedCommercialPerformance.doubleValue())
                    .build());
        });
        redisService.saveData(String.valueOf(requestDto.hashCode()), resultDtoWrapper.get(0));
        if (isCertain) sendEmailWithAttachment(requestGasPumpingUnit.getGpuId(), responsiblePerson, sb.toString(), currentLocale);
        log.info("ResultDto = {}", resultDtoWrapper.get(0));
        return resultDtoWrapper.get(0);
    }

    private Double getCommercialPerformance() {
        // коммерческая производительность ГПА, м^3/час
        return (VOLUMETRIC_REDUCED_FLOW.getValue() * ((0.00144D * INLET_GAS_PRESSURE_AT_THE_COMPRESSOR.getValue() * 1000000D * relativeTurns.getValue())
                / (gasDensityAtStandardConditions.getValue() * GAS_COMPRESSIBILITY_FACTOR.getValue() *
                GAS_CONSTANT_OF_COMPRESSED_GAS.getValue() * inletGasTemperatureKelvin.getValue())) * 1000000D) / 24D;
    }

    private Double getElectricityConsumption() {
        // время в час
        double tt = 1D;

        // Расход электроэнергии ГПА, кВт
        var roundedResult = BigDecimal.valueOf((CLUTCH_POWER_CONSUMED_BY_SUPERCHARGER.getValue() * tt) / (0.975D * 0.99D)).setScale(2, RoundingMode.HALF_UP);
        return roundedResult.doubleValue();
    }

    private Double getFuelGasConsumption() {
        // номинальный расход топливного газа на КЦ, тыс.ст.м^3/час
        double nominalFuelGasFlow = ((3.6D * Nnom.getValue()) / ((ettan.getValue() / 100D) * 34500D)) * kn.getValue();
        log.info("NominalFuelGasFlow={}", String.format("%8.3f", nominalFuelGasFlow));

        // расход топливного газа на КЦ, тыс. м^3/час
        double result = nominalFuelGasFlow * (0.75D * (CLUTCH_POWER_CONSUMED_BY_SUPERCHARGER.getValue() / Nnom.getValue()) + 0.25D
                * sqrt(getEstimatedInletTemperature() / getNominalGasTurbineInletTemperature())
                * ((externalAirPressure.getValue() * 0.0001333224D) / 0.1013D)) * 1.2D;
        log.info("FuelGasConsumption={}", String.format("%8.2f", result * 1000D));
        var roundedResult = BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP);
        return roundedResult.doubleValue();
    }

    private Double getNominalGasTurbineInletTemperature() {
        // Номинальная температура на входе в ГТУ, К
        double result = outdoorTemperature.getValue() + 273.15D;
        log.info("NominalGasTurbineInletTemperature={}", String.format("%8.2f", result));
        return result;
    }

    private Double getEstimatedInletTemperature() {
        // Среднегодовая температура окружающего воздуха, К
        double Ta = 285.35D;

        // Поправка на изменчивость климатических параметров и местный подогрев наруж. Воздуха на входе в ГТУ, К
        double dTa = 5.0D;

        // Расчетная температура на входе в ГТУ, К
        double result = Ta + dTa;
        log.info("EstimatedInletTemperature={}", String.format("%8.2f", result));
        return result;
    }

    private Double getAvailableDrivePower() {
        // Коэффициент технического состояния по мощности
        double Ktn = INTERNAL_BLOWER_POWER.getValue() / (Nnom.getValue() * 0.55D);

        // Коэффициент, учитывающий противообледелительной системы
        double Kob = 1.13D;

        // Коэффициент, учитывающий влияние температуры наружного воздуха
        double Kt = 1.02D + 0.0025D * (outdoorTemperature.getValue() + 5D);

        // Коэффициент, учитывающий влияние системы утилизации тепла выхлопных газов
        double Ky = 0.985D;
        log.info("Ktn={}", String.format("%8.2f", Ktn));
        log.info("Kt={}", String.format("%8.2f", Kt));

        // Располагаемая мощность привода, кВт
        double result = Nnom.getValue() * Ktn * Kob * Ky *
                (1D - Kt *
                        ((getEstimatedInletTemperature() - getNominalGasTurbineInletTemperature()) / getEstimatedInletTemperature()))
                * (getExternalAirPressure() / 0.1013D);

        if ((CLUTCH_POWER_CONSUMED_BY_SUPERCHARGER.getValue() <= result) && (result <= 1.15D * Nnom.getValue())) {
            log.info("AVAILABLE POWER STATE COMPLIES/УСЛОВИЕ РАСПОЛАГАЕМОЙ МОЩНОСТИ ВЫПОЛНЯЕТСЯ:");
            log.info("{} <= {} <= {} kVt", String.format("%8.2f", CLUTCH_POWER_CONSUMED_BY_SUPERCHARGER.getValue()),
                    String.format("%8.2f", result), String.format("%8.2f", 1.15D * Nnom.getValue()));
        }
        log.info("AvailableDrivePower={}", String.format("%8.2f", result));
        return result;
    }

    private Double getCoefficientOfDistanceFromTheSurgeZone() {
        // коэффициент удаленности от зоны помпажа
        double result = VOLUMETRIC_REDUCED_FLOW.getValue() / qprmin.getValue();
        if ((VOLUMETRIC_REDUCED_FLOW.getValue() / qprmin.getValue()) >= 1.1D) {
            log.info("CALCULATION OF THE REMOTE OF THE SUPPLIER OPERATION MODE FROM THE BORDERS TO BE PERFORMED");
            log.info("РАСЧЕТ УДАЛЕННОСТИ РЕЖИМА РАБОТЫ НАГНЕТАТЕЛЯ ОТ ГРАНИЦ ВЫПОЛНЯТЬСЯ: {} >= 1.1", String.format("%8.2f", VOLUMETRIC_REDUCED_FLOW.getValue() / qprmin.getValue()));
        }
        if (((qprmin.getValue() * result) <= VOLUMETRIC_REDUCED_FLOW.getValue()) && (VOLUMETRIC_REDUCED_FLOW.getValue() <= qprmax.getValue())) {
            log.info("CALCULATION OF REMOTE MODE OF OPERATION");
            log.info("РАСЧЕТ УДАЛЕННОСТИ РЕЖИМА РАБОТЫ НАГНЕТАТЕЛЯ ОТ ГРАНИЦ ВЫПОЛНЯТЬСЯ: {} <= {} <= {}", String.format("%8.2f", qprmin.getValue() * result),
                    String.format("%8.2f", VOLUMETRIC_REDUCED_FLOW.getValue()), String.format("%8.2f", qprmax.getValue()));
        }
        var roundedResult = BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP);
        return roundedResult.doubleValue();
    }

    private Double getResultOfFormula_2_208(Double compressionRatio) {
        double er1 = (POLYTROPIC_EFFICIENCY.getValue() * GAS_ADIABATIC_INDEX_KOBZ_FORMULA.getValue()) / (GAS_ADIABATIC_INDEX_KOBZ_FORMULA.getValue() - 1D);
        double er2 = (GAS_COMPRESSIBILITY_FACTOR.getValue() * inletGasTemperatureKelvin.getValue() * GAS_CONSTANT_OF_COMPRESSED_GAS.getValue())
                / (((REDUCED_RELATIVE_INTERNAL_POWER.getValue() * 1000D * 60D) / VOLUMETRIC_REDUCED_FLOW.getValue()) * POLYTROPIC_EFFICIENCY.getValue());
        double er3 = pow(compressionRatio, (GAS_ADIABATIC_INDEX_KOBZ_FORMULA.getValue() - 1D) / (POLYTROPIC_EFFICIENCY.getValue() * GAS_ADIABATIC_INDEX_KOBZ_FORMULA.getValue()));
        return sqrt(er1 * er2 * (er3 - 1.));
    }

    private Double getRequiredRevolutionDesiredRotorSpeed(Double compressionRatio) {
        // Формула (2.208) - Сарданашвили, стр.185
        // Требуемый оборот (искомая частота вращения ротора), об/мин
        var roundedResult = BigDecimal.valueOf(ntnd.getValue() * getResultOfFormula_2_208(compressionRatio)).setScale(2, RoundingMode.HALF_UP);
        return roundedResult.doubleValue();
    }

    private Double getExternalAirPressure() {
        double resultExternalAirPressure = externalAirPressure.getValue() * 0.0001333224;
        log.info("ResultExternalAirPressure={}", String.format("%8.2f", resultExternalAirPressure));
        return resultExternalAirPressure;
    }

    private Double getGasPressureAtTheBlowerOutlet(Double compressionRatio) {
        // Давление газа на выходе нагнетателя, МПа
        double airPressure = compressionRatio * INLET_GAS_PRESSURE_AT_THE_COMPRESSOR.getValue();
        log.info("Давление газа на выходе из нагнетателя (MPa): P2={}", String.format("%8.2f", airPressure * 10.19716D - getExternalAirPressure()));
        var roundedResult = BigDecimal.valueOf(airPressure * 10.19716D).setScale(2, RoundingMode.HALF_UP);
        return roundedResult.doubleValue();
    }

    private Double getGasTemperatureAtTheBlowerOutlet(Double compressionRatio) {
        // Температура газа на выходе нагнетателя, K
        double resultGasTemperatureAtTheBlowerOutlet = inletGasTemperatureKelvin.getValue() *
                pow(compressionRatio, (POLYTROPIC_INDEX.getValue() - 1D) / POLYTROPIC_INDEX.getValue());
        log.info("Температура газа на выходе из нагнетателя (K): ResultGasTemperatureAtTheBlowerOutlet={}", String.format("%8.2f", resultGasTemperatureAtTheBlowerOutlet - 273.15D));
        var roundedResult = BigDecimal.valueOf(resultGasTemperatureAtTheBlowerOutlet - 273.15D).setScale(2, RoundingMode.HALF_UP);
        return roundedResult.doubleValue();
    }

    private Double getCompressionRatio() {
        // Политропическая работа сжатия Вт/кг мин
        double polytropicCompressionWork = (relativeTurns.getValue() * relativeTurns.getValue()) * ((REDUCED_RELATIVE_INTERNAL_POWER.getValue() * 60D * 1000D)
                / VOLUMETRIC_REDUCED_FLOW.getValue()) * POLYTROPIC_EFFICIENCY.getValue();
        log.info("PolytropicCompressionWork={}", String.format("%8.4f", polytropicCompressionWork));

        double er23 = 1D + ((POLYTROPIC_INDEX.getValue() - 1D) / POLYTROPIC_INDEX.getValue()) * (polytropicCompressionWork
                / (GAS_COMPRESSIBILITY_FACTOR.getValue() * inletGasTemperatureKelvin.getValue() * GAS_CONSTANT_OF_COMPRESSED_GAS.getValue()));
        double er22 = POLYTROPIC_INDEX.getValue() / (POLYTROPIC_INDEX.getValue() - 1D);
        log.info("er23={}", String.format("%8.4f", er23));
        log.info("er22={}", String.format("%8.4f", er22));

        // степень сжатия газа
        double result = pow(er23, er22);
        log.info("CompressionRatio={}", String.format("%8.4f", result));

        // погрешность compressionRatio - степень сжатия
        double compressionRatioFault = abs(NOMINAL_COMPRESSION_RATIO.getValue() - result) / NOMINAL_COMPRESSION_RATIO.getValue();
        log.info("CompressionRatioFault={}", String.format("%8.6f", compressionRatioFault));
        return result;
    }

    private ResponsiblePerson getResponsiblePerson(Long gpuId) {
        var gasPumpingUnit = gasPumpingUnitRepo.findGasPumpingUnitByGpuId(gpuId);
        if (gasPumpingUnit == null) throw new InternalServerError("GasPumpingUnit with gpuId={} is not exist!");
        var responsiblePerson = responsiblePersonRepo.findResponsiblePersonByResponsiblePersonId(gasPumpingUnit.getResponsiblePersonId());
        if (responsiblePerson == null)
            throw new InternalServerError("ResponsiblePerson with responsiblePersonId={} is not exist!");
        return responsiblePerson;
    }

    private void sendEmailWithAttachment(Long gpuId, ResponsiblePerson responsiblePerson, String fileContent, Locale locale) {
        try {
            var fileName = MessageFormat.format("{0}{1}{2}{3}", gpuId, responsiblePerson.getResponsiblePersonName(),
                    responsiblePerson.getResponsiblePersonSurname(), getFormattedLocalDateTime());
            emailService.sendEmailWithAttachment(responsiblePerson.getResponsiblePersonMail(), Messages.GENERAL_TEXT.i18n(messageSource, locale), fileContent, fileName);
        } catch (MessagingException | IOException e) {
            log.error("An error occurred while sending message via mail", e);
            throw new InternalServerError("An error occurred while sending message via mail");
        }
    }

    private String getFormattedLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        String formattedTime = now.format(formatter);
        log.info("Formatted LocalDateTime = {}", formattedTime);
        return formattedTime;
    }

    private void initializeParams() {
        log.info("Расчет нагнетателя ЦБН 280-12-4 (280-11-1(2))");

        // Давление наружного воздуха, МПа
        double outsideAirPressure = ((Pvyhod.getValue() + getExternalAirPressure()) * 0.0980665) / ((Pvhod.getValue() + getExternalAirPressure()) * 0.0980665);
        log.info("OutsideAirPressure={}", String.format("%8.2f", outsideAirPressure));

        // температура газа на входе, К
        inletGasTemperatureKelvin.setValue(inletGasTemperature.getValue() + 273.15);
        log.info("InletGasTemperature={}", String.format("%8.2f", inletGasTemperature.getValue()));
        log.info("InletGasTemperatureKelvin={}", String.format("%8.2f", inletGasTemperatureKelvin.getValue()));

        // относительные обороты
        relativeTurns.setValue(actualSpeed.getValue() / ntnd.getValue());
        log.info("RelativeTurns={}", String.format("%8.2f", relativeTurns.getValue()));

        // Молекулярная масса газа: кг/кмоль
        MOLECULAR_WEIGHT_OF_GAS.setValue(x_CH4.getValue() * M_CH4.getValue()
                + x_C2H6.getValue() * M_C2H6.getValue()
                + x_C3H8.getValue() * M_C3H8.getValue()
                + x_iC4H10.getValue() * M_iC4H10.getValue()
                + x_nC4H10.getValue() * M_nC4H10.getValue()
                + x_C5H12.getValue() * M_C5H12.getValue()
                + x_C5H121.getValue() * M_C5H121.getValue()
                + x_C6H14.getValue() * M_C6H14.getValue()
                + x_N2.getValue() * M_N2.getValue()
                + x_CO2.getValue() * M_CO2.getValue()
                + x_O2.getValue() * M_O2.getValue());

        // Критическая температура газа: К
        CRITICAL_GAS_TEMPERATURE.setValue(x_CH4.getValue() * T_CH4.getValue()
                + x_C2H6.getValue() * T_C2H6.getValue()
                + x_C3H8.getValue() * T_C3H8.getValue()
                + x_iC4H10.getValue() * T_iC4H10.getValue()
                + x_nC4H10.getValue() * T_nC4H10.getValue()
                + x_C5H12.getValue() * T_C5H12.getValue()
                + x_C5H121.getValue() * T_C5H121.getValue()
                + x_C6H14.getValue() * T_C6H14.getValue()
                + x_N2.getValue() * T_N2.getValue()
                + x_CO2.getValue() * T_CO2.getValue()
                + x_O2.getValue() * T_O2.getValue());

        // Критическое давление газа: МПа
        CRITICAL_GAS_PRESSURE.setValue(x_CH4.getValue() * P_CH4.getValue()
                + x_C2H6.getValue() * P_C2H6.getValue()
                + x_C3H8.getValue() * P_C3H8.getValue()
                + x_iC4H10.getValue() * P_iC4H10.getValue()
                + x_nC4H10.getValue() * P_nC4H10.getValue()
                + x_C5H12.getValue() * P_C5H12.getValue()
                + x_C5H121.getValue() * P_C5H121.getValue()
                + x_C6H14.getValue() * P_C6H14.getValue()
                + x_N2.getValue() * P_N2.getValue()
                + x_CO2.getValue() * P_CO2.getValue()
                + x_O2.getValue() * P_O2.getValue());

        log.info("MOLECULAR_WEIGHT_OF_GAS={}", String.format("%8.5f", MOLECULAR_WEIGHT_OF_GAS.getValue()));
        log.info("CRITICAL_GAS_TEMPERATURE={}", String.format("%8.2f", CRITICAL_GAS_TEMPERATURE.getValue()));
        log.info("CRITICAL_GAS_PRESSURE={}", String.format("%8.4f", CRITICAL_GAS_PRESSURE.getValue()));

        // Газовая постоянная компримируемого газа, (Дж/(кг*K))/(кг м^3)
        GAS_CONSTANT_OF_COMPRESSED_GAS.setValue(Rg.getValue() / MOLECULAR_WEIGHT_OF_GAS.getValue());
        log.info("GAS_CONSTANT_OF_COMPRESSED_GAS={}", String.format("%8.5f", GAS_CONSTANT_OF_COMPRESSED_GAS.getValue()));


//         ограничения относительных оборотов
//         частота вращения силового вала - ЧВСВ
//         0.75 * ЧВСВ < nf < 1.1 * ЧВСВ

        // Потери давления в пылеуловителях и входном шлейфе КЦ, кг/см^2
        LOSS_OF_PRESSURE_IN_DUST_COLLECTORS_AND_SUCTION_CIRCUIT.setValue(1.5);
        log.info("LOSS_OF_PRESSURE_IN_DUST_COLLECTORS_AND_SUCTION_CIRCUIT={}", String.format("%8.5f", LOSS_OF_PRESSURE_IN_DUST_COLLECTORS_AND_SUCTION_CIRCUIT.getValue()));

        // Производительность одного нагнетателя, млн.ст.м^3.сут
        PERFORMANCE_OF_ONE_BLOWER.setValue((Qkom.getValue() / 1000000D) / kn.getValue());
        log.info("PERFORMANCE_OF_ONE_BLOWER={}", String.format("%8.5f", PERFORMANCE_OF_ONE_BLOWER.getValue()));

        // Давление газа на входе в КЦ: МПа
        INLET_GAS_PRESSURE_AT_THE_COMPRESSOR.setValue(gasPressureAtTheCompressorInlet.getValue() * 0.0980665D);
        log.info("INLET_GAS_PRESSURE_AT_THE_COMPRESSOR={}", String.format("%8.5f", INLET_GAS_PRESSURE_AT_THE_COMPRESSOR.getValue()));


        // плотность газа при нормальных условиях: кг/м^3
        GAS_DENSITY_UNDER_NORMAL_CONDITIONS.setValue(MOLECULAR_WEIGHT_OF_GAS.getValue() / 22.4D);
        // плотность газа при стандартных условиях: кг/м^3
        gasDensityAtStandardConditions.setValue(GAS_DENSITY_UNDER_NORMAL_CONDITIONS.getValue() * (273.15D / 293.15D));
        log.info("GAS_DENSITY_UNDER_NORMAL_CONDITIONS={}", String.format("%8.5f", GAS_DENSITY_UNDER_NORMAL_CONDITIONS.getValue()));
        log.info("GasDensityAtStandardConditions={}", String.format("%8.5f", gasDensityAtStandardConditions.getValue()));


        // Приведенная температура газа, K/K
        REDUCED_GAS_TEMPERATURE.setValue(inletGasTemperatureKelvin.getValue() / CRITICAL_GAS_TEMPERATURE.getValue());
        // Приведенное давление газа, МПа/МПа
        REDUCED_GAS_PRESSURE.setValue(INLET_GAS_PRESSURE_AT_THE_COMPRESSOR.getValue() / CRITICAL_GAS_PRESSURE.getValue());
        log.info("REDUCED_GAS_TEMPERATURE={}", String.format("%8.2f", REDUCED_GAS_TEMPERATURE.getValue()));
        log.info("REDUCED_GAS_PRESSURE={}", String.format("%8.4f", REDUCED_GAS_PRESSURE.getValue()));


        // Коэффициент сжимаемости газа (Journal of Natural Gas Science & Engineering (2015))
        GAS_COMPRESSIBILITY_FACTOR.setValue(1D - 3.52D * REDUCED_GAS_PRESSURE.getValue() * exp(-2.260D * (REDUCED_GAS_TEMPERATURE.getValue())) + 0.274D
                * pow(REDUCED_GAS_PRESSURE.getValue(), 2) * exp(-1.878D * (REDUCED_GAS_TEMPERATURE.getValue())));
        log.info("GAS_COMPRESSIBILITY_FACTOR={}", String.format("%8.6f", GAS_COMPRESSIBILITY_FACTOR.getValue()));


        // Плотность газа на входе в нагнетатель (при всасывании): кг/м^3
        GAS_DENSITY_AT_THE_BLOWER_INLET_AT_SUCTION.setValue((INLET_GAS_PRESSURE_AT_THE_COMPRESSOR.getValue() * 1000000D) / (GAS_CONSTANT_OF_COMPRESSED_GAS.getValue()
                * inletGasTemperatureKelvin.getValue() * GAS_COMPRESSIBILITY_FACTOR.getValue()));
        log.info("GAS_DENSITY_AT_THE_BLOWER_INLET_AT_SUCTION={}", String.format("%10.5f", GAS_DENSITY_AT_THE_BLOWER_INLET_AT_SUCTION.getValue()));


        // Объемная производительность нагнетателя: м^3/мин
        VOLUMETRIC_PERFORMANCE_OF_THE_SUPERCHARGER.setValue(694.444D * (volumeFlowUnderOperatingConditions.getValue() * 24D / 1000000D)
                * (gasDensityAtStandardConditions.getValue() / GAS_DENSITY_AT_THE_BLOWER_INLET_AT_SUCTION.getValue()));
        double volumetricPerformanceOfTheSuperchargers = 11.574074D * (volumeFlowUnderOperatingConditions.getValue() * 24D / 1000000D)
                * (gasDensityAtStandardConditions.getValue() / GAS_DENSITY_AT_THE_BLOWER_INLET_AT_SUCTION.getValue());
        log.info("VOLUMETRIC_PERFORMANCE_OF_THE_SUPERCHARGER={}", String.format("%10.2f", VOLUMETRIC_PERFORMANCE_OF_THE_SUPERCHARGER.getValue()));
        log.info("VolumetricPerformanceOfTheSuperchargers={}", String.format("%10.2f", volumetricPerformanceOfTheSuperchargers));


        // объемная производительность группы нагнетателей: м^3/мин
        double volumetricEfficiencyOfTheBlowerGroup = kn.getValue() * 694.444D * (volumeFlowUnderOperatingConditions.getValue() * 24D / 1000000D)
                * (gasDensityAtStandardConditions.getValue() / GAS_DENSITY_AT_THE_BLOWER_INLET_AT_SUCTION.getValue());
        log.info("VolumetricEfficiencyOfTheBlowerGroup={}", String.format("%10.2f", volumetricEfficiencyOfTheBlowerGroup));


        // объемный приведенный расход:  м^3/мин
        VOLUMETRIC_REDUCED_FLOW.setValue(VOLUMETRIC_PERFORMANCE_OF_THE_SUPERCHARGER.getValue() * (1D / relativeTurns.getValue()));
        log.info("VOLUMETRIC_REDUCED_FLOW={}", String.format("%8.2f", VOLUMETRIC_REDUCED_FLOW.getValue()));


        // приведенная частота вращения ротора: м^3/мин
        REDUCED_ROTOR_SPEED.setValue(relativeTurns.getValue() * sqrt((0.91D * 490.3325D * 288D) / (GAS_COMPRESSIBILITY_FACTOR.getValue()
                * GAS_CONSTANT_OF_COMPRESSED_GAS.getValue() * inletGasTemperatureKelvin.getValue())));
        log.info("REDUCED_ROTOR_SPEED={}", String.format("%8.2f", REDUCED_ROTOR_SPEED.getValue()));


        // ===================================================================================
        // ===================================================================================
        // ===================================================================================


        // показатель адиабаты газа (формула Кобза)
        GAS_ADIABATIC_INDEX_KOBZ_FORMULA.setValue(1.556D * (1D + 0.074D * (x_N2.getValue()))
                - 0.00039D * inletGasTemperatureKelvin.getValue() * (1D - 0.68D * (x_N2.getValue()))
                - 0.208D * gasDensityAtStandardConditions.getValue() + pow((INLET_GAS_PRESSURE_AT_THE_COMPRESSOR.getValue() / inletGasTemperatureKelvin.getValue()), 1.43D)
                * (384D * (1D - (x_N2.getValue())) * pow((INLET_GAS_PRESSURE_AT_THE_COMPRESSOR.getValue() / inletGasTemperatureKelvin.getValue()), 0.8D)
                + 26.4D * (x_N2.getValue())));
        log.info("GAS_ADIABATIC_INDEX_KOBZ_FORMULA={}", String.format("%8.2f", GAS_ADIABATIC_INDEX_KOBZ_FORMULA.getValue()));


        akpd.setValue(1.6209766D);
        bkpd.setValue(-0.017953226D);
        ckpd.setValue(0.00011901836D);
        dkpd.setValue(-0.00000024283107D);

        // ηp = (P2/P1)^((k-1)/k) / (T2/T1 - 1)
        POLYTROPIC_EFFICIENCY.setValue(akpd.getValue() + bkpd.getValue() * VOLUMETRIC_REDUCED_FLOW.getValue()
                + ckpd.getValue() * pow(VOLUMETRIC_REDUCED_FLOW.getValue(), 2D) + dkpd.getValue() * pow(VOLUMETRIC_REDUCED_FLOW.getValue(), 3D));
        // Политропический КПД
        log.info("POLYTROPIC_EFFICIENCY={}", String.format("%8.2f", POLYTROPIC_EFFICIENCY.getValue()));


        an.setValue(32.573216D);
        bn.setValue(0.53990954D);
        cn.setValue(0.00025016392D);
        dn.setValue(-0.0000036700693D);
        REDUCED_RELATIVE_INTERNAL_POWER.setValue(an.getValue() + bn.getValue() * VOLUMETRIC_REDUCED_FLOW.getValue()
                + cn.getValue() * pow(VOLUMETRIC_REDUCED_FLOW.getValue(), 2D) + dn.getValue() * pow(VOLUMETRIC_REDUCED_FLOW.getValue(), 3D));
        // Приведенная относительная внутренняя мощность: кВт/(кг*м^3)
        log.info("REDUCED_RELATIVE_INTERNAL_POWER={}", String.format("%8.2f", REDUCED_RELATIVE_INTERNAL_POWER.getValue()));


        // Внутренняя мощность нагнетателя: кВт
        INTERNAL_BLOWER_POWER.setValue(pow(relativeTurns.getValue(), 3D) * GAS_DENSITY_AT_THE_BLOWER_INLET_AT_SUCTION.getValue() * REDUCED_RELATIVE_INTERNAL_POWER.getValue());
        log.info("INTERNAL_BLOWER_POWER={}", String.format("%8.2f", INTERNAL_BLOWER_POWER.getValue()));


        // показатель политропы
        POLYTROPIC_INDEX.setValue((POLYTROPIC_EFFICIENCY.getValue() * GAS_ADIABATIC_INDEX_KOBZ_FORMULA.getValue())
                / (POLYTROPIC_EFFICIENCY.getValue() * GAS_ADIABATIC_INDEX_KOBZ_FORMULA.getValue() - GAS_ADIABATIC_INDEX_KOBZ_FORMULA.getValue() + 1D));
        log.info("POLYTROPIC_INDEX={}", String.format("%8.2f", POLYTROPIC_INDEX.getValue()));


        // Мощность на муфте (Мощность потребляемая нагнетателем): кВт
        CLUTCH_POWER_CONSUMED_BY_SUPERCHARGER.setValue(INTERNAL_BLOWER_POWER.getValue() + 100D);
        if (CLUTCH_POWER_CONSUMED_BY_SUPERCHARGER.getValue() < Nnom.getValue()) {
            log.info("POWER CONDITIONS PERFORMED/УСЛОВИЯ ПОТРЕБЛЯЕМОЙ МОЩНОСТИ ВЫПОЛНЯЕТСЯ: {} < {} kVt",
                    String.format("%8.2f", CLUTCH_POWER_CONSUMED_BY_SUPERCHARGER.getValue()), String.format("%8.2f", Nnom.getValue()));
        }
        log.info("N={}", String.format("%8.2f", CLUTCH_POWER_CONSUMED_BY_SUPERCHARGER.getValue()));


        // низшая теплотворная способность газа, ккал/м^3
        double netCalorificValueOfGas = 33483.3D / 4.1868D;
        log.info("NetCalorificValueOfGas={}", String.format("%8.2f", netCalorificValueOfGas));


        // Степень сжатия, номинальная МПа/МПа
        NOMINAL_COMPRESSION_RATIO.setValue(((Pvyhodk.getValue() + getExternalAirPressure()) * 0.098066D)
                / ((gasPressureAtTheCompressorInlet.getValue() + getExternalAirPressure()) * 0.098066D));
        log.info("NOMINAL_COMPRESSION_RATIO={}", String.format("%8.2f", NOMINAL_COMPRESSION_RATIO.getValue()));


        // (int)(x + 0.5)
        // Температура газа на входе нагнетателя, K
        double tv1 = (double) ((int) (inletGasTemperatureKelvin.getValue() * 100.0 + 0.5) / 100);
        double T11 = (double) ((int) ((T2.getValue() / pow(NOMINAL_COMPRESSION_RATIO.getValue(), (POLYTROPIC_INDEX.getValue() - 1D)
                / POLYTROPIC_INDEX.getValue())) * 100D + 0.5D) / 100.0D);
        if (T11 == tv1) {
            log.info("Температура газа на входе в нагнетатель OK! {}", String.format("%8.0f", T11 - 273.15D));
        } else {
            System.out.printf("Температура газа на входе отличается - SOS!  %8.0f \n", T11 - 273.15D);
            TEMPERATURE_DIFFERENCE.setValue(T11 - 273.15D);
        }


        // коммерческая производительность ГПА, м^3/час
        double commercialPerformance = (VOLUMETRIC_PERFORMANCE_OF_THE_SUPERCHARGER.getValue() * 0.00144D * (GAS_DENSITY_AT_THE_BLOWER_INLET_AT_SUCTION.getValue()
                / gasDensityAtStandardConditions.getValue()) * 1000000D) / 24D;
        log.info("CommercialPerformance={}", String.format("%8.2f", commercialPerformance));
    }

    private RequestDto getFileData(MultipartFile file) {
        try {
            log.info("Input file contentType: {}", file.getContentType());
            String fileContents = new String(file.getBytes(), StandardCharsets.UTF_8);
            log.info("Input file content: {}", fileContents);
            return RequestDto.fromJson(fileContents);
        } catch (JsonProcessingException e) {
            log.error("An error occurred while reading input file", e);
            throw new InternalServerError("An error occurred while reading input file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<RequestDto> getMultipleFileData(MultipartFile file) {
        try {
            log.info("Input file contentType: {}", file.getContentType());
            String fileContents = new String(file.getBytes(), StandardCharsets.UTF_8);
            log.info("Input file content: {}", fileContents);
            return RequestDto.fromMultipleJson(fileContents);
        } catch (JsonProcessingException e) {
            log.error("An error occurred while reading input file", e);
            throw new InternalServerError("An error occurred while reading input file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RequestGasPumpingUnit buildRequestGasPumpingUnit(Long gpuId, LocalDateTime timeOfRequest) {
        RequestGasPumpingUnit requestGasPumpingUnit = new RequestGasPumpingUnit();
        requestGasPumpingUnit.setGpuId(gpuId);
        requestGasPumpingUnit.setIsTemperatureDifference(Boolean.FALSE);
        requestGasPumpingUnit.setIsPressureDifference(Boolean.FALSE);
        requestGasPumpingUnit.setTimeOfRequest(timeOfRequest);
        return requestGasPumpingUnit;
    }
}
