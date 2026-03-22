package com.his.mapper;

import com.his.domain.BmsPayableItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BmsPayableItemMapper {

    int insert(BmsPayableItem row);

    BmsPayableItem selectById(@Param("id") Long id);

    List<BmsPayableItem> selectByRegistrationId(@Param("registrationId") Long registrationId);

    BmsPayableItem selectByRegistrationTypeSource(
            @Param("registrationId") Long registrationId,
            @Param("itemType") int itemType,
            @Param("sourceId") Long sourceId);

    int countUnpaidByRegistrationId(@Param("registrationId") Long registrationId);

    int updatePaidProgress(
            @Param("id") Long id,
            @Param("paidAmount") java.math.BigDecimal paidAmount,
            @Param("status") int status);
}
