package com.dynatrace.cart.repository;

import com.dynatrace.cart.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByEmail(String email);
    List<Cart> findByIsbn(String isbn);

    Cart findByEmailAndIsbn(String email, String isbn);
}
