package com.estagiopro.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.estagiopro.app.data.User;

/** Anexar e abrir o currículo (PDF/DOC) escolhido pelo aluno. */
public final class CvHelper {

    private CvHelper() { }

    public static final String[] MIME = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};

    /** Guarda no objeto User os dados do arquivo escolhido (ainda não salva no banco). */
    public static void store(Activity a, User u, Uri uri) {
        try {
            a.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
            // alguns provedores não permitem permissão persistente
        }
        String name = "curriculo";
        long size = 0;
        Cursor c = a.getContentResolver().query(uri, null, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    int ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    int si = c.getColumnIndex(OpenableColumns.SIZE);
                    if (ni >= 0 && !c.isNull(ni)) name = c.getString(ni);
                    if (si >= 0 && !c.isNull(si)) size = c.getLong(si);
                }
            } finally {
                c.close();
            }
        }
        u.cvUri = uri.toString();
        u.cvName = name;
        u.cvSize = size;
        u.cvDate = System.currentTimeMillis();
    }

    public static void clear(User u) {
        u.cvUri = "";
        u.cvName = "";
        u.cvSize = 0;
        u.cvDate = 0;
    }

    public static void open(Activity a, String uri, String name) {
        if (uri == null || uri.isEmpty()) {
            Ui.toast(a, "Este currículo é apenas um exemplo (sem arquivo anexado)");
            return;
        }
        Uri u = Uri.parse(uri);
        String type = a.getContentResolver().getType(u);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(u, type != null ? type : "application/pdf");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            a.startActivity(i);
        } catch (ActivityNotFoundException e) {
            Ui.toast(a, "Nenhum aplicativo encontrado para abrir " + name);
        } catch (SecurityException e) {
            Ui.toast(a, "Sem permissão para abrir o arquivo. Anexe o currículo novamente.");
        }
    }
}
