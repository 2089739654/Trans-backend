package com.example.trans_backend_socket.context;

import com.example.trans_backend_common.entity.User;
import lombok.Data;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class WebSocketSessionContext {

    //可改为set todo
    private static final ConcurrentHashMap<Long, List<SessionHolder>> sessionMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String,SessionHolder> sessionIdMap = new ConcurrentHashMap<>();

    public static void addSession(Long groupId, WebSocketSession webSocketSession, User user) {
        LockManager.writeLock(groupId);
        List<SessionHolder> list = sessionMap.getOrDefault(groupId, new ArrayList<>());
        list.add(new SessionHolder(webSocketSession, user));
        sessionMap.put(groupId,list);
        LockManager.writeUnlock(groupId);
        sessionIdMap.put(webSocketSession.getId(), new SessionHolder(webSocketSession, user));
    }

    public static List<WebSocketSession> getSession(Long groupId,WebSocketSession webSocketSession) {
        LockManager.readLock(groupId);
        List<SessionHolder> list = sessionMap.get(groupId);
        LockManager.readUnlock(groupId);
        List<WebSocketSession> collect = list.stream().map(SessionHolder::getWebSocketSession).filter(m -> m != webSocketSession).collect(Collectors.toList());
        return collect;
    }


    public static void removeSession(Long groupId, WebSocketSession webSocketSession) {
        LockManager.writeLock(groupId);
        List<SessionHolder> list = sessionMap.get(groupId);
        if (list != null) {
            list.removeIf(sessionHolder -> sessionHolder.getWebSocketSession().equals(webSocketSession));
            if (list.isEmpty()) {
                sessionMap.remove(groupId);
                LockManager.removeLock(groupId);
            } else {
                sessionMap.put(groupId, list);
            }
        }
        LockManager.writeUnlock(groupId);
        sessionIdMap.remove(webSocketSession.getId());
    }

    public static User getUser(WebSocketSession webSocketSession) {
        SessionHolder sessionHolder = sessionIdMap.get(webSocketSession.getId());
        if (sessionHolder != null) {
            return sessionHolder.getUser();
        }
        return null;
    }


    @Data
    private static class SessionHolder {
        private WebSocketSession webSocketSession;
        private User user;

        public SessionHolder(WebSocketSession webSocketSession, User user) {
            this.webSocketSession = webSocketSession;
            this.user = user;
        }

    }


    private static class LockManager {
        private static final ConcurrentHashMap<Long, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();
        public static ReentrantReadWriteLock getLock(Long groupId) {
            return lockMap.computeIfAbsent(groupId, k -> new ReentrantReadWriteLock());
        }

        public static void removeLock(Long groupId) {
            lockMap.remove(groupId);
        }

        public static void writeLock(Long groupId) {
            getLock(groupId).writeLock().lock();
        }

        public static void writeUnlock(Long groupId) {
            getLock(groupId).writeLock().unlock();
        }
        public static void readLock(Long groupId) {
            getLock(groupId).readLock().lock();
        }
        public static void readUnlock(Long groupId) {
            getLock(groupId).readLock().unlock();
        }


    }




}
