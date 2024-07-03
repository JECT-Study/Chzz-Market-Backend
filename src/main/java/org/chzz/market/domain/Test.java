package org.chzz.market.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "test")
public class Test {
    @Id
    @GeneratedValue
    @Column(name="test"+"_id")
    private Long id;
    @Column(name = "val")
    private String value;
}
