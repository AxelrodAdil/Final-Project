package kz.axelrod.finalproject.model.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ResultDto implements Serializable {

    private Double volumeFlowUnderOperatingConditions;
    private Double inletGasTemperature;
    private Double gasPressureAtTheCompressorInlet;
    private Double gasDensityAtStandardConditions;
    private Double externalAirPressure;
    private Double outdoorTemperature;
    private Double gasPressureAtTheBlowerOutlet;
    private Double gasTemperatureAtTheBlowerOutlet;
    private Double compressionRatio;
    private Double requiredRevolutionDesiredRotorSpeed;
    private Double coefficientOfDistanceFromTheSurgeZone;
    private Double clutchPower;
    private Double availableDrivePower;
    private Double reserveDrivePower;
    private Double driveLoadFactor;
    private Double fuelGasConsumption;
    private Double electricityConsumption;
    private Double commercialPerformance;
}
