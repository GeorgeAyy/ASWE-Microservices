package com.example.demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CartDTO;
import com.example.demo.dto.CartRequestDTO;
import com.example.demo.services.CartService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@RequestBody CartRequestDTO cartRequest) {
        logger.info("Request to add item with ID: {} to cart for user with ID: {}", cartRequest.getItemId(),
                cartRequest.getUserId());
        cartService.addToCart(cartRequest);
        logger.info("Item added to cart successfully");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/items/{userId}")
    public ResponseEntity<List<CartDTO>> getItemsInCart(@PathVariable Long userId) {
        logger.info("Request to fetch items in cart for user with ID: {}", userId);
        List<CartDTO> cartItems = cartService.getItemsInCart(userId);
        logger.info("Fetched {} items in cart for user with ID: {}", cartItems.size(), userId);
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateCartItemQuantity(@RequestBody CartRequestDTO cartRequest) {
        logger.info("Request to update cart item with ID: {} for user with ID: {} to quantity: {}",
                cartRequest.getItemId(), cartRequest.getUserId(), cartRequest.getQuantity());
        cartService.updateCartItemQuantity(cartRequest);
        logger.info("Cart item updated successfully");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/remove/{userId}/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Long userId, @PathVariable Long itemId) {
        logger.info("Request to remove item with ID: {} from cart for user with ID: {}", itemId, userId);
        cartService.removeItemFromCart(itemId, userId);
        logger.info("Item removed from cart successfully");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}