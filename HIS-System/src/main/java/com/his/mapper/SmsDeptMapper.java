package com.his.mapper;

import com.his.domain.SmsDept;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SmsDeptMapper {

    /**
     * 通过 id 查询科室信息
     * @param id
     * @return
     */
    public SmsDept selectDeptById(Long id);

    // 新增：查询所有科室
    public List<SmsDept> selectAllDepts();

}
