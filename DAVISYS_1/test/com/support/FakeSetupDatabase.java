package com.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.SwingUtilities;

/** In-memory JDBC contract fixture. It cannot connect to or modify a real database. */
public final class FakeSetupDatabase {
    public final List<Map<String,Object>> products = new ArrayList<>();
    public final Map<String,Integer> cartQuantities = new HashMap<>();
    public final List<String> queries = new ArrayList<>();
    public boolean validCart = true;
    public String failCode;
    public int commits, rollbacks, writes, opened, closed, statementsOpened, statementsClosed, resultsOpened, resultsClosed;
    public volatile boolean queriedOnEdt;
    public String cartId = "GH-REAL", customerId = "KH01", username = "staff", phone = "0900000000";

    public static Map<String,Object> product(String code, String category, Double price, Integer stock, String description) {
        Map<String,Object> row = new HashMap<>();
        row.put("MASP", code); row.put("TENSP", "Product "+code); row.put("MALH", category); row.put("TENLH", category);
        row.put("GIABAN", price); row.put("SOLUONG", stock); row.put("MOTA", description); row.put("HINH", "");
        return row;
    }

    public Connection open() {
        opened++;
        boolean[] auto = {true};
        boolean[] isClosed = {false};
        @SuppressWarnings("unchecked") Map<String,Integer>[] snapshot = new Map[]{null};
        return proxy(Connection.class, (obj, method, args) -> {
            switch (method.getName()) {
                case "getAutoCommit": return auto[0];
                case "setAutoCommit":
                    auto[0] = (Boolean) args[0];
                    if (!auto[0]) snapshot[0] = new HashMap<>(cartQuantities);
                    return null;
                case "prepareStatement": return statement((String)args[0]);
                case "commit": commits++; snapshot[0] = null; return null;
                case "rollback":
                    rollbacks++;
                    if (snapshot[0] != null) { cartQuantities.clear(); cartQuantities.putAll(snapshot[0]); }
                    return null;
                case "close": if (!isClosed[0]) { closed++; isClosed[0] = true; } return null;
                case "isClosed": return isClosed[0];
                case "toString": return "FakeConnection";
                default: throw new UnsupportedOperationException("Unexpected connection method: " + method.getName());
            }
        });
    }

    private PreparedStatement statement(String sql) {
        statementsOpened++;
        Map<Integer,Object> params = new HashMap<>();
        boolean[] isClosed = {false};
        return proxy(PreparedStatement.class, (obj, method, args) -> {
            switch (method.getName()) {
                case "setString": case "setInt": case "setObject": params.put((Integer)args[0], args[1]); return null;
                case "executeQuery":
                    queriedOnEdt |= SwingUtilities.isEventDispatchThread();
                    queries.add(sql); return result(query(sql.toUpperCase(Locale.ROOT), params));
                case "executeUpdate":
                    queriedOnEdt |= SwingUtilities.isEventDispatchThread();
                    queries.add(sql); return write(sql.toUpperCase(Locale.ROOT), params);
                case "close": if (!isClosed[0]) { statementsClosed++; isClosed[0] = true; } return null;
                case "toString": return "FakeStatement";
                default: throw new UnsupportedOperationException("Unexpected statement method: " + method.getName());
            }
        });
    }

    private List<Map<String,Object>> query(String sql, Map<Integer,Object> params) throws SQLException {
        if (sql.startsWith("SELECT S.MASP")) {
            return products.stream().filter(p -> !params.containsKey(1) || params.get(1).equals(p.get("MALH"))).toList();
        }
        if (sql.startsWith("SELECT G.MAGH,G.MAKH")) {
            return validCart && username.equals(params.get(1)) ? List.of(cartRow()) : List.of();
        }
        if (sql.startsWith("SELECT G.MAGH FROM")) {
            return validCart && cartId.equals(params.get(1)) && customerId.equals(params.get(2))
                    && username.equals(params.get(3)) ? List.of(cartRow()) : List.of();
        }
        if (sql.startsWith("SELECT GIABAN,SOLUONG")) {
            return products.stream().filter(p -> params.get(1).equals(p.get("MASP"))).toList();
        }
        if (sql.startsWith("SELECT SOLUONG FROM")) {
            Integer count = cartQuantities.get((String)params.get(2));
            return count == null ? List.of() : List.of(Map.of("SOLUONG", count));
        }
        throw new SQLException("Unknown query in fixture: " + sql);
    }

    private Map<String,Object> cartRow() {
        return Map.of("MAGH", cartId, "MAKH", customerId, "DIENTHOAI", phone, "HOTEN", "Customer");
    }
    private int write(String sql, Map<Integer,Object> params) throws SQLException {
        String code;
        int quantity;
        if (sql.startsWith("INSERT INTO DBO.GIOHANGTAM")) { code = (String)params.get(2); quantity = (Integer)params.get(3); }
        else if (sql.startsWith("UPDATE DBO.GIOHANGTAM")) { code = (String)params.get(3); quantity = (Integer)params.get(1); }
        else throw new SQLException("Unknown write in fixture: " + sql);
        if (code.equals(failCode)) throw new SQLException("Injected middle-row failure");
        writes++; cartQuantities.put(code, quantity); return 1;
    }

    private ResultSet result(List<Map<String,Object>> rows) {
        resultsOpened++;
        int[] position = {-1}; boolean[] wasNull = {false}, isClosed = {false};
        return proxy(ResultSet.class, (obj, method, args) -> {
            switch (method.getName()) {
                case "next": return ++position[0] < rows.size();
                case "getString": case "getDouble": case "getInt":
                    Object value = rows.get(position[0]).get(args[0]); wasNull[0] = value == null;
                    if (method.getName().equals("getString")) return value == null ? null : value.toString();
                    if (method.getName().equals("getDouble")) return value == null ? 0d : ((Number)value).doubleValue();
                    return value == null ? 0 : ((Number)value).intValue();
                case "wasNull": return wasNull[0];
                case "close": if (!isClosed[0]) { resultsClosed++; isClosed[0] = true; } return null;
                case "toString": return "FakeResultSet";
                default: throw new UnsupportedOperationException("Unexpected result method: " + method.getName());
            }
        });
    }
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
