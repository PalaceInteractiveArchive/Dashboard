package network.palace.dashboard.forums.db;

import java.util.HashMap;

/**
 * Created by Marc on 12/12/16.
 */
public class DbRow extends HashMap<String, Object> {
    public Object get(String column) {
        return super.get(column);
    }

    public Object get(String column, Object def) {
        Object res = super.get(column);
        return res == null ? def : res;
    }

    public Object remove(String column) {
        return super.remove(column);
    }

    public Object remove(String column, Object def) {
        Object res = super.remove(column);
        return res == null ? def : res;
    }

    @Override
    public DbRow clone() {
        DbRow clone = (DbRow) super.clone();
        DbRow row = new DbRow();
        row.putAll(this);
        return row;
    }
}
