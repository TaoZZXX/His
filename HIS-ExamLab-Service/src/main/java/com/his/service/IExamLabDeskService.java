package com.his.service;

import com.his.domain.Result;
import com.his.vo.ExamLabItemRowVo;

import java.util.List;
import java.util.Map;

public interface IExamLabDeskService {
    Result<List<ExamLabItemRowVo>> listQueue(String token, String mode, boolean filterByStaffDept);
    Result<List<ExamLabItemRowVo>> listWorkbench(String token, String keyword, boolean filterByStaffDept, Integer limit);
    Result<Object> executeItem(Long itemId, String token);
    Result<Object> saveResult(Long itemId, String token, Map<String, Object> body);
}
