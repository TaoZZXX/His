package com.his.controller;

import com.his.domain.Result;
import com.his.service.IExamLabDeskService;
import com.his.vo.ExamLabItemRowVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 检查检验：登记执行、结果录入
 */
@RestController
@RequestMapping("/exam-lab")
public class ExamLabDeskController {

    @Autowired
    private IExamLabDeskService examLabDeskService;

    /**
     * @param mode                execute=待执行队列；result=已执行（结果录入）
     * @param filterByStaffDept   1=按当前登录人科室与项目字典执行科室匹配；0=全院
     */
    @GetMapping("/queue")
    public Result<List<ExamLabItemRowVo>> queue(
            @RequestParam("token") String token,
            @RequestParam String mode,
            /** 默认不过滤：字典执行科室与登录人科室常不一致，易导致列表为空 */
            @RequestParam(value = "filterByStaffDept", defaultValue = "0") int filterByStaffDept) {
        return examLabDeskService.listQueue(token, mode, filterByStaffDept == 1);
    }

    /**
     * 门诊医技工作台列表（未登记 + 已登记），keyword 匹配姓名/病历号/项目名称
     */
    @GetMapping("/workbench")
    public Result<List<ExamLabItemRowVo>> workbench(
            @RequestParam("token") String token,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "filterByStaffDept", defaultValue = "0") int filterByStaffDept,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return examLabDeskService.listWorkbench(token, keyword, filterByStaffDept == 1, limit);
    }

    @PostMapping("/items/{id}/execute")
    public Result<Object> execute(@PathVariable("id") Long id, @RequestParam("token") String token) {
        return examLabDeskService.executeItem(id, token);
    }

    @PutMapping("/items/{id}/result")
    public Result<Object> saveResult(
            @PathVariable("id") Long id,
            @RequestParam("token") String token,
            @RequestBody(required = false) Map<String, Object> body) {
        return examLabDeskService.saveResult(id, token, body);
    }
}
