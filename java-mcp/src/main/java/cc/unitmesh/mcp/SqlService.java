package cc.unitmesh.mcp;

import cc.unitmesh.mcp.annotation.MCPTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SqlService {
    private final JdbcTemplate jdbcTemplate;

    public SqlService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @MCPTool(
        name = "query_sql",
        description = "Execute a select SQL query and return results in a readable format. Results will be truncated after 4000 characters. Will throw an exception if the query is not a SELECT statement.",
        category = "database",
        version = "1.0",
        tags = {"sql", "query", "database"},
        timeoutMs = 30000,
        requiresAuth = true
    )
    @Tool(description = "Execute a select SQL query and return results in a readable format. Results will be truncated after 4000 characters. Will throw an exception if the query is not a SELECT statement.")
    public List<Map<String, Object>> queryBySql(String sql) {
        if (!sql.toLowerCase().startsWith("select")) {
            throw new RuntimeException("Only SELECT queries are allowed.");
        }
        return jdbcTemplate.queryForList(sql);
    }

    @MCPTool(
        name = "list_tables",
        description = "Return all table names in the database separated by comma. This is useful for getting a quick overview of the database structure.",
        category = "database",
        version = "1.0",
        tags = {"sql", "tables", "schema", "database"},
        cacheable = true,
        cacheTtlSeconds = 600
    )
    @Tool(description = "Return all table names in the database separated by comma. This is useful for getting a quick overview of the database structure.")
    public String listAllTablesName() {
        List<Map<String, Object>> tableNames = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()");
        return tableNames.stream()
                .map(e -> e.values().iterator().next().toString())
                .collect(Collectors.joining(","));
    }

    @MCPTool(
        name = "get_table_schema",
        description = "Returns schema and relation information for the given table. Includes column name, data type and constraints. This is useful for understanding the structure of a specific table.",
        category = "database",
        version = "1.0",
        tags = {"sql", "schema", "table", "database", "structure"},
        cacheable = true,
        cacheTtlSeconds = 1800
    )
    @Tool(description = "Returns schema and relation information for the given table. Includes column name, data type and constraints. This is useful for understanding the structure of a specific table.")
    public String getTableSchema(String tableName) {
        String sql = """
                SELECT
                    column_name,
                    data_type,
                    is_nullable,
                    column_default
                FROM information_schema.columns
                WHERE table_name = ?""";
        List<Map<String, Object>> columnNames = jdbcTemplate.queryForList(sql, tableName);
        return columnNames.stream()
                .map(e -> e.values().iterator().next().toString())
                .collect(Collectors.joining(","));
    }
}
