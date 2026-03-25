package org.qrkanri.repository;

import org.qrkanri.entity.QrToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QrTokenRepository extends JpaRepository<QrToken, Long> {
    Optional<QrToken> findByToken(String token);
}
