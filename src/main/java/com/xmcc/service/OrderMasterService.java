package com.xmcc.service;

import com.xmcc.common.ResultResponse;
import com.xmcc.dto.OrderMasterDto;

public interface OrderMasterService {
    ResultResponse insertOrder(OrderMasterDto orderMasterDto);
}
