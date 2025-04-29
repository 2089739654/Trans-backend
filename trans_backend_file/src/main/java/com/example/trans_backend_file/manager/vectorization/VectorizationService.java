package com.example.trans_backend_file.manager.vectorization;

import java.io.IOException;

public interface VectorizationService {


    Float[] vectorize(String text) throws IOException;
}
