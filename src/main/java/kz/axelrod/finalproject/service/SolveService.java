package kz.axelrod.finalproject.service;

import kz.axelrod.finalproject.model.dto.RequestDto;
import kz.axelrod.finalproject.model.dto.RequestGasPumpingUnitDto;
import kz.axelrod.finalproject.model.dto.ResultDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SolveService {

    ResultDto getResultDto(Long gpuId, Long actualSpeed, Long inputVolumeFlowUnderOperatingConditions,
                           Double inputInletGasTemperature, Double inputGasPressureAtTheCompressorInlet,
                           Double inputGasDensityAtStandardConditions, Double inputExternalAirPressure,
                           Double inputOutdoorTemperature, String language);

    ResultDto getResultDto(RequestDto requestDto);

    List<ResultDto> getSeveralResultDto(MultipartFile file);

    ResultDto getResultDto(MultipartFile file);

    List<RequestGasPumpingUnitDto> getLastResultUsingGet(String labelName);
}
