package com.his.service.impl;

import com.his.domain.PageResult;
import com.his.domain.SmsDept;
import com.his.enums.ResultCode;
import com.his.exception.BusinessException;
import com.his.mapper.SmsDeptMapper;
import com.his.service.ISmsDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SmsDeptService implements ISmsDeptService {

    @Autowired
    private SmsDeptMapper smsDeptMapper;

    @Override
    public PageResult<SmsDept> page(int pageNo, int pageSize, String code, String name, Integer catId, Integer type) {
        if (pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 200) {
            pageSize = 200;
        }
        int offset = (pageNo - 1) * pageSize;
        String c = code == null ? null : code.trim();
        String n = name == null ? null : name.trim();
        if (c != null && c.isEmpty()) {
            c = null;
        }
        if (n != null && n.isEmpty()) {
            n = null;
        }
        long total = smsDeptMapper.countByPage(c, n, catId, type);
        List<SmsDept> list = smsDeptMapper.selectByPage(c, n, catId, type, offset, pageSize);
        int pages = pageSize == 0 ? 0 : (int) ((total + pageSize - 1) / pageSize);
        PageResult<SmsDept> pr = new PageResult<>();
        pr.setList(list);
        pr.setTotal(total);
        pr.setPageSize(pageSize);
        pr.setPageNo(pageNo);
        pr.setPages(pages);
        return pr;
    }

    @Override
    @Transactional
    public void create(SmsDept dept) {
        if (dept == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数不能为空");
        }
        if (dept.getCode() == null || dept.getCode().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "科室编码不能为空");
        }
        if (dept.getName() == null || dept.getName().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "科室名称不能为空");
        }
        String code = dept.getCode().trim();
        if (smsDeptMapper.countByCode(code, null) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "科室编码已存在");
        }
        SmsDept row = new SmsDept();
        row.setCode(code);
        row.setName(dept.getName().trim());
        row.setCatId(dept.getCatId());
        row.setType(dept.getType());
        row.setStatus(dept.getStatus() != null ? dept.getStatus() : 1);
        smsDeptMapper.insert(row);
    }

    @Override
    @Transactional
    public void update(Long id, SmsDept dept) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "科室ID不能为空");
        }
        if (dept == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "参数不能为空");
        }
        SmsDept exist = smsDeptMapper.selectDeptById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "科室不存在");
        }
        if (dept.getCode() != null && !dept.getCode().trim().isEmpty()) {
            String code = dept.getCode().trim();
            if (smsDeptMapper.countByCode(code, id) > 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "科室编码已存在");
            }
            exist.setCode(code);
        }
        if (dept.getName() != null) {
            String nm = dept.getName().trim();
            if (nm.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "科室名称不能为空");
            }
            exist.setName(nm);
        }
        if (dept.getCatId() != null) {
            exist.setCatId(dept.getCatId());
        }
        if (dept.getType() != null) {
            exist.setType(dept.getType());
        }
        if (dept.getStatus() != null) {
            exist.setStatus(dept.getStatus());
        }
        smsDeptMapper.updateById(exist);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "科室ID不能为空");
        }
        if (smsDeptMapper.selectDeptById(id) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "科室不存在");
        }
        if (smsDeptMapper.countStaffByDeptId(id) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该科室下仍有员工，无法删除");
        }
        smsDeptMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择要删除的科室");
        }
        for (Long id : ids) {
            if (id == null) {
                continue;
            }
            delete(id);
        }
    }
}
