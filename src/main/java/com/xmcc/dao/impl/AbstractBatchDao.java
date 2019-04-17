package com.xmcc.dao.impl;

import com.xmcc.dao.BatchDao;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class AbstractBatchDao<T> implements BatchDao<T> {
    @PersistenceContext
    private EntityManager em;
    @Override
    @Transactional
    public void batchInsert(List<T> list) {
        for (int i = 0; i <list.size() ; i++) {
            em.persist(list.get(i));
            //每100条写入数据库，不足100全部写入
            if (i%100==0 || i==(list.size()-1)){
                em.flush();
                em.clear();
            }
        }
    }
}
