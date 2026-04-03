package com.secondhand.common.jdbc;

import com.secondhand.common.entity.Base;
import com.secondhand.common.spring.SpringApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;

/**
 * JdbcQueryBuilder 用于构建和执行 SQL 语句，提供流畅 API 来生成查询、更新、删除和插入操作。
 */
@SuppressWarnings("all")
@Slf4j
public class JdbcQueryBuilder {

    /** 空格 */
    public static final String SPACE = " ";
    /** 等于符号 */
    public static final String EQ = "=";
    /** 小于符号 */
    public static final String LT = "<";
    /** 小于等于符号 */
    public static final String LTE = "<=";
    /** 大于符号 */
    public static final String GT = ">";
    /** 大于等于符号 */
    public static final String GTE = ">=";
    /** 为空判断 */
    public static final String IS_NULL = "is null";
    /** 不为空判断 */
    public static final String IS_NOT_NULL = "is not null";
    /** IN 关键字 */
    public static final String IN = "in ";
    /** NOT IN 关键字 */
    public static final String NOT_IN = "not in ";
    /** 不等于符号 */
    public static final String NE = "<>";
    /** LIKE 关键字 */
    public static final String LIKE = "like";
    /** NOT LIKE 关键字 */
    public static final String NOT_LIKE = "not like";
    /** SET 关键字 */
    public static final String SET = "set";
    /** AND 关键字 */
    public static final String AND = "and";
    /** OR 关键字 */
    public static final String OR = "or";
    /** 占位符 */
    public static final String QUESTION_MARK = "?";
    /** 逗号 */
    public static final String COMMA = ",";
    /** 空字符串 */
    public static final String EMPTY = "''";

    /** JdbcTemplate 的 Bean 名称 */
    private static final String BEAN_NAME = "jdbcTemplate";
    /** WHERE 关键字 */
    private static final String WHERE = "where";

    /** 用于构建 SQL 语句的 StringBuilder 对象 */
    private StringBuilder builder = new StringBuilder();

    /** 存储连接表名的列表 */
    private List<String> joinTables = new ArrayList<>();
    /** 存储连接条件中第一个列名的列表 */
    private List<String> colOnes = new ArrayList<>();
    /** 存储连接条件中第二个列名的列表 */
    private List<String> colTwos = new ArrayList<>();

    /** 连接类型，默认为左连接 */
    private JoinType joinType = JoinType.LEFT;

    /** 原生 SQL 语句 */
    private String nativeSql;

    /** 当前页码，默认为 1 */
    private int page = 1;
    /** 每页记录数，默认为 10 */
    private int limit = 10;

    /** 当前操作类型，默认为查询 */
    private Action action = Action.SELECT;

    /** 是否不使用分页限制 */
    private boolean withoutLimit = false;

    /** JdbcTemplate 实例，用于执行 SQL 语句 */
    private JdbcTemplate jdbcTemplate;

    /** 操作的表名 */
    private String table;

    /** 要查询的列名列表 */
    private List<String> columns = new ArrayList<>();

    /** 构造函数，指定操作的表名 */
    private JdbcQueryBuilder(String table) {
        this.table = table;
        initTemplate();
    }

    /** 无参构造函数 */
    private JdbcQueryBuilder() {
        initTemplate();
    }

    /** 构造函数，指定操作的表名和要查询的列名列表 */
    private JdbcQueryBuilder(String table, List<String> columns) {
        this.table = table;
        this.columns = columns;
        initTemplate();
    }

    /** 静态工厂方法：指定表名 */
    public static synchronized JdbcQueryBuilder build(String table) {
        return new JdbcQueryBuilder(table);
    }

    /** 静态工厂方法：根据实体类 */
    public static synchronized JdbcQueryBuilder build(Class<? extends Base> c) {
        return new JdbcQueryBuilder(NameHelper.table(c));
    }

    /** 静态工厂方法：根据实体类和列名列表 */
    public static synchronized JdbcQueryBuilder build(Class<? extends Base> c, List<String> columns) {
        return new JdbcQueryBuilder(NameHelper.table(c), columns);
    }

    /** 静态工厂方法：无参 */
    public static synchronized JdbcQueryBuilder build() {
        return new JdbcQueryBuilder();
    }

    /** 静态工厂方法：根据表名和列名列表 */
    public static synchronized JdbcQueryBuilder build(String table, List<String> columns) {
        return new JdbcQueryBuilder(table, columns);
    }

    public static void main(String[] args) {
        List<String> list = Arrays.asList("clothing", "shoes");
        String count = JdbcQueryBuilder.build("omc_order_item").eq("order_id", 1)
                .in("orderItemStatus", "warehousing", "pass")
                .in("orderItemType", list).count();
        System.err.println(count);
    }

    /** 设置分页信息 */
    public JdbcQueryBuilder limit(int page, int limit) {
        this.page = page;
        this.limit = limit;
        return this;
    }

    /** 初始化 JdbcTemplate 实例 */
    private void initTemplate() {
        jdbcTemplate = SpringApplicationContext.getBean(BEAN_NAME);
    }

    /** 设置原生 SQL 语句 */
    public JdbcQueryBuilder select(String nativeSql) {
        this.nativeSql = nativeSql;
        return this;
    }

    /** 添加左连接表 */
    public JdbcQueryBuilder left_join(String joinTable) {
        this.joinTables.add(joinTable);
        return this;
    }

    /** 添加右连接表 */
    public JdbcQueryBuilder right_join(String joinTable) {
        this.joinTables.add(joinTable);
        this.joinType = JoinType.RIGHT;
        return this;
    }

    /** 添加内连接表 */
    public JdbcQueryBuilder inner_join(String joinTable) {
        this.joinTables.add(joinTable);
        this.joinType = JoinType.INNER;
        return this;
    }

    public JdbcQueryBuilder inner_join(Class<? extends Base> c) {
        this.joinTables.add(NameHelper.table(c));
        this.joinType = JoinType.INNER;
        return this;
    }

    /** 设置不使用分页限制 */
    public JdbcQueryBuilder withoutLimit() {
        this.withoutLimit = true;
        return this;
    }

    /** 设置连接条件 */
    public JdbcQueryBuilder on(String colOne, String colTwo) {
        this.colOnes.add(colOne);
        this.colTwos.add(colTwo);
        return this;
    }

    /** 添加等于条件 */
    public JdbcQueryBuilder eq(String key, Object value) {
        return op(key, value, EQ);
    }

    /** 添加不等于条件 */
    public JdbcQueryBuilder ne(String key, Object value) {
        return op(key, value, NE);
    }

    /** 添加小于条件 */
    public JdbcQueryBuilder lt(String key, Object value) {
        return op(key, value, LT);
    }

    /** 添加小于等于条件 */
    public JdbcQueryBuilder lte(String key, Object value) {
        return op(key, value, LTE);
    }

    /** 添加大于条件 */
    public JdbcQueryBuilder gt(String key, Object value) {
        return op(key, value, GT);
    }

    /** 添加大于等于条件 */
    public JdbcQueryBuilder gte(String key, Object value) {
        return op(key, value, GTE);
    }

    /** 添加分组条件 */
    public JdbcQueryBuilder groupBy(String key) {
        if (StringUtils.isNotBlank(key)) {
            builder.append(SPACE).append("group by ").append(key).append(SPACE);
        }
        return this;
    }

    public JdbcQueryBuilder or(List<String> keys, List<Object> values, List<String> ops) {
        if (Objects.nonNull(keys) && Objects.nonNull(values)) {
            builder.append(SPACE);
            if (!hasWhereClause()) {
                builder.append(WHERE).append(SPACE);
            } else {
                builder.append(AND).append(SPACE);
            }
            builder.append("(");
            for (int i = 0; i < keys.size(); i++) {
                String op = ops.get(i);
                Object value = values.get(i);
                if (op.equals(LIKE)) {
                    value = "%" + value + "%";
                }

                orOp(keys.get(i), value, op);
                builder.append(SPACE).append(OR);
            }
            builder.delete(builder.length() - OR.length(), builder.length());
            builder.append(SPACE).append(")");
        }

        return this;
    }

    public JdbcQueryBuilder isNull(String key) {
        return op(key, "NULL", IS_NULL);
    }

    public JdbcQueryBuilder isNotNull(String key) {
        return op(key, "NULL", IS_NOT_NULL);
    }

    public JdbcQueryBuilder in(String key, Object... values) {
        if (Objects.nonNull(values)) {
            buildIn(key, Arrays.asList(values));
        }
        return this;
    }

    public JdbcQueryBuilder nin(String key, Object... values) {
        if (Objects.nonNull(values)) {
            buildnin(key, Arrays.asList(values));
        }
        return this;
    }

    public JdbcQueryBuilder in(String key, List values) {
        if (values != null && values.size() > 0) {
            buildIn(key, values);
        }
        return this;
    }

    public void buildIn(String key, Collection<?> values) {
        StringBuilder inBuilder = new StringBuilder();
        inBuilder.append(SPACE).append("(");
        for (Object value : values) {
            if (value instanceof String) {
                inBuilder.append("'").append(value).append("'").append(",");
            } else {
                inBuilder.append(value).append(",");
            }
        }
        inBuilder.deleteCharAt(inBuilder.length() - 1);
        inBuilder.append(")");
        op(key, inBuilder.toString(), IN);
    }

    public void buildnin(String key, Collection<?> values) {
        StringBuilder inBuilder = new StringBuilder();
        inBuilder.append(SPACE).append("(");
        for (Object value : values) {
            if (value instanceof String) {
                inBuilder.append("'").append(value).append("'").append(",");
            } else {
                inBuilder.append(value).append(",");
            }
        }
        inBuilder.deleteCharAt(inBuilder.length() - 1);
        inBuilder.append(")");
        op(key, inBuilder.toString(), NOT_IN);
    }

    public JdbcQueryBuilder like(String key, Object value) {
        if ((value instanceof String) && org.springframework.util.StringUtils.hasText(value.toString())) {
            String re = String.valueOf(value);
            if (re.contains("'")) {
                re = re.replaceAll("\\'", "\\\\\\'");
            }
            op(key, "%" + re + "%", LIKE);
        }
        return this;
    }

    public JdbcQueryBuilder likePre(String key, Object value) {
        if ((value instanceof String) && org.springframework.util.StringUtils.hasText(value.toString())) {
            String re = String.valueOf(value);
            if (re.contains("'")) {
                re = re.replaceAll("\\'", "\\\\\\'");
            }
            op(key, re + "%", LIKE);
        }
        return this;
    }

    public JdbcQueryBuilder notLike(String key, Object value) {
        if ((value instanceof String) && org.springframework.util.StringUtils.hasText(value.toString())) {
            String re = String.valueOf(value);
            if (re.contains("'")) {
                re = re.replaceAll("\\'", "\\\\\\'");
            }
            op(key, "%" + re + "%", NOT_LIKE);
        }
        return this;
    }

    public JdbcQueryBuilder orderBy(String key, Sort sort) {
        if (StringUtils.isNotBlank(key)) {
            builder.append(SPACE).append("order by ").append(key).append(SPACE).append(sort);
        }
        return this;
    }

    public JdbcQueryBuilder orderBy(List<String> keys, List<Sort> sorts) {
        if (Objects.nonNull(keys) && Objects.nonNull(sorts)) {
            builder.append(SPACE).append("order by ");
            for (int i = 0; i < keys.size(); i++) {
                builder.append(keys.get(i)).append(SPACE).append(sorts.get(i)).append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
        }
        return this;
    }

    public String create() {
        if (StringUtils.isEmpty(nativeSql)) {
            StringBuilder selectBuilder = new StringBuilder();
            if (CollectionUtils.isEmpty(columns)) {
                selectBuilder.append("select * from ");
            } else {
                selectBuilder.append("select").append(SPACE);
                columns.forEach(column -> selectBuilder.append(column).append(","));
                selectBuilder.deleteCharAt(selectBuilder.length() - 1)
                        .append(SPACE).append("from").append(SPACE);
            }
            selectBuilder.append(table).append(SPACE);
            joinTable(selectBuilder);
            if (Action.SELECT.equals(action)) {
                String s = selectBuilder.append(builder).toString();
                if (withoutLimit) {
                    return s;
                }
                return limit(s);
            }
            return selectBuilder.append(builder).toString().concat(SPACE);
        } else {
            if (Action.SELECT.equals(action)) {
                String s = nativeSql.concat(SPACE).concat(builder.toString());
                if (withoutLimit) {
                    return s;
                }
                return limit(s);
            } else {
                return nativeSql.concat(SPACE).concat(builder.toString()).concat(SPACE);
            }
        }
    }

    private String limit(String sql) {
        int offset = Math.max(0, (page - 1) * limit);
        return sql.concat(SPACE)
                .concat("limit").concat(SPACE)
                .concat(String.valueOf(limit))
                .concat(SPACE)
                .concat("offset").concat(SPACE)
                .concat(String.valueOf(offset));
    }

    public int delete() {
        StringBuilder stringBuilder = new StringBuilder("delete from");
        stringBuilder.append(SPACE).append(table).append(builder);
        String deleteSql = stringBuilder.toString();
        log.info("delete sql:{}", deleteSql);
        return jdbcTemplate.update(deleteSql);
    }

    public int insert(Object... params) {
        StringBuilder stringBuilder = new StringBuilder("insert into " + table + " (");
        if (CollectionUtils.isEmpty(columns)) {
            throw new RuntimeException("columns is empty");
        }

        columns.forEach(column -> stringBuilder.append(column).append(","));
        StringBuilder builder = new StringBuilder(stringBuilder.substring(0, stringBuilder.length() - 1)).append(")");
        builder.append(SPACE).append("values (");
        columns.forEach(column -> builder.append("?").append(","));
        StringBuilder finalBuilder = new StringBuilder(builder.substring(0, builder.length() - 1)).append(")");
        String sql = finalBuilder.toString();
        log.info("insert sql:{}", sql);
        return jdbcTemplate.update(sql, params);
    }

    public String count() {
        if (StringUtils.isEmpty(nativeSql)) {
            StringBuilder selectBuilder = new StringBuilder();
            selectBuilder.append("select count(1) total from ").append(table).append(SPACE);
            joinTable(selectBuilder);
            return selectBuilder.append(builder).toString().concat(SPACE);
        } else {
            return nativeSql.concat(SPACE).concat(builder.toString());
        }
    }

    private void joinTable(StringBuilder selectBuilder) {
        if (joinTables.size() != colOnes.size() || colOnes.size() != colTwos.size()) {
            throw new RuntimeException("join table size error");
        }
        if (!CollectionUtils.isEmpty(joinTables)) {
            for (int i = 0; i < joinTables.size(); i++) {
                selectBuilder.append(this.joinType.getValue()).append(SPACE)
                        .append(joinTables.get(i)).append(SPACE)
                        .append("on").append(SPACE);

                String colOne = colOnes.get(i);
                String colTwo = colTwos.get(i);
                if (colOne.contains(".") || colTwo.contains(".")) {
                    selectBuilder.append(colOne).append(EQ).append(colTwo).append(SPACE);
                } else {
                    selectBuilder.append(table).append(".").append(colOne)
                            .append(EQ)
                            .append(joinTables.get(i)).append(".").append(colTwo)
                            .append(SPACE);
                }
            }
        }
    }

    private JdbcQueryBuilder op(String key, Object value, String op) {
        if (!Objects.isNull(value)) {
            builder.append(SPACE);
            if (value instanceof String || value instanceof Enum) {
                String valueStr = Objects.toString(value);
                valueStr = valueStr.replaceAll("\'", "\\\\'");
                if (StringUtils.isNotEmpty(valueStr) && (!op.equals(IN) && !op.equals(NOT_IN))) {
                    if (!hasWhereClause()) {
                        if (valueStr.contains("NULL")) {
                            builder.append(WHERE).append(SPACE).append("(").append(SPACE).append(key).append(SPACE).append(op).append(SPACE).append(SPACE).append(OR)
                                    .append(SPACE).append(key).append(SPACE).append(EQ).append(EMPTY).append(")");
                        } else {
                            builder.append(WHERE).append(SPACE).append(key).append(SPACE).append(op).append(SPACE).append("'").append(valueStr).append("'").append(SPACE);
                        }
                    } else {
                        if (valueStr.contains("NULL")) {
                            builder.append(AND).append(SPACE).append("(").append(SPACE).append(key).append(SPACE).append(op).append(SPACE).append(SPACE).append(OR)
                                    .append(SPACE).append(key).append(SPACE).append(EQ).append(EMPTY).append(")");
                        } else {
                            builder.append(AND).append(SPACE).append(key).append(SPACE).append(op).append(SPACE).append("'").append(valueStr).append("'").append(SPACE);
                        }
                    }
                }
            }
            if (value instanceof Integer || value instanceof Long || op.equals(IN) || op.equals(NOT_IN)) {
                if (!hasWhereClause()) {
                    builder.append(WHERE).append(SPACE).append(key).append(SPACE).append(op).append(SPACE).append(value).append(SPACE);
                } else {
                    builder.append(AND).append(SPACE).append(key).append(SPACE).append(op).append(SPACE).append(value).append(SPACE);
                }
            }
            if (value instanceof Timestamp) {
                if (!hasWhereClause()) {
                    builder.append(WHERE).append(SPACE).append(key).append(SPACE).append(op).append(SPACE).append("'").append(value).append("'").append(SPACE);
                } else {
                    builder.append(AND).append(SPACE).append(key).append(SPACE).append(op).append(SPACE).append("'").append(value).append("'").append(SPACE);
                }
            }
            if (value instanceof Boolean) {
                if (!hasWhereClause()) {
                    builder.append(WHERE).append(SPACE).append(key).append(SPACE).append(op).append(SPACE).append(value).append(SPACE);
                } else {
                    builder.append(AND).append(SPACE).append(key).append(SPACE).append(op).append(SPACE).append(value).append(SPACE);
                }
            }
        }
        return this;
    }

    private boolean hasWhereClause() {
        if (builder.indexOf(WHERE) != -1) {
            return true;
        }
        return StringUtils.isNotBlank(nativeSql)
                && StringUtils.containsIgnoreCase(nativeSql, SPACE + WHERE + SPACE);
    }

    private void orOp(String key, Object value, String op) {
        if (!Objects.isNull(value)) {
            builder.append(SPACE);
            if (value instanceof String) {
                String valueStr = Objects.toString(value);
                if (StringUtils.isNotEmpty(valueStr) && !op.equals(IN)) {
                    builder.append(SPACE).append(key).append(SPACE).append(op).append(SPACE).append("'").append(valueStr).append("'").append(SPACE);
                }
            }
            if (value instanceof Number || op.equals(IN)) {
                builder.append(SPACE).append(key).append(SPACE).append(op).append(SPACE).append(value).append(SPACE);
            }
        }
    }

    public <T> List<T> find(Class<T> clazz) {
        String sql = this.create();
        log.info("sql:{}", sql);
        if (columns.size() == 1) {
            return jdbcTemplate.queryForList(sql, clazz);
        }
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<T>(clazz));
    }

    public List<Map<String, Object>> find() {
        String sql = this.create();
        log.info("sql:{}", sql);
        return jdbcTemplate.queryForList(sql);
    }

    public int update(Object... params) {
        StringBuilder stringBuilder = new StringBuilder("update");
        stringBuilder.append(SPACE).append(table).append(SPACE).append(SET).append(SPACE);
        for (String column : columns) {
            stringBuilder.append(column).append(EQ).append(QUESTION_MARK).append(COMMA);
        }
        String updateSql = stringBuilder.substring(0, stringBuilder.length() - 1);
        updateSql = updateSql.concat(builder.toString());
        log.info("update sql:{}", updateSql);
        return jdbcTemplate.update(updateSql, params);
    }

    public <T> T object(Class<T> clazz) {
        String sql = this.create();
        log.info("sql:{}", sql);
        return jdbcTemplate.queryForObject(sql, clazz);
    }

    public <T> T findOne(Class<T> clazz) {
        String sql = this.create();
        log.info("sql:{}", sql);
        if (columns.size() == 1) {
            return jdbcTemplate.queryForObject(sql, clazz);
        }
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<T>(clazz));
    }

    public Long total() {
        String sql = this.count();
        log.info("sql:{}", sql);
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    /** SQL 操作类型 */
    public enum Action {
        UPDATE,
        SELECT,
        DELETE,
        INSERT
    }

    /** SQL 连接类型 */
    public enum JoinType {
        LEFT("left join"),
        RIGHT("right join"),
        INNER("inner join");

        private String value;

        JoinType(String s) {
            this.value = s;
        }

        public String getValue() {
            return this.value;
        }
    }

    /** 排序类型 */
    public enum Sort {
        ASC,
        DESC
    }
}
