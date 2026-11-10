package com.estagiopro.app.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Guarda quem está logado. */
public final class Session {

    private Session() { }

    private static SharedPreferences p(Context c) {
        return c.getApplicationContext().getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public static long userId(Context c) {
        return p(c).getLong("user_id", 0);
    }

    public static void login(Context c, User u) {
        p(c).edit().putLong("user_id", u.id).apply();
    }

    public static void logout(Context c) {
        p(c).edit().clear().apply();
    }

    public static User current(Context c) {
        long id = userId(c);
        return id == 0 ? null : Repo.getUser(c, id);
    }
}
