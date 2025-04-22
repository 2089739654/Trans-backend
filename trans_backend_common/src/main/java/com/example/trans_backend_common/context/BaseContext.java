package com.example.trans_backend_common.context;

import com.example.trans_backend_common.entity.User;
import org.springframework.stereotype.Component;

@Component
public class BaseContext {
    private static final ThreadLocal<User> threadLocal = new ThreadLocal<>();

    public static void setUser(User user) {
        threadLocal.set(user);
    }

    public static User getUser() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }

}
