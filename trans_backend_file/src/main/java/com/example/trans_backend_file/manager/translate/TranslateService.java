package com.example.trans_backend_file.manager.translate;

import java.util.List;

public interface TranslateService {

    String translateText(String text,String from,String to);

    List<String> translate(List<String> text, String from, String to);
}
