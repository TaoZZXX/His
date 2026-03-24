package com.his.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ModuleBackfillMapper {

    List<Map<String, Object>> selectTableColumns(@Param("tableName") String tableName);

    Long countRows(@Param("table") String table);

    List<Map<String, Object>> selectPageRows(@Param("table") String table,
                                             @Param("limit") Integer limit,
                                             @Param("offset") Integer offset);

    int insertRow(@Param("table") String table,
                  @Param("writable") List<String> writable,
                  @Param("body") Map<String, Object> body);

    Long selectLastInsertId();

    int updateRow(@Param("table") String table,
                  @Param("id") Long id,
                  @Param("writable") List<String> writable,
                  @Param("body") Map<String, Object> body);

    int deleteRow(@Param("table") String table, @Param("id") Long id);
}
