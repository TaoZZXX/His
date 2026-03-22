package com.his.service;

import com.his.domain.BmsDailySettlement;

import java.time.LocalDateTime;
import java.util.List;

public interface IBmsDailySettlementService {

    BmsDailySettlement generate(String token, LocalDateTime rangeStart, LocalDateTime rangeEnd);

    List<BmsDailySettlement> listByReportRange(String token, LocalDateTime queryStart, LocalDateTime queryEnd);

    BmsDailySettlement getById(String token, Long id);

    void audit(String token, Long settlementId);
}
