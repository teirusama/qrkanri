package org.qrkanri.service;

import lombok.RequiredArgsConstructor;
import org.qrkanri.entity.QrToken;
import org.qrkanri.repository.QrTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrTokenService {
    private final QrTokenRepository qrTokenRepository;

    public QrToken generateToken(){
        String tokenValue = UUID.randomUUID().toString();
        QrToken qrToken = new QrToken();
        qrToken.setToken(tokenValue);
        qrToken.setCreatedAt(LocalDateTime.now());
        qrToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        return qrTokenRepository.save(qrToken);
    }
}
