package com.mjmh.dao.common;

import com.mjmh.common.ReflectHelper;
import com.mjmh.entity.common.Page;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.sql.CountSqlParserUtil;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import javax.xml.bind.PropertyException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


/**
 * Created by zhujf on 2018/4/20
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }) })
public class PagePlugin implements Interceptor {

    private static String dialect = ""; // 数据库方言
    private static String pageSqlId = ""; // mapper.xml中需要拦截的ID(正则匹配)
    private static String listPagelimitsql = "listPagelimitsql"; // mapper.xml中设置limit位置参数名称
    public Object intercept(Invocation ivk) throws Throwable {
        // TODO Auto-generated method stub
        if (ivk.getTarget() instanceof RoutingStatementHandler) {
            RoutingStatementHandler statementHandler = (RoutingStatementHandler) ivk
                    .getTarget();
            BaseStatementHandler delegate = (BaseStatementHandler) ReflectHelper
                    .getValueByFieldName(statementHandler, "delegate");
            MappedStatement mappedStatement = (MappedStatement) ReflectHelper
                    .getValueByFieldName(delegate, "mappedStatement");

            if (mappedStatement.getId().matches(pageSqlId)) { // 拦截需要分页的SQL
                BoundSql boundSql = delegate.getBoundSql();
                Object parameterObject = boundSql.getParameterObject();// 分页SQL<select>中parameterType属性对应的实体参数，即Mapper接口中执行分页方法的参数,该参数不得为空
                if (parameterObject == null) {
                    throw new NullPointerException("parameterObject尚未实例化！");
                } else {
                    Connection connection = (Connection) ivk.getArgs()[0];
                    String sql = boundSql.getSql();
                    String countSql = "select count(0) from (" + sql
                            + ") as tmp_count"; // 记录统计
                    String[] notCountSQLIDs = {

                            //"com.mjmh.dao.sms.SMSCommentsLogDao.querylistPage"

                    };
                    if (!Arrays.asList(notCountSQLIDs).contains(
                            mappedStatement.getId())) {
                        try {
                            countSql = this.getCountSQL(
                                    mappedStatement.getId(), sql);
                        } catch (Exception e) {
                            countSql = "select count(0) from (" + sql
                                    + ") as tmp_count"; // 记录统计
                        }
                    }
                    PreparedStatement countStmt = null;
                    ResultSet rs = null;
                    int count = 0;
                    try {
                        countStmt = connection.prepareStatement(countSql);
                        BoundSql countBS = new BoundSql(
                                mappedStatement.getConfiguration(), countSql,
                                boundSql.getParameterMappings(),
                                parameterObject);
                        setParameters(countStmt, mappedStatement, countBS,
                                parameterObject);
                        rs = countStmt.executeQuery();
                        if (rs.next()) {
                            count = rs.getInt(1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (SQLException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        if (countStmt != null) {
                            try {
                                countStmt.close();
                            } catch (SQLException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                    Page page = null;
                    if (parameterObject instanceof Page) { // 参数就是Page实体
                        page = (Page) parameterObject;
                        page.setEntityOrField(true);
                        page.setTotalResult(count);
                    } else { // 参数为某个实体，该实体拥有Page属性
                        Field pageField = ReflectHelper.getFieldByFieldName(
                                parameterObject, "page");
                        if (pageField != null) {
                            page = (Page) ReflectHelper.getValueByFieldName(
                                    parameterObject, "page");
                            if (page == null)
                                page = new Page();
                            page.setEntityOrField(false);
                            page.setTotalResult(count);
                            ReflectHelper.setValueByFieldName(parameterObject,
                                    "page", page); // 通过反射，对实体对象设置分页对象
                        } else {
                            throw new NoSuchFieldException(parameterObject
                                    .getClass().getName() + "不存在 page 属性！");
                        }
                    }
                    String pageSql = "";
                    if ("com.mjmh.dao.sms.aaaDao.querylistPage"
                            .equals(mappedStatement.getId())) {
                        pageSql = this.getOrderDataSQL(sql, page);
                    } else {
                        pageSql = generatePageSql(sql, page);
                    }
                    ReflectHelper.setValueByFieldName(boundSql, "sql", pageSql); // 将分页sql语句反射回BoundSql.
                }
            }
        }
        return ivk.proceed();
    }

    /**
     * 对SQL参数(?)设值,参考org.apache.ibatis.executor.parameter.
     * DefaultParameterHandler
     *
     * @param ps
     * @param mappedStatement
     * @param boundSql
     * @param parameterObject
     * @throws SQLException
     */
    private void setParameters(PreparedStatement ps,
                               MappedStatement mappedStatement, BoundSql boundSql,
                               Object parameterObject) throws SQLException {
        ErrorContext.instance().activity("setting parameters")
                .object(mappedStatement.getParameterMap().getId());
        List<ParameterMapping> parameterMappings = boundSql
                .getParameterMappings();
        if (parameterMappings != null) {
            Configuration configuration = mappedStatement.getConfiguration();
            TypeHandlerRegistry typeHandlerRegistry = configuration
                    .getTypeHandlerRegistry();
            MetaObject metaObject = parameterObject == null ? null
                    : configuration.newMetaObject(parameterObject);
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    PropertyTokenizer prop = new PropertyTokenizer(propertyName);
                    if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry
                            .hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (propertyName
                            .startsWith(ForEachSqlNode.ITEM_PREFIX)
                            && boundSql.hasAdditionalParameter(prop.getName())) {
                        value = boundSql.getAdditionalParameter(prop.getName());
                        if (value != null) {
                            value = configuration.newMetaObject(value)
                                    .getValue(
                                            propertyName.substring(prop
                                                    .getName().length()));
                        }
                    } else {
                        value = metaObject == null ? null : metaObject
                                .getValue(propertyName);
                    }
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    if (typeHandler == null) {
                        throw new ExecutorException(
                                "There was no TypeHandler found for parameter "
                                        + propertyName + " of statement "
                                        + mappedStatement.getId());
                    }
                    typeHandler.setParameter(ps, i + 1, value,
                            parameterMapping.getJdbcType());
                }
            }
        }
    }

    /**
     * 根据数据库方言，生成特定的分页sql
     *
     * @param sql
     * @param page
     * @return
     */
    private String generatePageSql(String sql, Page page) {
        if (page != null && Util.notEmpty(dialect)) {
            StringBuffer pageSql = new StringBuffer();
            if ("mysql".equals(dialect)) {
                if (sql.indexOf(listPagelimitsql)>0) {
                    pageSql.append(sql.replace(listPagelimitsql, " limit " + page.getCurrentResult() + ","
                            + page.getRows()));
                }else{
                    pageSql.append(sql);
                    pageSql.append(" limit " + page.getCurrentResult() + ","
                            + page.getRows());
                }
            } else if ("oracle".equals(dialect)) {
                pageSql.append("select * from (select tmp_tb.*,ROWNUM row_id from (");
                pageSql.append(sql);
                // pageSql.append(") as tmp_tb where ROWNUM<=");
                pageSql.append(") tmp_tb where ROWNUM<=");
                pageSql.append(page.getCurrentResult() + page.getRows());
                pageSql.append(") where row_id>");
                pageSql.append(page.getCurrentResult());
            }
            return pageSql.toString();
        } else {
            return sql;
        }
    }

    public Object plugin(Object arg0) {
        // TODO Auto-generated method stub
        return Plugin.wrap(arg0, this);
    }

    public void setProperties(Properties p) {
        dialect = p.getProperty("dialect");
        if (Util.isEmpty(dialect)) {
            try {
                throw new PropertyException("dialect property is not found!");
            } catch (PropertyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        pageSqlId = p.getProperty("pageSqlId");
        if (Util.isEmpty(pageSqlId)) {
            try {
                throw new PropertyException("pageSqlId property is not found!");
            } catch (PropertyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private String getCountSQL(String id, String sql)
            throws IOException, JSQLParserException {
        if (sql.indexOf(listPagelimitsql)>0) {
            sql=sql.replace(listPagelimitsql, "");
        }
        return new CountSqlParserUtil().removeCountNotUsed(sql); // 记录统计
    }

    /**
     * 临时处理工单列表中，翻页超过1000页时，出现的性能问题
     *
     * @param sql
     * @param page
     * @return
     */
    private String getOrderDataSQL(String sql, Page page) {
        if (page != null && Util.notEmpty(dialect)) {
            String serviceTimeASC = "ORDER BY c.ServiceTime ASC".toLowerCase();
            String serviceTimeDESC = "ORDER BY c.ServiceTime desc".toLowerCase();
            String ServiceFinishTimeASC = "ORDER BY c.ServiceFinishTime ASC".toLowerCase();
            String yuyueEndTimeDESC = "c.YuyueEndTime ASC, a.CreateTime ASC".toLowerCase();
            String markServiceTimeASC ="a.mark desc,c.servicetime asc".toLowerCase();
            String CreateTimeDESC = "ORDER BY a.CreateTime DESC".toLowerCase();
            String CreateTimeASC = "ORDER BY a.CreateTime ASC".toLowerCase();
            String logisticssigntimeDESC="order by d.logisticssigntime asc".toLowerCase();
            String lowersql = sql.toLowerCase();
            int whereIndex = lowersql.indexOf("where");
            String where = lowersql.substring(whereIndex + 5);
            String startQuery = lowersql.substring(0, whereIndex);
            String[] ands = where.split("and");
            String orderbaseWhere = "";
            String orderworkerWhere = "";
            String ordertmallWhere = "";
            String endWhere = "";
            Boolean bl = false;//是否关联ordertmall合单
            for (int i = 0; i < ands.length; i++) {
                String and = ands[i].trim();
                if ("".equals(and) || "1=1".equals(and)) {
                    continue;
                }
                if (and.startsWith("a.")
                        ||and.startsWith("(a.Status")
                        ||and.startsWith("(a.status")
                        ||and.startsWith("(a.HasWorker")) {//bui_orderbase
                    orderbaseWhere += " and ";
                    orderbaseWhere += and;
                }else if (and.startsWith("c.")) {//bui_orderworker
                    orderworkerWhere += " and ";
                    orderworkerWhere += and;
                    orderbaseWhere += " and ";
                    orderbaseWhere += and;
                }else if (and.startsWith("f.")
                        ||and.startsWith("(f.TmallWriteOff")
                        ||and.startsWith("(f.tmallwriteoff")
                        ||and.startsWith("f.oldnumber")
                        ||and.startsWith("f.Oldnumber")) {//bui_ordertmall
                    ordertmallWhere += " and ";
                    ordertmallWhere += and;
                    orderbaseWhere += " and ";
                    orderbaseWhere += and;
                    if(and.startsWith("f.oldnumber LIKE") || and.startsWith("f.oldnumber like")){
                        bl=true;
                    }
                }else {
                    endWhere += " and ";
                    endWhere += and;
                }
            }
            String replaceStr="(SELECT a.* FROM bui_orderbase a ${bui_orderworker} ${bui_ordertmall} ${bui_orderlogistics} WHERE 1=1 ";
            replaceStr+=orderbaseWhere;
            if(!"".equals(orderworkerWhere)){
                replaceStr=replaceStr.replace("${bui_orderworker}", " left join bui_orderworker c on a.OrderWorkerId = c.ID ");
            }else{
                if (lowersql.indexOf(yuyueEndTimeDESC) > 0
                        ||lowersql.indexOf(markServiceTimeASC)>0
                        ||lowersql.indexOf(serviceTimeASC)>0
                        ||lowersql.indexOf(serviceTimeDESC)>0
                        ||lowersql.indexOf(ServiceFinishTimeASC)>0) {
                    replaceStr=replaceStr.replace("${bui_orderworker}", " left join bui_orderworker c on a.OrderWorkerId = c.ID ");
                }else{
                    replaceStr=replaceStr.replace("${bui_orderworker}", "");
                }
            }
            if(!"".equals(ordertmallWhere)){
                if(bl){
                    replaceStr=replaceStr.replace("${bui_ordertmall}", " LEFT JOIN bui_ordertmall f ON a.ID = f.mergeorderid ");
                }else{
                    replaceStr=replaceStr.replace("${bui_ordertmall}", " LEFT JOIN bui_ordertmall f ON a.ID = f.BaseOrderID ");
                }
            }else{
                replaceStr=replaceStr.replace("${bui_ordertmall}", "");
            }


            if (lowersql.indexOf(logisticssigntimeDESC) > 0) {
                replaceStr=replaceStr.replace("${bui_orderlogistics}", " left join bui_orderlogistics d ON a.ID = d.BaseOrderID ");
            }else{
                replaceStr=replaceStr.replace("${bui_orderlogistics}", "");
            }

            replaceStr+=" limit "
                    + page.getCurrentResult() + "," + page.getRows()
                    + " ) AS a";
            startQuery = startQuery.replace(
                    "bui_orderbase a",replaceStr);
            startQuery=startQuery.replace("group by a.`id`", "");
            if (!"".equals(endWhere)) {
                return this.generatePageSql(sql, page);
            } else {
                if (startQuery.indexOf(yuyueEndTimeDESC) > 0) {
                    startQuery = startQuery.replace(yuyueEndTimeDESC, "");
                    startQuery = startQuery.replace("limit",yuyueEndTimeDESC+" limit");
                } else if (startQuery.indexOf(markServiceTimeASC) > 0) {
                    startQuery = startQuery.replace(markServiceTimeASC, "");
                    startQuery = startQuery.replace("limit",markServiceTimeASC+" limit");
                } else if (startQuery.indexOf(serviceTimeASC) > 0) {
                    startQuery = startQuery.replace(serviceTimeASC, "");
                    startQuery = startQuery.replace("limit",serviceTimeASC+" limit");
                }else if (startQuery.indexOf(serviceTimeDESC) > 0) {
                    startQuery = startQuery.replace(serviceTimeDESC, "");
                    startQuery = startQuery.replace("limit",serviceTimeASC+" limit");
                }else if (startQuery.indexOf(ServiceFinishTimeASC) > 0) {
                    startQuery = startQuery.replace(ServiceFinishTimeASC, "");
                    startQuery = startQuery.replace("limit",ServiceFinishTimeASC+" limit");
                }else if (startQuery.indexOf(CreateTimeDESC) > 0) {
                    startQuery = startQuery.replace(CreateTimeDESC, "");
                    startQuery = startQuery.replace("limit",
                            "ORDER BY CreateTime DESC limit");
                }else if (startQuery.indexOf(CreateTimeASC) > 0) {
                    startQuery = startQuery.replace(CreateTimeASC, "");
                    startQuery = startQuery.replace("limit",
                            "ORDER BY CreateTime ASC limit");
                }
                //startQuery+=" GROUP BY a.id ";

            }
            return startQuery;
        }
        return sql;

    }
}
