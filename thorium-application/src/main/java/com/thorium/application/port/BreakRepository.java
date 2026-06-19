package com.thorium.application.port;

import com.thorium.domain.model.BreakPeriod;

import java.util.List;
import java.util.Optional;

public interface BreakRepository {

    BreakPeriod save(BreakPeriod breakPeriod);

    Optional<BreakPeriod> findById(Long id);

    List<BreakPeriod> findAll();

    void deleteById(Long id);
}
