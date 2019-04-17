package com.xmcc.service;

import com.xmcc.common.ResultResponse;
import org.springframework.stereotype.Service;

@Service
public interface ProductInfoService {
    ResultResponse queryList();
}
