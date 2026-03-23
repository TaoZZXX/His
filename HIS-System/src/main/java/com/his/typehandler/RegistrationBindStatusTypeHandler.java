package com.his.typehandler;

import com.his.enums.RegistrationBindStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(RegistrationBindStatus.class)
@MappedJdbcTypes(JdbcType.INTEGER)
public class RegistrationBindStatusTypeHandler extends BaseTypeHandler<RegistrationBindStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RegistrationBindStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public RegistrationBindStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int v = rs.getInt(columnName);
        return rs.wasNull() ? null : RegistrationBindStatus.fromCode(v);
    }

    @Override
    public RegistrationBindStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int v = rs.getInt(columnIndex);
        return rs.wasNull() ? null : RegistrationBindStatus.fromCode(v);
    }

    @Override
    public RegistrationBindStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int v = cs.getInt(columnIndex);
        return cs.wasNull() ? null : RegistrationBindStatus.fromCode(v);
    }
}
