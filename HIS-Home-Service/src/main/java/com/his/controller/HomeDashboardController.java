package com.his.controller;

import com.his.domain.Result;
import com.his.service.IHomeDashboardService;
import com.his.vo.HomeDashboardVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomeDashboardController {

    private final IHomeDashboardService homeDashboardService;

    public HomeDashboardController(IHomeDashboardService homeDashboardService) {
        this.homeDashboardService = homeDashboardService;
    }

    @GetMapping("/dashboard")
    public Result<HomeDashboardVo> dashboard(@RequestParam("token") String token) {
        return homeDashboardService.loadDashboard(token);
    }
}
