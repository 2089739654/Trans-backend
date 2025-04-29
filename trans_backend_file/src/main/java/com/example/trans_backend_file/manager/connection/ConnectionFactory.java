package com.example.trans_backend_file.manager.connection;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

public interface ConnectionFactory {
    HttpClient getHttpClient();


}
