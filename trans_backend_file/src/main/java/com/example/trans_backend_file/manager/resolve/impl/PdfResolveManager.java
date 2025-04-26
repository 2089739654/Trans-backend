package com.example.trans_backend_file.manager.resolve.impl;

import com.example.trans_backend_file.manager.ResolveManager;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfResolveManager extends ResolveManager {

    @Override
    protected List<String> doResolve(InputStream inputStream) {

        List<String> result=new ArrayList<>();
        try(PDDocument pdDocument=PDDocument.load(inputStream)){
            PDFTextStripper pdfTextStripper=new PDFTextStripper();
            String text = pdfTextStripper.getText(pdDocument);
            Pattern nonChinesePattern = Pattern.compile("[^\u4e00-\u9fa5]+");
            Matcher nonChineseMatcher = nonChinesePattern.matcher(text);
            StringBuilder nonChineseText = new StringBuilder();
            while (nonChineseMatcher.find()) {
                nonChineseText.append(nonChineseMatcher.group());
            }

            // 按指定分隔符划分
            String[] sentences = nonChineseText.toString().split("[。？！\\n\\r]+");

            // 过滤掉空字符串
            for (String sentence : sentences) {
                if (!sentence.trim().isEmpty()) {
                    result.add(sentence.trim());
                }
            }

        }catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;

    }
}
