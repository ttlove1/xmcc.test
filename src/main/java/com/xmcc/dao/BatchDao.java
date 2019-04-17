package com.xmcc.dao;

import java.util.List;

public interface BatchDao<T> {
    void batchInsert(List<T> list);
}
