package kz.axelrod.finalproject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gas_pumping_unit")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GasPumpingUnit {

    @Id
    @SequenceGenerator(name = "gas_pumping_unit_seq", sequenceName = "gas_pumping_unit_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gas_pumping_unit_seq")
    @Column(name = "gpu_id")
    private Long gpuId;

    @Column(name = "gpu_name")
    private String gpuName;

    @Column(name = "gpu_state")
    private String gpuState;

    @Column(name = "gpu_length")
    private Long gpuLength;

    @Column(name = "responsible_person_id")
    private Long responsiblePersonId;
}
