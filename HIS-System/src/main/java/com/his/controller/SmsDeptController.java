package com.his.controller;

import com.his.domain.PageResult;
import com.his.domain.Result;
import com.his.domain.SmsDept;
import com.his.service.ISmsDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dept")
public class SmsDeptController {

    @Autowired
    private ISmsDeptService smsDeptService;

    /**
     * 科室管理：分页查询（含筛选）
     */
    @GetMapping("/page")
    public Result<PageResult<SmsDept>> page(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "catId", required = false) Integer catId,
            @RequestParam(value = "type", required = false) Integer type) {
        return Result.success("查询成功", smsDeptService.page(page, size, code, name, catId, type));
    }

    @PostMapping
    public Result<Void> create(@RequestBody SmsDept body) {
        smsDeptService.create(body);
        return Result.success("新增成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody SmsDept body) {
        smsDeptService.update(id, body);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        smsDeptService.delete(id);
        return Result.success("删除成功");
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body == null ? null : body.get("ids");
        smsDeptService.batchDelete(ids);
        return Result.success("删除成功");
    }
}
