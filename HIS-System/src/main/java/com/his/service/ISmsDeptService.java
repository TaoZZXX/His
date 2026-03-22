package com.his.service;

import com.his.domain.PageResult;
import com.his.domain.SmsDept;

import java.util.List;

public interface ISmsDeptService {

    PageResult<SmsDept> page(int pageNo, int pageSize, String code, String name, Integer catId, Integer type);

    void create(SmsDept dept);

    void update(Long id, SmsDept dept);

    void delete(Long id);

    void batchDelete(List<Long> ids);
}
