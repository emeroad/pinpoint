package com.navercorp.pinpoint.web.metric.mybatis.typehandler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleTypeHandler implements TypeHandler<Number> {
    @Override
    public void setParameter(PreparedStatement ps, int i, Number parameter, JdbcType jdbcType) throws SQLException {
        ps.setDouble(i, (Double) parameter);
    }

    @Override
    public Number getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getDouble(columnName);
    }

    @Override
    public Number getResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getDouble(columnIndex);
    }

    @Override
    public Number getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getDouble(columnIndex);
    }
}
