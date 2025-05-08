package com.example.supermartbackend.repository;

import com.example.supermartbackend.entity.Product;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<Product> findAll() {
        // Using HQL to get all products including out-of-stock
        String hql = "FROM Product p ORDER BY p.id";
        return entityManager.createQuery(hql, Product.class).getResultList();
    }
    
    public List<Product> findAllInStock() {
        // Using HQL
        String hql = "FROM Product p WHERE p.quantity > 0";
        return entityManager.createQuery(hql, Product.class).getResultList();
    }
    
    public Optional<Product> findById(Long id) {
        // Using Criteria API
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);
        query.select(root).where(cb.equal(root.get("id"), id));
        
        try {
            return Optional.ofNullable(entityManager.createQuery(query).getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public Product save(Product product) {
        if (product.getId() == null) {
            entityManager.persist(product);
            return product;
        } else {
            return entityManager.merge(product);
        }
    }
    
    public List<Product> findTopXByProfit(int count) {
        // Profit = (retailPrice - wholesalePrice) * quantity
        String hql = "SELECT p FROM Product p " +
                "ORDER BY (p.retailPrice - p.wholesalePrice) DESC";
        return entityManager.createQuery(hql, Product.class)
                .setMaxResults(count)
                .getResultList();
    }
    
    public List<Product> findTopXByPopularity(int count) {
        // Using JPQL instead of Criteria API for clearer join semantics
        String jpql = "SELECT p, COUNT(oi) as orderCount " +
                "FROM Product p " +
                "LEFT JOIN p.orderItems oi " +
                "GROUP BY p.id " +
                "ORDER BY orderCount DESC";
        
        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                .setMaxResults(count)
                .getResultList();
        
        // Extract just the products from the results
        return results.stream()
                .map(result -> (Product) result[0])
                .collect(Collectors.toList());
    }
    
    public void delete(Product product) {
        entityManager.remove(product);
    }
    
    public List<Product> findMostRecentlyPurchased(Long userId, int count) {
        // HQL query to find most recently purchased products by user
        String hql = "SELECT p FROM Product p " +
                "JOIN p.orderItems oi " +
                "JOIN oi.order o " +
                "WHERE o.user.id = :userId " +
                "ORDER BY o.createdAt DESC";
        
        return entityManager.createQuery(hql, Product.class)
                .setParameter("userId", userId)
                .setMaxResults(count)
                .getResultList();
    }
    
    public List<Product> findMostFrequentlyPurchased(Long userId, int count) {
        // Using Criteria API
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> product = query.from(Product.class);
        
        // Join with OrderItem and Order
        Join<Object, Object> orderItems = product.join("orderItems");
        Join<Object, Object> order = orderItems.join("order");
        
        // Where user id matches
        query.where(cb.equal(order.get("user").get("id"), userId));
        
        // Group by product and order by count
        query.select(product);
        query.groupBy(product.get("id"));
        query.orderBy(cb.desc(cb.count(orderItems)));
        
        return entityManager.createQuery(query)
                .setMaxResults(count)
                .getResultList();
    }
} 