package com.example.trans_backend_socket.processor;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

public class ProcessorChain {

    private Processor firstProcessor;

    private Processor lastProcessor;

    public void addProcessor(Processor processor) {
        if (firstProcessor == null) {
            firstProcessor = processor;
            lastProcessor = processor;
        } else {
            lastProcessor.setNext(processor);
            lastProcessor = processor;
        }
    }


    public void process(WebSocketSession webSocketSession) {
        if (firstProcessor != null) {
            firstProcessor.process(webSocketSession);
        }
    }


}
