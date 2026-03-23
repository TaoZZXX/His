package com.his.typehandler;

import com.his.enums.RegistrationStatusCode;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(RegistrationStatusCode.class)
@MappedJdbcTypes(JdbcType.INTEGER)
public class RegistrationStatusCodeTypeHandler extends BaseTypeHandler<RegistrationStatusCode> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RegistrationStatusCode parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public RegistrationStatusCode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int v = rs.getInt(columnName);
        return rs.wasNull() ? null : RegistrationStatusCode.fromCode(v);
    }

    @Override
    public RegistrationStatusCode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int v = rs.getInt(columnIndex);
        return rs.wasNull() ? null : RegistrationStatusCode.fromCode(v);
    }

    @Override
    public RegistrationStatusCode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int v = cs.getInt(columnIndex);
        return cs.wasNull() ? null : RegistrationStatusCode.fromCode(v);
    }
}
