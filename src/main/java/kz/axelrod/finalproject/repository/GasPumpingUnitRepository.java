package kz.axelrod.finalproject.repository;

import kz.axelrod.finalproject.model.GasPumpingUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GasPumpingUnitRepository extends JpaRepository<GasPumpingUnit, Long> {

    GasPumpingUnit findGasPumpingUnitByGpuId(Long gpuId);

    @Query(
            nativeQuery = true,
            value = "select gpu.gpu_id, gpu.gpu_name, gpu.gpu_state, " +
                    "gpu.gpu_length, gpu.responsible_person_id " +
                    "from gas_pumping_unit gpu order by gpu.gpu_id"
    )
    List<GasPumpingUnit> findGasPumpingUnitByGpuLength(Long gpuLength);
}
