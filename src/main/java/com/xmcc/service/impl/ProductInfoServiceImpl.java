package com.xmcc.service.impl;

import com.xmcc.common.ResultEnums;
import com.xmcc.common.ResultResponse;
import com.xmcc.dto.ProductCategoryDto;
import com.xmcc.dto.ProductInfoDto;
import com.xmcc.entity.ProductInfo;
import com.xmcc.repository.ProductInfoRepository;
import com.xmcc.service.ProductCategoryService;
import com.xmcc.service.ProductInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class ProductInfoServiceImpl implements ProductInfoService {
    @Autowired
    private ProductCategoryService productCategoryService;
    @Autowired
    private ProductInfoRepository productInfoRepository;
    @Override
    public ResultResponse queryList() {
        ResultResponse<List<ProductCategoryDto>> resultResponse = productCategoryService.findAll();
        List<ProductCategoryDto> categoryDtoList = resultResponse.getData();
        if (CollectionUtils.isEmpty(categoryDtoList)){
            //如果分类列表为空 直接返回了
            return resultResponse;
        }
        //获得类目编号集合
        List<Integer> categoryTypeList = categoryDtoList.stream().map(
                productCategoryDto -> productCategoryDto.
                        getCategoryType()).collect(Collectors.toList());
        //根据typeList 查询商品列表  这里商品上下架可以用枚举 方便扩展
        List<ProductInfo> productInfoList=
                productInfoRepository.findByProductStatusAndCategoryTypeIn(
                        ResultEnums.PRODUCT_UP.getCode(),categoryTypeList);
        //多线程遍历 取出每个商品类目编号对应的 商品列表 设置进入类目中
        List<ProductCategoryDto> productCategoryDtos = categoryDtoList.parallelStream().map(productCategoryDto -> {
            productCategoryDto.setProductInfoDtoList(productInfoList.stream()
                    .filter(productInfo -> productInfo.getCategoryType() == productCategoryDto.getCategoryType()).map(productInfo ->
                            ProductInfoDto.build(productInfo)).collect(Collectors.toList()));
            return productCategoryDto;
        }).collect(Collectors.toList());
        return ResultResponse.success(productCategoryDtos);
    }
}
