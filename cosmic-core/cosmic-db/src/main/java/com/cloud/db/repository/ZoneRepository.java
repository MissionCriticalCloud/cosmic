package com.cloud.db.repository;

import com.cloud.db.model.Zone;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneRepository extends CrudRepository<Zone, Long> {

    Zone findByName(String name);

    List<Zone> findByRemovedIsNull();
}
