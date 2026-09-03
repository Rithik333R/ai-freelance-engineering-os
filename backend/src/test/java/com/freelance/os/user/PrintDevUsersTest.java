package com.freelance.os.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class PrintDevUsersTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void printAllUsers() {
        List<User> users = userRepository.findAll();
        System.out.println("\n=========================================================");
        System.out.println("            REGISTERED USERS (DEV DATABASE)              ");
        System.out.println("=========================================================");
        if (users.isEmpty()) {
            System.out.println("No users found in database. (Database is empty - Register a new user in the app frontend!)");
        } else {
            for (User u : users) {
                System.out.printf("ID: %s\nEmail: %s\nName: %s\nRole: %s\nCreated At: %s\n---------------------------------------------------------\n",
                        u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.getCreatedAt());
            }
        }
        System.out.println("=========================================================\n");
    }
}
