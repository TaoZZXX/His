package com.his.mapper;

import com.his.domain.SmsStaff;
import com.his.vo.StaffPageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SmsStaffMapper {
    Integer insertSmsStaff(SmsStaff smsStaff);
    Long selectSmsStaffCountByUsername(@Param("username") String username);
    SmsStaff selectSmsStaffByUsername(@Param("username") String username);
    Long selectStaffCount(@Param("filterDeptId") Integer deptId, @Param("filterRoleId") Integer roleId);
    List<StaffPageVo> selectStaffByPage(@Param("filterDeptId") Integer deptId, @Param("filterRoleId") Integer roleId,
                                        @Param("offset") Integer offset, @Param("limit") Integer limit);
    Integer deleteSmsStaffByUsername(@Param("username") String username);
    Integer updateSmsStaff(SmsStaff smsStaff);
}
