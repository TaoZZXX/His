package com.his.mapper;


import com.his.domain.SmsStaff;
import org.apache.ibatis.annotations.Param;

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

}
