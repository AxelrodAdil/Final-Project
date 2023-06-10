package kz.axelrod.finalproject.controller;

import kz.axelrod.finalproject.exception.BadRequestException;
import kz.axelrod.finalproject.model.dto.ApiResponse;
import kz.axelrod.finalproject.exception.UnauthorizedException;
import kz.axelrod.finalproject.model.dto.RequestDto;
import kz.axelrod.finalproject.service.SolveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiController {

    private final SolveService solveService;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleError(Exception e) {
        log.error("Caught unhandled exception", e);
        String message = e.getMessage();
        int status = e instanceof UnauthorizedException ? 401 : e instanceof BadRequestException ? 400 : 500;
        return ResponseEntity.status(status).body(ApiResponse.fail(message));
    }

    @CrossOrigin(origins = "*")
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<ApiResponse<?>> getResultUsingPost(@RequestPart("file") MultipartFile file) {
        var response = solveService.getResultDto(file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @CrossOrigin(origins = "*")
    @PostMapping(
            path = "/several",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<ApiResponse<?>> getSeveralResultUsingPost(@RequestPart("file") MultipartFile file) {
        var response = solveService.getSeveralResultDto(file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/dto")
    public ResponseEntity<ApiResponse<?>> getResultUsingDtoPost(@RequestBody RequestDto requestDto) {
        var response = solveService.getResultDto(requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<?>> getResultUsingGet(@RequestParam("gasPumpingUnitId") Long gasPumpingUnitId,
                                                            @RequestParam("actualSpeed") Long actualSpeed,
                                                            @RequestParam("volumeFlowUnderOperatingConditions") Long volumeFlowUnderOperatingConditions,
                                                            @RequestParam("inletGasTemperature") Double inletGasTemperature,
                                                            @RequestParam("gasPressureAtTheCompressorInlet") Double gasPressureAtTheCompressorInlet,
                                                            @RequestParam("gasDensityAtStandardConditions") Double gasDensityAtStandardConditions,
                                                            @RequestParam("externalAirPressure") Double externalAirPressure,
                                                            @RequestParam("outdoorTemperature") Double outdoorTemperature,
                                                            @RequestParam("language") String language) {
        var response = solveService.getResultDto(gasPumpingUnitId, actualSpeed, volumeFlowUnderOperatingConditions, inletGasTemperature,
                gasPressureAtTheCompressorInlet, gasDensityAtStandardConditions, externalAirPressure, outdoorTemperature, language);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/results")
    public ResponseEntity<ApiResponse<?>> getLastResultUsingGet(@RequestParam("label") String labelName) {
        var response = solveService.getLastResultUsingGet(labelName);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
