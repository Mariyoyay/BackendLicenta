package org.example.backend.repository;

import org.example.backend.model.RefreshToken;
import org.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken t WHERE t.expiryDate < :expiryDate")
    void deleteExpiredTokens(@Param("expiryDate") Date dateOfExpirationUpUntilToWhichToDelete);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken t SET t.isBlacklisted = true WHERE t.user = :user")
    void invalidateAllByUser(@Param("user") User user);
}
