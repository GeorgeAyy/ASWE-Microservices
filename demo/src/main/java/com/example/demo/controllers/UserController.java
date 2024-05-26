package com.example.demo.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.UserDTO;
import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/User")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public ResponseEntity<List<User>> getUsers() {
        logger.info("Fetching all users");
        List<User> users = userRepository.findAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("/Registration")
    public ResponseEntity<User> createUser(@RequestBody UserDTO body) {
        logger.info("Creating user with email: {}", body.getEmail());
        User myUser = new User(body.getUserLname(), body.getEmail(),
                BCrypt.hashpw(body.getUserPassword(), BCrypt.gensalt(12)),
                body.getUserFname(), body.getUserAddress(), false);
        userRepository.save(myUser);
        return new ResponseEntity<>(myUser, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserDetails(@PathVariable Long id) {
        logger.info("Fetching user details for id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody UserDTO body) {
        logger.info("Updating user with id: {}", id);
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userToUpdate.setUser_fname(body.getUserFname());
        userToUpdate.setUser_Lname(body.getUserLname());
        userToUpdate.setEmail(body.getEmail());
        userToUpdate.setUser_address(body.getUserAddress());
        userRepository.save(userToUpdate);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/toggle-admin")
    public ResponseEntity<Void> toggleAdmin(@PathVariable Long id) {
        logger.info("Toggling admin status for user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setUser_isAdmin(!user.isUser_isAdmin());
        userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        logger.info("Checking if email exists: {}", email);
        boolean exists = userRepository.existsByEmail(email);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Boolean> loginUser(@RequestParam String email, @RequestParam String password,
            HttpSession session) {
        logger.info("Logging in user with email: {}", email);
        User user = userRepository.findByEmail(email);
        if (user != null && BCrypt.checkpw(password, user.getUserPassword())) {
            session.setAttribute("user", user);
            logger.info("User logged in successfully: {}", email);
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        logger.warn("User login failed: {}", email);
        return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/by-email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        logger.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
}
