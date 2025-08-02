package com.deepdirect.deepwebide_be.repository.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "port_registry")
public class PortRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer port;

    @Enumerated(EnumType.STRING)
    private PortStatus status;

    @OneToOne
    private Repository repository;

    public PortRegistry() {} // JPA 기본 생성자

    public void assignToRepository(Repository repository) {
        this.status = PortStatus.IN_USE;
        this.repository = repository;
    }

    public void release() {
        this.status = PortStatus.AVAILABLE;
        this.repository = null;
    }

}
