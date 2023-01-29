package com.dynatrace.cart.controller;

import com.dynatrace.cart.exception.BadRequestException;
import com.dynatrace.cart.exception.ResourceNotFoundException;
import com.dynatrace.cart.model.Book;
import com.dynatrace.cart.model.Cart;
import com.dynatrace.cart.model.Client;
import com.dynatrace.cart.repository.BookRepository;
import com.dynatrace.cart.repository.CartRepository;
import com.dynatrace.cart.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/")
public class CartController {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private BookRepository bookRepository;
    Logger logger = LoggerFactory.getLogger(CartController.class);

    // get all Carts
    @GetMapping("/carts")
    public List<Cart> getAllCarts() {
        return cartRepository.findAll(Sort.by(Sort.Direction.ASC, "email"));
    }

    // get Cart by ID
    @GetMapping("/carts/{id}")
    public Cart getCartById(@PathVariable Long id) {
        Optional<Cart> cart = cartRepository.findById(id);
        if (cart.isEmpty()) {
            throw new ResourceNotFoundException("Cart not found");
        }
        return cart.get();
    }

    // get Cart of a user
    @GetMapping("/carts/findByEmail")
    public List<Cart> getCartsByEmail(@RequestParam String email) {
        this.verifyClient(email);
        return cartRepository.findByEmail(email);
    }

    // get all users who have the book in their cart
    @GetMapping("/carts/findByISBN")
    public List<Cart> getCartsByISBN(@RequestParam String isbn) {
        this.verifyBook(isbn);
        return cartRepository.findByEmail(isbn);
    }

    // add a book to the cart
    @PostMapping("/carts")
    public Cart addToCart(@RequestBody Cart cart) {
        this.verifyClient(cart.getEmail());
        this.verifyBook(cart.getIsbn());
        Cart cartDB = cartRepository.findByEmailAndIsbn(cart.getEmail(), cart.getIsbn());
        if (cartDB == null) {
            return cartRepository.save(cart);
        }
        // cart already exists
        // increase quantity if found
        cartDB.setQuantity(cartDB.getQuantity() <= 0 ? cart.getQuantity() : cartDB.getQuantity() + cart.getQuantity());
        return cartRepository.save(cartDB);
    }

    // update a Cart
    @PutMapping("/carts")
    public Cart updateCart(@RequestBody Cart cart) {
        this.verifyClient(cart.getEmail());
        this.verifyBook(cart.getIsbn());
        Cart cartByEmailIsbn = cartRepository.findByEmailAndIsbn(cart.getEmail(), cart.getIsbn());

        if (null == cartByEmailIsbn) {
            throw new BadRequestException("You can change only quantity");
        }
        cart.setId(cartByEmailIsbn.getId());
        return cartRepository.save(cart);
    }

    // remove specified quantity
    @DeleteMapping("/carts")
    public Cart reduceCart(@RequestBody Cart cart) {
        this.verifyClient(cart.getEmail());
        this.verifyBook(cart.getIsbn());
        Cart cartByEmailIsbn = cartRepository.findByEmailAndIsbn(cart.getEmail(), cart.getIsbn());
        if (null == cartByEmailIsbn) {
            throw new ResourceNotFoundException("Cart not found");
        }
        if (cart.getQuantity() >= cartByEmailIsbn.getQuantity()) {
            // deleting the cart
            cartRepository.delete(cart);
            return null;
        }
        cartByEmailIsbn.setQuantity(cartByEmailIsbn.getQuantity() - cart.getQuantity());
        return cartRepository.save(cartByEmailIsbn);
    }

    // remove all carts
    @DeleteMapping("/carts/delete-all")
    public void deteteAllCarts() {
        cartRepository.deleteAll();
    }


    private void verifyClient(String email) {
        Client client = clientRepository.getClientByEmail(email);
        if (null == client) {
            throw new ResourceNotFoundException("Client is not found by email " + email);
        }
        Client[] clients = clientRepository.getAllClients();
        logger.debug(clients.toString());
    }

    private void verifyBook(String isbn) {
        Book book = bookRepository.getBookByISBN(isbn);
        if (null == book) {
            throw new ResourceNotFoundException("Book not found by isbn " + isbn);
        }
        Book[] books = bookRepository.getAllBooks();
        logger.debug(books.toString());
    }
}
