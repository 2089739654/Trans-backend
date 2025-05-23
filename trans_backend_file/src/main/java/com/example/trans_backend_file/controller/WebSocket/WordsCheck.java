package com.example.trans_backend_file.controller.WebSocket;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.trans_backend_common.context.BaseContext;
import com.example.trans_backend_file.config.SpringBeanServerEndpointConfigurator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.hash.BloomFilter;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * WebSocket服务
 */
@Component
@ServerEndpoint(value = "/ws/{sid}", configurator = SpringBeanServerEndpointConfigurator.class)
public class WordsCheck {
    @Resource
     RedissonClient redisson;

    //存放会话对象
    private  final static Map<String, Session> sessionMap = new ConcurrentHashMap<String, Session>();
    private static RBloomFilter<String> words;
   @PostConstruct
   private void loadWords2Filter(){
       words=redisson.getBloomFilter("words");
       boolean b = words.tryInit(400000L, 0.033);
       if(b){
           URL resource = this.getClass().getClassLoader().getResource("words.txt");
           List<String> strings = FileUtil.readUtf8Lines(Objects.requireNonNull(resource));
           for (String string : strings) {
               boolean add = words.add(string);
           }
       }

   }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println(this.hashCode());
        System.out.println("客户端：" + sid + "建立连接");
        sessionMap.put(sid, session);

    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid ) {
        JSONArray array = JSONObject.parseArray(message);
        ArrayList<String> errors = new ArrayList<>();
        if (array != null) {
            for (Object obj : array) {
                if (obj instanceof String) {
                    String word = ((String) obj).trim().toLowerCase(); // 与词典加载时同样的处理
                    if (!word.isEmpty() && !words.contains(word)) {
                        errors.add((String) obj); // 返回原始大小写的错误单词
                    }
                }
            }
        }
        String jsonString = JSONObject.toJSONString(errors);
        sendToAllClient(jsonString);//发送消息
        System.out.println("收到来自客户端：" + sid + "的信息:" + message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开:" + sid);
        sessionMap.remove(sid);
    }

    /**
     * 群发
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
