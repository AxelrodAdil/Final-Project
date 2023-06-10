package kz.axelrod.finalproject.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public enum Messages {

    GENERAL_ERROR("general.error"),
    GENERAL_TEXT("general.text"),
    GENERAL_MESSAGE("general.message"),
    GENERAL_SAKE("general.sake"),


    GENERAL_VOLUME("general.volume"),
    GENERAL_INLET_TEMPERATURE("general.inlet.temperature"),
    GENERAL_INLET_PRESSURE("general.inlet.pressure"),
    GENERAL_DENSITY("general.density"),
    GENERAL_EXTERNAL("general.external"),
    GENERAL_OUTDOOR("general.outdoor"),


    OUTPUT_PRESSURE("output.pressure"),
    OUTPUT_TEMPERATURE("output.temperature"),
    OUTPUT_COMPRESSION("output.compression"),
    OUTPUT_RPM("output.rpm"),
    OUTPUT_SURGE("output.surge"),
    OUTPUT_ROUNDED_SURGE("output.rounded.surge"),
    OUTPUT_POWER("output.power"),
    OUTPUT_RESERVE("output.reserve"),
    OUTPUT_LOAD("output.load"),
    OUTPUT_FUEL("output.fuel"),
    OUTPUT_ELECTRICITY("output.electricity"),
    OUTPUT_COMMERCIAL("output.commercial");

    private final String label;

    Messages(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String i18n(MessageSource messageSource, Locale locale) {
        return messageSource.getMessage(this.label, null, locale);
    }

    public String i18n(MessageSource messageSource) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(this.label, null, currentLocale);
    }
}
