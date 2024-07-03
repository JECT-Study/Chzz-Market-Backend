package org.chzz.market.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<Test,Long>, TestRepositoryCustom {
}
