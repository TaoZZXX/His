package com.his.mapper;

import com.his.domain.DmsDrug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DmsDrugMapper {
    int insert(DmsDrug record);
    int updateById(DmsDrug record);
    long countForAdmin(@Param("keyword") String keyword, @Param("status") Integer status);
    List<DmsDrug> selectPage(@Param("keyword") String keyword, @Param("status") Integer status, @Param("offset") int offset, @Param("limit") int limit);
    DmsDrug selectById(@Param("id") Long id);
}
