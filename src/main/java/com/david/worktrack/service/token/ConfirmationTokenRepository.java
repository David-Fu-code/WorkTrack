package com.david.worktrack.service.token;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

    Optional<ConfirmationToken> findByToken(String token);

    // Automatically marks a token as confirmed ONLY if it has not been confirmed yet
    @Modifying
    @Transactional
    @Query("""
        UPDATE ConfirmationToken c
        SET c.confirmedAt = :now
        WHERE c.token = :token AND c.confirmedAt IS NULL
    """)
    int confirmToken(@Param("token") String token,
                     @Param("now") LocalDateTime now);
}
