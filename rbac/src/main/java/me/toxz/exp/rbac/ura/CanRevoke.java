package me.toxz.exp.rbac.ura;

import com.google.common.collect.Range;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import me.toxz.exp.rbac.Role;
import me.toxz.exp.rbac.data.RoleRangeType;

/**
 * Created by Carlos on 2016/1/16.
 */
@DatabaseTable
public class CanRevoke {
    @DatabaseField(generatedId = true) private int _id;
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Role operator;
    @DatabaseField(persisterClass = RoleRangeType.class)
    private Range<Role> range;

    public CanRevoke(Role operator, Range<Role> range) {
        this.operator = operator;
        this.range = range;
    }

    private CanRevoke() {
    }

    public Role getOperator() {
        return operator;
    }

    public Range<Role> getRange() {
        return range;
    }
}
