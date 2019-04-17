package com.xmcc.service;

import com.xmcc.common.ResultResponse;
import com.xmcc.entity.ProductInfo;
import org.springframework.stereotype.Service;

@Service
public interface ProductInfoService {
    ResultResponse queryList();
    //根据id查询商品
    ResultResponse<ProductInfo> queryById(String productId);
    //修改商品库存的方法
    void updateProduct(ProductInfo productInfo);
}
