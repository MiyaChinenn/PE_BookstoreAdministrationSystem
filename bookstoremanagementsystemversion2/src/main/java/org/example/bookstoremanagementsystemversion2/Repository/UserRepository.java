package org.example.bookstoremanagementsystemversion2.Repository;

import java.util.Optional;

import org.example.bookstoremanagementsystemversion2.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndPassword(String username, String password);
}