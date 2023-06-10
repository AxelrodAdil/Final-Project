package kz.axelrod.finalproject.repository;

import kz.axelrod.finalproject.model.RequestGasPumpingUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestGasPumpingUnitRepository extends JpaRepository<RequestGasPumpingUnit, Long> {

    @Query(
            nativeQuery = true,
            value = "select rg.request_id, rg.gpu_id, rg.status, " +
                    "rg.is_pressure_difference, rg.is_temperature_difference, " +
                    "rg.time_of_mail_message_report, rg.time_of_request, " +
                    "rg.temperature, rg.pressure, rg.commercial_performance " +
                    "from request_gas_pumping_unit rg order by rg.request_id DESC LIMIT 5"
    )
    List<RequestGasPumpingUnit> findLastRequestGasPumpingUnit();
}
