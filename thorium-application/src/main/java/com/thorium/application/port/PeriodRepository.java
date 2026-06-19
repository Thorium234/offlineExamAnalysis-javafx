package com.thorium.application.port;

import com.thorium.domain.model.Period;

import java.util.List;
import java.util.Optional;

public interface PeriodRepository {

    Period save(Period period);

    Optional<Period> findById(Long id);

    List<Period> findAll();

    void deleteById(Long id);

    int count();
}
