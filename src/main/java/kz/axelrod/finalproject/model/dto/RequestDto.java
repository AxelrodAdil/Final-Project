package kz.axelrod.finalproject.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {

    @NotNull
    @Positive
    @ApiModelProperty(value = "ID of the GPU", example = "1")
    private Long gpuId;

    @NotNull
    @Positive
    @ApiModelProperty(value = "Actual speed value", example = "4900")
    private Long actualSpeed;

    @NotNull
    @Positive
    @JsonAlias("inputVolumeFlow")
    @ApiModelProperty(value = "Volume flow under operating conditions value in cubic meters per day", example = "393290")
    private Long inputVolumeFlowUnderOperatingConditions;

    @NotNull
    @ApiModelProperty(value = "Inlet gas temperature value in degrees Celsius", example = "4")
    private Double inputInletGasTemperature;

    @NotNull
    @Positive
    @ApiModelProperty(value = "Gas pressure value at the compressor inlet in kilopascals", example = "30.6")
    private Double inputGasPressureAtTheCompressorInlet;

    @NotNull
    @ApiModelProperty(value = "Gas density value at standard conditions in kilograms per cubic meter", example = "0")
    private Double inputGasDensityAtStandardConditions;

    @NotNull
    @Positive
    @ApiModelProperty(value = "External air pressure value in hectopascals", example = "737")
    private Double inputExternalAirPressure;

    @NotNull
    @ApiModelProperty(value = "Outdoor temperature value in Celsius", example = "3")
    private Double inputOutdoorTemperature;

    @NotNull
    @JsonAlias("lang")
    @ApiModelProperty(value = "Language", example = "kk")
    private String language;

    @JsonCreator
    public static RequestDto fromJson(String fileContents) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(fileContents, RequestDto.class);
    }

    @JsonCreator
    public static List<RequestDto> fromMultipleJson(String fileContents) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<RequestDto>> typeRef = new TypeReference<>() {
        };
        return mapper.readValue(fileContents, typeRef);
    }
}
