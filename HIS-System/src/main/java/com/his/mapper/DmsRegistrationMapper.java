package com.his.mapper;

import com.his.domain.DmsRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DmsRegistrationMapper {

    Integer insertDmsRegistration(DmsRegistration dmsRegistration);

    /**
     * 分页查询挂号记录，按挂号时间倒序，返回 VO
     */
    List<com.his.vo.RegistrationPageVo> selectByPage(@Param("offset") Integer offset,
                                                     @Param("limit") Integer limit);

    /**
     * 统计总记录数
     */
    Long countAll();

    /**
     * 根据主键更新挂号的部分字段（科室、就诊日期等）
     */
    Integer updateById(DmsRegistration dmsRegistration);

}
