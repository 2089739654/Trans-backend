package com.example.trans_backend_file.manager.translate;

import java.util.List;

public interface TranslateService {


    List<String> translate(List<String> text, String from, String to);
}
