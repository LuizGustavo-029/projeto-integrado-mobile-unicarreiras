package com.estagiopro.app.data;

/** Notificação do usuário. */
public class Notice {
    public long id;
    public long userId;
    public String title = "";
    public String body = "";
    public long createdAt;
    public boolean read;
}
