package com.example.trans_backend_file.manager.resolve.impl;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TxtResolveManager extends ResolveManager{

    @Override
    protected List<String> doResolve(InputStream inputStream) {

        // 将 InputStream 转换为字符串
        StringBuilder textBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                textBuilder.append(line).append(" ");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String text = textBuilder.toString();

        // 使用正则表达式拆分句子
        List<String> sentences = new ArrayList<>();
        // 正则表达式匹配句子，以 .?! 结尾
        Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE | Pattern.COMMENTS);
        // 正则表达式匹配句子，以 .?! 结尾
        Matcher matcher = re.matcher(text);
        while (matcher.find()) {
            sentences.add(matcher.group().trim());
        }
        return sentences;
    }

}
