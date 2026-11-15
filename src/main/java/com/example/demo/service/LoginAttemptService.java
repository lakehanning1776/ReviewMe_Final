package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/** Basic per-IP-and-username throttling for the application's custom login flow. */
@Service
public class LoginAttemptService {
    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCKOUT = Duration.ofMinutes(15);
    private final ConcurrentHashMap<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        AttemptState state = attempts.get(key);
        if (state == null) return false;
        synchronized (state) {
            Instant now = Instant.now();
            if (state.lockedUntil != null && now.isBefore(state.lockedUntil)) return true;
            if (state.lockedUntil != null && !now.isBefore(state.lockedUntil)) {
                state.failures.clear();
                state.lockedUntil = null;
            }
            prune(state, now);
            return false;
        }
    }

    public void recordFailure(String key) {
        AttemptState state = attempts.computeIfAbsent(key, ignored -> new AttemptState());
        synchronized (state) {
            Instant now = Instant.now();
            prune(state, now);
            state.failures.addLast(now);
            if (state.failures.size() >= MAX_FAILURES) state.lockedUntil = now.plus(LOCKOUT);
        }
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    private void prune(AttemptState state, Instant now) {
        while (!state.failures.isEmpty() && state.failures.peekFirst().plus(WINDOW).isBefore(now)) {
            state.failures.removeFirst();
        }
    }

    private static final class AttemptState {
        private final Deque<Instant> failures = new ArrayDeque<>();
        private Instant lockedUntil;
    }
}
