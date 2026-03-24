package com.his.mapper;


import com.his.domain.SmsStaff;
import com.his.vo.StaffPageVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SmsStaffMapper {
    /**
     * 用户注册
     * @param smsStall 插入的员工
     * @return 条目
     */
    Integer insertSmsStaff(SmsStaff smsStall);

    /**
     * @param username 用户名
     * @return 该用户名对应的用户数量
     */
    Long selectSmsStaffCountByUsername(@Param("username") String username);

    /**
     * 通过用户名查询用户信息
     * @param username 用户名
     * @return 用户
     */
    SmsStaff selectSmsStaffByUsername(@Param("username") String username);

    // 分页查询总数（可根据科室/角色筛选）
    Long selectStaffCount(@Param("filterDeptId") Integer deptId, @Param("filterRoleId") Integer roleId);

    // 分页查询列表，参数为 offset 和 limit
    List<StaffPageVo> selectStaffByPage(@Param("filterDeptId") Integer deptId, @Param("filterRoleId") Integer roleId,
                                        @Param("offset") Integer offset, @Param("limit") Integer limit);

    // 新增：通过科室查询医生列表（不分页）
    List<StaffPageVo> selectStaffByDept(@Param("deptId") Long deptId);

    // 新增：通过 ID 列表查询医生（不分页）
    List<StaffPageVo> selectStaffByIds(@Param("ids") java.util.List<Long> ids);

    // 删除员工
    Integer deleteSmsStaffByUsername(@Param("username") String username);

    // 更新员工（传入 SmsStaff, 只更新非 null 字段）
    Integer updateSmsStaff(SmsStaff smsStaff);

    Integer selectDeptIdById(@Param("id") Long id);

    String selectStaffDisplayNameById(@Param("id") Long id);

}
