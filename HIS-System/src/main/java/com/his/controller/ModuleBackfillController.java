package com.his.controller;

import com.his.domain.Result;
import com.his.enums.ResultCode;
import com.his.service.IModuleBackfillService;
import com.his.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/module-backfill")
public class ModuleBackfillController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IModuleBackfillService moduleBackfillService;

    private boolean validToken(String token) {
        return token != null && jwtUtil.validateToken(token) && jwtUtil.getUserIdFromToken(token) != null;
    }

    @GetMapping("/modules")
    public Result<List<Map<String, Object>>> listModules(@RequestParam("token") String token) {
        if (!validToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        return Result.success("查询成功", moduleBackfillService.listModules());
    }

    @GetMapping("/tables/{table}/columns")
    public Result<List<Map<String, Object>>> listColumns(@PathVariable("table") String table,
                                                         @RequestParam("token") String token) {
        if (!validToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        return Result.success("查询成功", moduleBackfillService.listTableColumns(table));
    }

    @GetMapping("/tables/{table}/rows")
    public Result<Map<String, Object>> pageRows(@PathVariable("table") String table,
                                                @RequestParam("token") String token,
                                                @RequestParam(value = "page", required = false) Integer page,
                                                @RequestParam(value = "size", required = false) Integer size) {
        if (!validToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        return Result.success("查询成功", moduleBackfillService.pageRows(table, page, size));
    }

    @PostMapping("/tables/{table}/save")
    public Result<Map<String, Object>> saveRow(@PathVariable("table") String table,
                                               @RequestParam("token") String token,
                                               @RequestBody Map<String, Object> body) {
        if (!validToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        return Result.success("保存成功", moduleBackfillService.saveRow(table, body));
    }

    @DeleteMapping("/tables/{table}/rows/{id}")
    public Result<Object> deleteRow(@PathVariable("table") String table,
                                    @PathVariable("id") Long id,
                                    @RequestParam("token") String token) {
        if (!validToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        moduleBackfillService.deleteRow(table, id);
        return Result.success("删除成功");
    }
}
