package com.his.service;

import java.util.List;
import java.util.Map;

public interface IModuleBackfillService {

    List<Map<String, Object>> listModules();

    List<Map<String, Object>> listTableColumns(String table);

    Map<String, Object> pageRows(String table, Integer page, Integer size);

    Map<String, Object> saveRow(String table, Map<String, Object> body);

    void deleteRow(String table, Long id);
}
