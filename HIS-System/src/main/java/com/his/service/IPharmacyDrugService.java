package com.his.service;

import com.his.domain.DmsDrug;
import com.his.domain.Result;

import java.util.Map;

public interface IPharmacyDrugService {

    Result<Map<String, Object>> page(String token, int page, int size, String keyword, Integer status);

    Result<Object> create(String token, DmsDrug drug);

    Result<Object> update(String token, Long id, DmsDrug drug);
}
