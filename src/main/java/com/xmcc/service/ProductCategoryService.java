package com.xmcc.service;


import com.xmcc.common.ResultResponse;
import com.xmcc.dto.ProductCategoryDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductCategoryService {
    ResultResponse<List<ProductCategoryDto>> findAll();
}
