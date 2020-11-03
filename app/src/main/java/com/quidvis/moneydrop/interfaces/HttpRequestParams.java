package com.quidvis.moneydrop.interfaces;

import java.util.Map;

public interface HttpRequestParams {
    Map<String, String> getParams();
    Map<String, String> getHeaders();
    byte[] getBody();
}