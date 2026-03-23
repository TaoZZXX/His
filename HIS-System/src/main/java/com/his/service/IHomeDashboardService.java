package com.his.service;

import com.his.domain.Result;
import com.his.vo.HomeDashboardVo;

public interface IHomeDashboardService {

    Result<HomeDashboardVo> loadDashboard(String token);
}
