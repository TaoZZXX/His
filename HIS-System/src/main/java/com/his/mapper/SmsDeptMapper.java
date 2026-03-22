package com.his.mapper;

import com.his.domain.SmsDept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsDeptMapper {

    SmsDept selectDeptById(@Param("id") Long id);

    /** 挂号/业务下拉：仅启用 */
    List<SmsDept> selectAllDepts();

    List<SmsDept> selectByPage(
            @Param("code") String code,
            @Param("name") String name,
            @Param("catId") Integer catId,
            @Param("type") Integer type,
            @Param("offset") int offset,
            @Param("limit") int limit);

    long countByPage(
            @Param("code") String code,
            @Param("name") String name,
            @Param("catId") Integer catId,
            @Param("type") Integer type);

    int countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    int insert(SmsDept row);

    int updateById(SmsDept row);

    int deleteById(@Param("id") Long id);

    int deleteByIds(@Param("ids") List<Long> ids);

    int countStaffByDeptId(@Param("deptId") Long deptId);
}
