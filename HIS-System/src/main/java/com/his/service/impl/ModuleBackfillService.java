package com.his.service.impl;

import com.his.enums.ResultCode;
import com.his.exception.BusinessException;
import com.his.mapper.ModuleBackfillMapper;
import com.his.service.IModuleBackfillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ModuleBackfillService implements IModuleBackfillService {

    private static final List<String> TABLES_FINANCE = Arrays.asList(
            "bms_bills_record",
            "bms_invoice_exp",
            "bms_operator_settle_record",
            "bms_settlement_cat"
    );

    private static final List<String> TABLES_DOCTOR_TEMPLATE = Arrays.asList(
            "dms_case_model",
            "dms_case_model_catalog",
            "dms_dise",
            "dms_dise_catalog"
    );

    private static final List<String> TABLES_PRESCRIPTION_TEMPLATE = Arrays.asList(
            "dms_dosage",
            "dms_drug_model",
            "dms_drug_refund_item_record",
            "dms_herbal_model_item",
            "dms_medicine_model_item",
            "dms_non_drug_model"
    );

    private static final List<String> TABLES_AUDIT = Arrays.asList(
            "sms_description",
            "sms_frequent_used",
            "sms_login_log",
            "sms_workload_record"
    );

    private static final Set<String> ALLOW_TABLES = new LinkedHashSet<>();

    static {
        ALLOW_TABLES.addAll(TABLES_FINANCE);
        ALLOW_TABLES.addAll(TABLES_DOCTOR_TEMPLATE);
        ALLOW_TABLES.addAll(TABLES_PRESCRIPTION_TEMPLATE);
        ALLOW_TABLES.addAll(TABLES_AUDIT);
    }

    @Autowired
    private ModuleBackfillMapper moduleBackfillMapper;

    @Override
    public List<Map<String, Object>> listModules() {
        List<Map<String, Object>> out = new ArrayList<>();
        out.add(module("doctor-workbench", "医生工作台扩展", concat(TABLES_DOCTOR_TEMPLATE, TABLES_PRESCRIPTION_TEMPLATE)));
        out.add(module("finance", "财务扩展", TABLES_FINANCE));
        out.add(module("audit-ops", "系统审计与运营", TABLES_AUDIT));
        return out;
    }

    private static List<String> concat(List<String> a, List<String> b) {
        List<String> out = new ArrayList<>(a);
        out.addAll(b);
        return out;
    }

    private static Map<String, Object> module(String key, String name, List<String> tables) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("key", key);
        m.put("name", name);
        m.put("tables", tables);
        return m;
    }

    private String normTable(String table) {
        String t = table == null ? "" : table.trim().toLowerCase();
        if (!ALLOW_TABLES.contains(t)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不允许访问该表: " + table);
        }
        return t;
    }

    @Override
    public List<Map<String, Object>> listTableColumns(String table) {
        String t = normTable(table);
        return moduleBackfillMapper.selectTableColumns(t);
    }

    @Override
    public Map<String, Object> pageRows(String table, Integer page, Integer size) {
        String t = normTable(table);
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 200);
        int offset = (p - 1) * s;

        Long total = moduleBackfillMapper.countRows(t);
        List<Map<String, Object>> rows = moduleBackfillMapper.selectPageRows(t, s, offset);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("records", rows);
        out.put("total", total == null ? 0L : total);
        out.put("page", p);
        out.put("size", s);
        return out;
    }

    @Override
    @Transactional
    public Map<String, Object> saveRow(String table, Map<String, Object> body) {
        String t = normTable(table);
        if (body == null || body.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请求体不能为空");
        }

        List<String> columns = listTableColumns(t).stream()
                .map(m -> String.valueOf(m.get("name")))
                .collect(Collectors.toList());

        Object idObj = body.get("id");
        Long id = parseId(idObj);
        List<String> writable = columns.stream()
                .filter(c -> !"id".equalsIgnoreCase(c))
                .filter(body::containsKey)
                .collect(Collectors.toList());
        if (writable.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "没有可保存的字段");
        }

        for (String key : writable) {
            body.put(key, normalizeValue(body.get(key)));
        }

        if (id == null) {
            moduleBackfillMapper.insertRow(t, writable, body);
            Long newId = moduleBackfillMapper.selectLastInsertId();
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("id", newId);
            return out;
        }

        int rows = moduleBackfillMapper.updateRow(t, id, writable, body);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "记录不存在");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", id);
        return out;
    }

    @Override
    @Transactional
    public void deleteRow(String table, Long id) {
        String t = normTable(table);
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "id 不能为空");
        }
        int rows = moduleBackfillMapper.deleteRow(t, id);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "记录不存在");
        }
    }

    private static Object normalizeValue(Object value) {
        if (value instanceof String) {
            String s = ((String) value).trim();
            return s.isEmpty() ? null : s;
        }
        return value;
    }

    private static Long parseId(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(String.valueOf(obj).trim());
        } catch (Exception e) {
            return null;
        }
    }
}
