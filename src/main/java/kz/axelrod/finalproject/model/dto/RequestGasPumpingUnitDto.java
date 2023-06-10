package kz.axelrod.finalproject.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestGasPumpingUnitDto {

    private Long requestId;
    private Double certainValue;
}
