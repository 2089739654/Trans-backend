package com.example.trans_backend_file.manager.resolve.impl;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TxtResolveManager extends ResolveManager{

    private static final Set<String> ABBREVIATIONS = new HashSet<>(Arrays.asList(
            "mr", "mrs", "dr", "ms", "prof", "jr", "sr", "st", "vs", "no", "ex"
    ));

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

        List<String> sentences = splitSentences(text);

        return sentences;
    }

    public static List<String> splitSentences(String text) {
        List<String> sentences = new ArrayList<>();
        if (text == null || text.isEmpty()) return sentences;

        // 步骤1：匹配潜在的句子结束符（排除小数点）
        Pattern potentialSplitPattern = Pattern.compile(
                "(?<=\\D)" +        // 前面不能是数字（排除小数点）
                        "(?<=[.!?])" +          // 匹配 .!? 作为句子结束符
                        "(?!\\d)" +             // 后面不能是数字（排除小数点）
                        "(?=\\s+[A-Za-z]|\\s+\\d|\"+\\s+[A-Za-z]|$)"       // 后面是空格+大写字母或空格+数字或双引号+空格 + 字母或字符串结尾
        );
        Matcher potentialMatcher = potentialSplitPattern.matcher(text);

        // 记录所有潜在的分割点
        List<Integer> splitIndices = new ArrayList<>();
        while (potentialMatcher.find()) {
            splitIndices.add(potentialMatcher.start(0));
        }

        // 步骤2：根据分割点拆分原始文本
        int lastEnd = 0;
        for (int index : splitIndices) {
            String candidate = text.substring(lastEnd, index).trim();
            if (!candidate.isEmpty()) {
                sentences.add(candidate);
            }
            lastEnd = index;
        }
        // 添加剩余文本
        String remaining = text.substring(lastEnd).trim();
        if (!remaining.isEmpty()) {
            sentences.add(remaining);
        }

        // 步骤3：后处理验证（排除缩写词）
        List<String> validated = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            if (endsWithAbbreviation(sentence) || sentence.length() <= 2) {
                // 如果当前句子以缩写结尾或长度小于等于2，合并到下一句
                if (i < sentences.size() - 1) {
                    sentences.set(i + 1, sentence + " " + sentences.get(i + 1));
                }
            } else {
                validated.add(sentence);
            }
        }

        return validated;
    }

    private static boolean endsWithAbbreviation(String sentence) {
        if (sentence.isEmpty()) return false;
        char lastChar = sentence.charAt(sentence.length() - 1);
        if (lastChar != '.' && lastChar != '!' && lastChar != '?') return false;

        // 提取最后一个符号前的单词
        Matcher wordMatcher = Pattern.compile("\\b([A-Za-z]+)\\b(?=[.!?]$)").matcher(sentence.trim());
        return wordMatcher.find() && ABBREVIATIONS.contains(wordMatcher.group(1).replace(".", "").toLowerCase());
    }



}
