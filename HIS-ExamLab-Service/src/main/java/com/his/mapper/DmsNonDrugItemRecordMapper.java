package com.his.mapper;

import com.his.domain.DmsNonDrugItemRecord;
import com.his.vo.ExamLabItemRowVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DmsNonDrugItemRecordMapper {
    DmsNonDrugItemRecord selectById(@Param("id") Long id);
    List<ExamLabItemRowVo> selectExamLabQueue(@Param("mode") String mode, @Param("staffDeptId") Long staffDeptId);
    List<ExamLabItemRowVo> selectMedTechWorkbench(@Param("keyword") String keyword, @Param("staffDeptId") Long staffDeptId, @Param("limit") int limit);
    Integer updateExecuteById(@Param("id") Long id, @Param("staffId") Long staffId, @Param("executeTime") LocalDateTime executeTime);
    Integer updateResultById(@Param("id") Long id, @Param("logStaffId") Long logStaffId, @Param("logDatetime") LocalDateTime logDatetime,
                             @Param("checkResult") String checkResult, @Param("clinicalImpression") String clinicalImpression,
                             @Param("clinicalDiagnosis") String clinicalDiagnosis, @Param("resultImgUrlList") String resultImgUrlList);
}
