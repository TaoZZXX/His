package com.his.mapper;

import com.his.domain.BmsPayableItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BmsPayableItemMapper {
    int insert(BmsPayableItem row);
    BmsPayableItem selectByRegistrationTypeSource(@Param("registrationId") Long registrationId, @Param("itemType") int itemType, @Param("sourceId") Long sourceId);
    Integer countPaidRegistrationFee(@Param("registrationId") Long registrationId);
}
