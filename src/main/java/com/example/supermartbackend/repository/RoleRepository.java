package com.example.supermartbackend.repository;

import com.example.supermartbackend.entity.Role;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Repository
public class RoleRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public Optional<Role> findByName(Role.ERole name) {
        String hql = "FROM Role r WHERE r.name = :name";
        try {
            return Optional.ofNullable(
                    entityManager.createQuery(hql, Role.class)
                            .setParameter("name", name)
                            .getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public Role save(Role role) {
        if (role.getId() == null) {
            entityManager.persist(role);
            return role;
        } else {
            return entityManager.merge(role);
        }
    }
} 