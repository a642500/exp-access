/*
 *     Copyright (C) 2016 Carlos
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package me.toxz.exp.dac.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sun.istack.internal.NotNull;
import me.toxz.exp.dac.data.DatabaseHelper;

import java.sql.SQLException;

/**
 * Created by Carlos on 1/4/16.
 */
@DatabaseTable
public class User implements Ject {
    private static User admin;
    @DatabaseField(generatedId = true) private int _id;
    @DatabaseField(canBeNull = false, unique = true) private String username;
    @DatabaseField private String passwordHash;

    private User() {
        // keep for ORMLite
    }

    public User(@NotNull String username, String password) {
        this.username = username;
        updatePassword(password);
    }

    public static User admin() {
        if (admin == null) {
            try {
                admin = DatabaseHelper.getUserDao().queryForMatching(new User("admin", null)).get(0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return admin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(@NotNull String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isPasswordValidate(String password) {
        return computePasswordHash(password).equals(this.passwordHash);
    }

    public void updatePassword(String password) {
        this.passwordHash = computePasswordHash(password);
    }

    private String computePasswordHash(String password) {
        return password;//TODO hash password
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof User && this._id == ((User) obj)._id;
    }

    @Override
    public int hashCode() {
        return ("User" + _id).hashCode();
    }

    @Override
    public String toString() {
        return username;
    }
}
