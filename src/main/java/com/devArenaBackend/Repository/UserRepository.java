package com.devArenaBackend.Repository;

import com.devArenaBackend.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository

public interface UserRepository extends JpaRepository<User,Long> {
   boolean existsByEmail(String email);
   Optional<User> findByEmail(String email);
}
