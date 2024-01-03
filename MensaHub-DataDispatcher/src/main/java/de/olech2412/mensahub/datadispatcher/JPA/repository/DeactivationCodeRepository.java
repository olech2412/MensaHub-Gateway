package de.olech2412.mensahub.datadispatcher.JPA.repository;

import de.olech2412.mensahub.models.DeactivationCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeactivationCodeRepository extends CrudRepository<DeactivationCode, Long> {

    List<DeactivationCode> findByCode(String code);
}