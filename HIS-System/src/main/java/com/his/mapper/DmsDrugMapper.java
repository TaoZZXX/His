package com.his.mapper;

import com.his.domain.DmsDrug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DmsDrugMapper {
    List<DmsDrug> selectEnabledByKeyword(@Param("keyword") String keyword);

    DmsDrug selectById(@Param("id") Long id);

    int insert(DmsDrug record);

    int updateById(DmsDrug record);

    long countForAdmin(@Param("keyword") String keyword, @Param("status") Integer status);

    List<DmsDrug> selectPage(@Param("keyword") String keyword,
                             @Param("status") Integer status,
                             @Param("offset") int offset,
                             @Param("limit") int limit);
}

