package com.his.controller;

import com.his.domain.Result;
import com.his.service.IExamLabDeskService;
import com.his.vo.ExamLabItemRowVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exam-lab")
public class ExamLabDeskController {
    private final IExamLabDeskService examLabDeskService;
    public ExamLabDeskController(IExamLabDeskService examLabDeskService) { this.examLabDeskService = examLabDeskService; }

    @GetMapping("/queue")
    public Result<List<ExamLabItemRowVo>> queue(@RequestParam("token") String token, @RequestParam String mode,
                                                @RequestParam(value = "filterByStaffDept", defaultValue = "0") int filterByStaffDept) {
        return examLabDeskService.listQueue(token, mode, filterByStaffDept == 1);
    }

    @GetMapping("/workbench")
    public Result<List<ExamLabItemRowVo>> workbench(@RequestParam("token") String token,
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
    public Result<Object> saveResult(@PathVariable("id") Long id, @RequestParam("token") String token,
                                     @RequestBody(required = false) Map<String, Object> body) {
        return examLabDeskService.saveResult(id, token, body);
    }
}
