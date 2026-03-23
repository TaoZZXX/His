package com.his.mapper;

import com.his.domain.DmsNonDrugItemRecord;
import com.his.vo.ExamLabItemRowVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DmsNonDrugItemRecordMapper {
    Integer insert(DmsNonDrugItemRecord record);

    DmsNonDrugItemRecord selectById(@Param("id") Long id);

    List<DmsNonDrugItemRecord> selectByRegistrationIdAndType(@Param("registrationId") Long registrationId,
                                                             @Param("type") Integer type);

    BigDecimal sumAmountByRegistrationId(@Param("registrationId") Long registrationId);

    /**
     * @param mode execute=待执行队列；result=已执行待录入/查看结果
     */
    List<ExamLabItemRowVo> selectExamLabQueue(@Param("mode") String mode,
                                              @Param("staffDeptId") Long staffDeptId);

    /**
     * 门诊医技工作台：全部未作废检查/检验单，支持姓名/病历号模糊
     */
    List<ExamLabItemRowVo> selectMedTechWorkbench(@Param("keyword") String keyword,
                                                    @Param("staffDeptId") Long staffDeptId,
                                                    @Param("limit") int limit);

    Integer updateExecuteById(@Param("id") Long id,
                              @Param("staffId") Long staffId,
                              @Param("executeTime") LocalDateTime executeTime);

    Integer updateResultById(@Param("id") Long id,
                             @Param("logStaffId") Long logStaffId,
                             @Param("logDatetime") LocalDateTime logDatetime,
                             @Param("checkResult") String checkResult,
                             @Param("clinicalImpression") String clinicalImpression,
                             @Param("clinicalDiagnosis") String clinicalDiagnosis,
                             @Param("resultImgUrlList") String resultImgUrlList);
}

