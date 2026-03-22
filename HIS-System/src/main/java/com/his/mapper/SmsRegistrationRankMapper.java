package com.his.mapper;

import com.his.domain.SmsRegistrationRank;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsRegistrationRankMapper {

    SmsRegistrationRank selectById(@Param("id") Long id);

    List<SmsRegistrationRank> selectAllEnabled();
}
