package kz.axelrod.finalproject.repository;

import kz.axelrod.finalproject.model.ResponsiblePerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponsiblePersonRepository extends JpaRepository<ResponsiblePerson, Long> {

    ResponsiblePerson findResponsiblePersonByResponsiblePersonId(Long responsiblePersonId);
}
