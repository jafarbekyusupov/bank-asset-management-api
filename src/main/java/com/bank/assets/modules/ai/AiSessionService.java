package com.bank.assets.modules.ai;

import com.bank.assets.common.exception.AppException;
import com.bank.assets.common.exception.ErrorCode;
import com.bank.assets.modules.ai.dto.MessageResponse;
import com.bank.assets.modules.ai.dto.SessionResponse;
import com.bank.assets.modules.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiSessionService {
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    @Transactional
    public SessionResponse createSession(User user) {
        ChatSession session = ChatSession.builder()
            .user(user)
            .build();
        return SessionResponse.from(sessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public Page<SessionResponse> listSessions(User user, Pageable pageable) {
        return sessionRepository
            .findByUserIdOrderByUpdatedAtDesc(user.getId(), pageable)
            .map(SessionResponse::from);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(UUID sessionId, User user) {
        sessionRepository
            .findByIdAndUserId(sessionId, user.getId())
            .orElseThrow(() -> AppException.notFound(ErrorCode.SESSION_NOT_FOUND));

        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
            .stream()
            .map(MessageResponse::from)
            .toList();
    }
}
