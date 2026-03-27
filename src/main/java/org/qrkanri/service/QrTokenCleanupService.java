package org.qrkanri.service;

import lombok.RequiredArgsConstructor;
import org.qrkanri.repository.QrTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QrTokenCleanupService {
    private final QrTokenRepository qrTokenRepository;

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void deleteExpiredTokens(){
        qrTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
