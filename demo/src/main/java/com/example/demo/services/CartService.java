package com.example.demo.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CartDTO;
import com.example.demo.dto.CartRequestDTO;
import com.example.demo.models.Cart;
import com.example.demo.models.Item;
import com.example.demo.models.User;
import com.example.demo.repositories.CartRepository;
import com.example.demo.repositories.ItemImagesRepository;
import com.example.demo.repositories.ItemRepository;
import com.example.demo.repositories.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemImagesRepository imagesRepository;

    public void addToCart(CartRequestDTO cartRequest) {
        Item item = this.itemRepository.findByItemId(cartRequest.getItemId());
        User user = this.userRepository.findById(cartRequest.getUserId()).orElse(null);

        if (user != null && item != null) {
            // Check if item quantity is greater than 0
            if (item.getItemQuantity() > 0) {
                // Reduce item quantity by 1
                // item.setItemQuantity(item.getItemQuantity() - cartRequest.getQuantity());
                this.itemRepository.save(item);

                // Check if the item is already in the user's cart
                Cart existingCartItem = this.cartRepository.findByUserAndItem(user, item);
                if (existingCartItem != null) {
                    // Increment quantity in the cart
                    existingCartItem.setQuantity(existingCartItem.getQuantity() + cartRequest.getQuantity());
                    this.cartRepository.save(existingCartItem);
                } else {
                    // Add item to cart
                    Cart cart = new Cart();
                    cart.setItem(item);
                    cart.setUser(user);
                    cart.setQuantity(cartRequest.getQuantity());
                    this.cartRepository.save(cart);
                }
                logger.info("Item with ID: {} added to cart for user with ID: {}", cartRequest.getItemId(),
                        cartRequest.getUserId());
            } else {
                throw new IllegalArgumentException("Item is out of stock");
            }
        } else {
            throw new IllegalArgumentException("User or item not found");
        }
    }

    public List<CartDTO> getItemsInCart(Long userId) {
        User user = this.userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.warn("User with ID: {} not found.", userId);
            return new ArrayList<>();
        }

        List<Cart> items = this.cartRepository.findByUser(user);
        List<CartDTO> cartItemDTOs = new ArrayList<>();

        for (Iterator<Cart> iterator = items.iterator(); iterator.hasNext();) {
            Cart cart = iterator.next();

            // Fetch the item using itemId
            Long itemId = cart.getItem().getItemId();
            Item item = this.itemRepository.findById(itemId).orElse(null);

            if (item == null) {
                logger.warn("Item with ID: {} not found. Removing from cart.", itemId);
                this.cartRepository.delete(cart);
                iterator.remove();
                continue;
            }

            int availableQuantity = item.getItemQuantity();

            // If the cart quantity exceeds the available quantity, remove the item from the
            // cart
            if (cart.getQuantity() > availableQuantity) {
                this.cartRepository.delete(cart);
                logger.info("Removed item with ID: {} from cart for user with ID: {} due to insufficient quantity.",
                        itemId, userId);
                iterator.remove();
                continue;
            }

            CartDTO cartItemDTO = new CartDTO();
            cartItemDTO.setItemId(item.getItemId());
            cartItemDTO.setItemName(item.getItemTitle());
            cartItemDTO.setItemPrice(item.getItemPrice());
            cartItemDTO.setQuantity(cart.getQuantity());

            // Fetch and add image paths
            List<String> imagePaths = this.imagesRepository.findImagePathsByItemId(item.getItemId());
            cartItemDTO.setImages(imagePaths);

            cartItemDTOs.add(cartItemDTO);
        }

        logger.info("Fetched {} items in cart for user with ID: {}", cartItemDTOs.size(), userId);
        return cartItemDTOs;
    }

    public void updateCartItemQuantity(CartRequestDTO cartRequest) {
        Item item = this.itemRepository.findByItemId(cartRequest.getItemId());
        User user = this.userRepository.findById(cartRequest.getUserId()).orElse(null);

        Cart cartItem = cartRepository.findByUserAndItem(user, item);

        if (cartItem != null) {
            // Calculate the difference in quantity
            int quantityDifference = cartRequest.getQuantity() - cartItem.getQuantity();

            // Update quantity in cart
            cartItem.setQuantity(cartRequest.getQuantity());
            cartRepository.save(cartItem);

            // Update quantity in item table
            // item.setItemQuantity(item.getItemQuantity() - quantityDifference);
            itemRepository.save(item);

            logger.info("Updated cart item with ID: {} for user with ID: {} to quantity: {}", cartRequest.getItemId(),
                    cartRequest.getUserId(), cartRequest.getQuantity());
        }
    }

    public void removeItemFromCart(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));
        User user = userRepository.findById(userId).orElse(null);
        Cart cartItem = cartRepository.findByUserAndItem(user, item);

        if (cartItem != null) {
            cartRepository.delete(cartItem);
            // Optionally adjust the item stock quantity if needed
            // item.setItemQuantity(item.getItemQuantity() + cartItem.getQuantity());
            itemRepository.save(item);
            logger.info("Removed item with ID: {} from cart for user with ID: {}", itemId, userId);
        }
    }
}