package com.core.match.db.jdbc;

import com.core.match.db.jdbc.msgs.DBFieldEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Map;

/**
 * Created by jgreco on 12/28/14.
 */
public class WriterUtilities
{

	public static PreparedStatement assemble(Connection connection) throws SQLException
	{
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" insert into messages (");
		EnumSet<DBFieldEnum> allDbFieldEnums=EnumSet.allOf(DBFieldEnum.class);
		int i=0;
		for (DBFieldEnum fieldEnum: allDbFieldEnums)
		{
			if (i != 0)
			{
				queryBuilder.append(", ");
			}
			queryBuilder.append(fieldEnum.name());
			i++;
		}
		queryBuilder.append(") values (");

		for (int j=0;j<allDbFieldEnums.size();j++)
		{
			if (j != 0)
			{
				queryBuilder.append(", ");
			}
			queryBuilder.append("?");
		}
		queryBuilder.append(")");
		PreparedStatement preparedStatement=connection.prepareStatement(queryBuilder.toString());

		for(Map.Entry<DBFieldEnum,Integer> entry: DBFieldEnum.getFieldEnumTypeMap().entrySet()){
			preparedStatement.setNull(entry.getKey().getColumnIndex(), entry.getValue().intValue());

		}
		return preparedStatement;
	}


}
