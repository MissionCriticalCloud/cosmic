package com.cloud.db.repository;

import com.cloud.db.model.LoadBalancerRule;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoadBalancerRuleRepository extends CrudRepository<LoadBalancerRule, Long> {

    List<LoadBalancerRule> findByRemovedIsNull();
}
