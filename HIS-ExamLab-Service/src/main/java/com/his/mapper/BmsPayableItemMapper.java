package com.his.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BmsPayableItemMapper {
    Integer selectPayableStatus(@Param("registrationId") Long registrationId,
                                @Param("itemType") int itemType,
                                @Param("sourceId") Long sourceId);
}
