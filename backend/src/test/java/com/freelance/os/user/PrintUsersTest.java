package com.freelance.os.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class PrintUsersTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void printAllUsers() {
        List<User> users = userRepository.findAll();
        System.out.println("\n=========================================================");
        System.out.println("               REGISTERED USERS DATABASE LIST            ");
        System.out.println("=========================================================");
        if (users.isEmpty()) {
            System.out.println("No users found in database.");
        } else {
            for (User u : users) {
                System.out.printf("ID: %s | Email: %s | Name: %s | Role: %s\n",
                        u.getId(), u.getEmail(), u.getFullName(), u.getRole());
            }
        }
        System.out.println("=========================================================\n");
    }
}
