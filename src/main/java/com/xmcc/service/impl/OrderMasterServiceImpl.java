package com.xmcc.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xmcc.common.OrderEnums;
import com.xmcc.common.PayEnums;
import com.xmcc.common.ResultEnums;
import com.xmcc.common.ResultResponse;
import com.xmcc.dto.OrderDetailDto;
import com.xmcc.dto.OrderMasterDto;
import com.xmcc.entity.OrderDetail;
import com.xmcc.entity.OrderMaster;
import com.xmcc.entity.ProductInfo;
import com.xmcc.exception.CustomException;
import com.xmcc.repository.OrderMasterRepository;
import com.xmcc.service.OrderDetailService;
import com.xmcc.service.OrderMasterService;
import com.xmcc.service.ProductInfoService;
import com.xmcc.util.BigDecimalUtil;
import com.xmcc.util.IDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderMasterServiceImpl implements OrderMasterService {
    @Autowired
    private OrderMasterRepository orderMasterRepository;
    @Autowired
    private ProductInfoService productInfoService;
    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    public ResultResponse insertOrder(OrderMasterDto orderMasterDto) {
     //取出订单
        List<OrderDetailDto> items = orderMasterDto.getItems();
     //创建订单集合 将符合的放入其中 待会批量插入
        List<OrderDetail> orderDetailList = Lists.newArrayList();
        //创建订单总金额为0  涉及到钱的都用高精度计算
        BigDecimal totalPrice = new BigDecimal("0");
        //遍历订单项
        for (OrderDetailDto detailDto: items) {
            ResultResponse<ProductInfo>resultResponse =productInfoService.queryById(detailDto.getProductId());
            //如果该商品未查询到 生成订单失败，涉及到事务需要抛出异常，事务机制是遇到异常才会回滚
            if (resultResponse.getCode()== ResultEnums.FAIL.getCode()){
                throw new CustomException(resultResponse.getMsg());
            }
            //获得查询商品
            ProductInfo productInfo = resultResponse.getData();
            //如果库存不足，单生成失败 直接抛出异常 事务回滚
            if (productInfo.getProductStock()<detailDto.getProductQuantity()){
                throw new CustomException(ResultEnums.PRODUCT_NOT_ENOUGH.getMsg());
            }
            //将前台传入的订单项DTO与数据库查询到的 商品数据组装成OrderDetail 放入集合中构建
            OrderDetail orderDetail =
                    OrderDetail.builder().detailId(IDUtils.createIdbyUUID()).productIcon(productInfo.getProductIcon())
                    .productId(detailDto.getProductId()).productName(productInfo.getProductName())
                    .productPrice(productInfo.getProductPrice()).productQuantity(detailDto.getProductQuantity())
                    .build();
            orderDetailList.add(orderDetail);
            //减少商品库存
            productInfo.setProductStock(productInfo.getProductStock()-detailDto.getProductQuantity());
            productInfoService.updateProduct(productInfo);
            //计算价格
            totalPrice = BigDecimalUtil.add(totalPrice, BigDecimalUtil.multi(productInfo.getProductPrice(), detailDto.getProductQuantity()));
        }
            //生成订单
        String orderId = IDUtils.createIdbyUUID();
        //构建订单信息
        OrderMaster orderMaster =
                OrderMaster.builder().buyerAddress(orderMasterDto.getAddress()).buyerName(orderMasterDto.getName())
                .buyerOpenid(orderMasterDto.getOpenid()).orderStatus(OrderEnums.NEW.getCode())
                .payStatus(PayEnums.WAIT.getCode()).buyerPhone(orderMasterDto.getPhone())
                .orderId(orderId).orderAmount(totalPrice).build();
        //将生成的订单id设置到订单中
        orderDetailList.stream().map(orderDetail -> {
            orderDetail.setOrderId(orderId);
            return orderDetail;
        }).collect(Collectors.toList());
        //插入订单项
        orderDetailService.batchInsert(orderDetailList);
        //插入订单
        orderMasterRepository.save(orderMaster);
        HashMap<String,String> map = Maps.newHashMap();
        //按照前台要求的数据结构传入
        map.put("orderId",orderId );
        return ResultResponse.success(map);
    }
}
