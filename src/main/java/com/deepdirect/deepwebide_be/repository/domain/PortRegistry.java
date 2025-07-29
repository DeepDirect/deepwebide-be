package com.deepdirect.deepwebide_be.repository.domain;

import jakarta.persistence.*;

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

    public void setStatus(PortStatus status) {
        this.status = status;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public Integer getPort() {
        return this.port;
    }
}

