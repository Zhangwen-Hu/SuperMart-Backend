package com.example.supermartbackend.repository;

import com.example.supermartbackend.entity.Order;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public Order save(Order order) {
        if (order.getId() == null) {
            entityManager.persist(order);
            return order;
        } else {
            return entityManager.merge(order);
        }
    }
    
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Order.class, id));
    }
    
    public List<Order> findAllByUserId(Long userId) {
        // Using HQL
        String hql = "FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC";
        return entityManager.createQuery(hql, Order.class)
                .setParameter("userId", userId)
                .getResultList();
    }
    
    public List<Order> findAll() {
        // Using Criteria API
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> query = cb.createQuery(Order.class);
        Root<Order> root = query.from(Order.class);
        query.select(root).orderBy(cb.desc(root.get("createdAt")));
        
        return entityManager.createQuery(query).getResultList();
    }
} 