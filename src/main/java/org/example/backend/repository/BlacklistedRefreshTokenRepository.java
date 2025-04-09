package org.example.backend.repository;

import org.example.backend.model.BlacklistedRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.Optional;

@Repository
public interface BlacklistedRefreshTokenRepository extends JpaRepository<BlacklistedRefreshToken, Long> {
    Optional<BlacklistedRefreshToken> findByRefreshToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM BlacklistedRefreshToken t WHERE t.expiryDate < :expiryDate")
    void deleteExpiredTokens(@Param("expiryDate") Date dateOfExpirationUpUntilToWhichToDelete);
}
