package com.his.mapper;

import com.his.domain.DmsNonDrug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DmsNonDrugMapper {
    List<DmsNonDrug> selectEnabledByTypeAndKeyword(@Param("type") Integer type,
                                                   @Param("keyword") String keyword);

    DmsNonDrug selectById(@Param("id") Long id);
}

