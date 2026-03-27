package org.qrkanri.repository;

import org.qrkanri.entity.QrToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface QrTokenRepository extends JpaRepository<QrToken, Long> {
    Optional<QrToken> findByToken(String token);
    void deleteByExpiresAtBefore(LocalDateTime time);
}
