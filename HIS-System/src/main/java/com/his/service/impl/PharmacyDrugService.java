package com.his.service.impl;

import com.his.domain.DmsDrug;
import com.his.domain.Result;
import com.his.enums.ResultCode;
import com.his.mapper.DmsDrugMapper;
import com.his.service.IPharmacyDrugService;
import com.his.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PharmacyDrugService implements IPharmacyDrugService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private DmsDrugMapper dmsDrugMapper;

    private Long requireStaffId(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    @Override
    public Result<Map<String, Object>> page(String token, int page, int size, String keyword, Integer status) {
        if (requireStaffId(token) == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        int p = page < 1 ? 1 : page;
        int sz = size < 1 ? 10 : Math.min(size, 100);
        String kw = keyword == null ? null : keyword.trim();
        if (kw != null && kw.isEmpty()) {
            kw = null;
        }
        long total = dmsDrugMapper.countForAdmin(kw, status);
        int offset = (p - 1) * sz;
        List<DmsDrug> records = dmsDrugMapper.selectPage(kw, status, offset, sz);
        Map<String, Object> data = new HashMap<>(8);
        data.put("records", records);
        data.put("total", total);
        data.put("page", p);
        data.put("size", sz);
        return Result.success("查询成功", data);
    }

    @Override
    @Transactional
    public Result<Object> create(String token, DmsDrug drug) {
        if (requireStaffId(token) == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        if (drug == null || drug.getName() == null || drug.getName().trim().isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR, "药品名称不能为空");
        }
        normalizeDrug(drug, true);
        int n = dmsDrugMapper.insert(drug);
        if (n <= 0) {
            return Result.error(ResultCode.SERVER_ERROR, "新增失败");
        }
        return Result.success("新增成功", drug.getId());
    }

    @Override
    @Transactional
    public Result<Object> update(String token, Long id, DmsDrug drug) {
        if (requireStaffId(token) == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        if (id == null) {
            return Result.error(ResultCode.PARAM_ERROR, "药品ID不能为空");
        }
        DmsDrug old = dmsDrugMapper.selectById(id);
        if (old == null) {
            return Result.error(ResultCode.PARAM_ERROR, "药品不存在");
        }
        if (drug == null || drug.getName() == null || drug.getName().trim().isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR, "药品名称不能为空");
        }
        drug.setId(id);
        if (drug.getDosageId() == null) {
            drug.setDosageId(old.getDosageId());
        }
        if (drug.getTypeId() == null) {
            drug.setTypeId(old.getTypeId());
        }
        normalizeDrug(drug, false);
        int n = dmsDrugMapper.updateById(drug);
        if (n <= 0) {
            return Result.error(ResultCode.SERVER_ERROR, "保存失败");
        }
        return Result.success("保存成功");
    }

    private static void normalizeDrug(DmsDrug drug, boolean isNew) {
        if (drug.getCode() != null) {
            drug.setCode(drug.getCode().trim());
        }
        drug.setName(drug.getName().trim());
        if (drug.getGenericName() != null) {
            drug.setGenericName(drug.getGenericName().trim());
        }
        if (drug.getMnemonicCode() != null) {
            drug.setMnemonicCode(drug.getMnemonicCode().trim());
        }
        if (drug.getFormat() != null) {
            drug.setFormat(drug.getFormat().trim());
        }
        if (drug.getUnit() != null) {
            drug.setUnit(drug.getUnit().trim());
        }
        if (drug.getManufacturer() != null) {
            drug.setManufacturer(drug.getManufacturer().trim());
        }
        if (drug.getPrice() == null) {
            drug.setPrice(BigDecimal.ZERO);
        }
        if (drug.getStock() == null) {
            drug.setStock(0L);
        }
        if (drug.getStatus() == null) {
            drug.setStatus(1);
        }
        if (isNew && drug.getCreateDate() == null) {
            drug.setCreateDate(LocalDateTime.now());
        }
    }
}
