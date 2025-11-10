package com.example.demo.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "invite_codes")
public class InviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invite_code_id_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    Organization organization;

    @OneToOne
    @JoinColumn(name = "role_id")
    Role assigningRole;

    public InviteCode() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Role getAssigningRole() {
        return assigningRole;
    }

    public void setAssigningRole(Role assigningRole) {
        this.assigningRole = assigningRole;
    }
    

    
}
