package kz.axelrod.finalproject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_gas_pumping_unit")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestGasPumpingUnit {

    @Id
    @SequenceGenerator(name = "request_gas_pumping_unit_seq", sequenceName = "request_gas_pumping_unit_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_gas_pumping_unit_seq")
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "gpu_id")
    private Long gpuId;

    @Column(name = "status")
    private String status;

    @Column(name = "is_pressure_difference")
    private Boolean isPressureDifference;

    @Column(name = "is_temperature_difference")
    private Boolean isTemperatureDifference;

    @Column(name = "time_of_mail_message_report")
    private LocalDateTime timeOfMailMessageReport;

    @Column(name = "time_of_request")
    private LocalDateTime timeOfRequest;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "pressure")
    private Double pressure;

    @Column(name = "commercial_performance")
    private Double commercialPerformance;
}
