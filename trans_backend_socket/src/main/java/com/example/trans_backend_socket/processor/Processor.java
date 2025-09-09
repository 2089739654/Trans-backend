package com.example.trans_backend_socket.processor;

import org.springframework.web.socket.WebSocketSession;

public abstract class Processor {

    private Processor next;

    public final void process(WebSocketSession webSocketSession){
        doProcess(webSocketSession);
        if (next != null) {
            next.process(webSocketSession);
        }
    }

    protected abstract void doProcess(WebSocketSession webSocketSession);


    public void setNext(Processor next) {
        this.next = next;
    }

}
