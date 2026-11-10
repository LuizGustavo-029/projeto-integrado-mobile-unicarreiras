package com.estagiopro.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Banco SQLite local do app. Na primeira execução é preenchido com dados de exemplo. */
public class Db extends SQLiteOpenHelper {

    private static final String NAME = "estagiopro.db";
    private static final int VERSION = 1;
    private static Db instance;

    public static synchronized Db get(Context c) {
        if (instance == null) instance = new Db(c.getApplicationContext());
        return instance;
    }

    private Db(Context c) {
        super(c, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, role TEXT NOT NULL, name TEXT, "
                + "email TEXT NOT NULL UNIQUE COLLATE NOCASE, salt TEXT, pass TEXT, university TEXT, course TEXT, "
                + "semester TEXT, phone TEXT, linkedin TEXT, about TEXT, skills TEXT, cv_uri TEXT, cv_name TEXT, "
                + "cv_size INTEGER DEFAULT 0, cv_date INTEGER DEFAULT 0, org_name TEXT, org_type TEXT, initials TEXT, "
                + "cnpj TEXT, sector TEXT, location TEXT, responsible TEXT, responsible_role TEXT, "
                + "status TEXT DEFAULT 'Disponível', grad_year INTEGER DEFAULT 0, created_at INTEGER)");
        db.execSQL("CREATE TABLE jobs (id INTEGER PRIMARY KEY AUTOINCREMENT, owner_id INTEGER NOT NULL, "
                + "company_name TEXT, title TEXT, alias TEXT, area TEXT, location TEXT, mode TEXT, type TEXT, "
                + "salary TEXT, hours TEXT, description TEXT, requirements TEXT, status TEXT, source TEXT, "
                + "views INTEGER DEFAULT 0, created_at INTEGER)");
        db.execSQL("CREATE TABLE applications (id INTEGER PRIMARY KEY AUTOINCREMENT, job_id INTEGER NOT NULL, "
                + "student_id INTEGER NOT NULL, stage INTEGER DEFAULT 0, interview_at INTEGER DEFAULT 0, "
                + "favorite INTEGER DEFAULT 0, match_pct INTEGER DEFAULT 0, referred INTEGER DEFAULT 0, "
                + "created_at INTEGER, updated_at INTEGER, UNIQUE(job_id, student_id))");
        db.execSQL("CREATE TABLE saved (student_id INTEGER NOT NULL, job_id INTEGER NOT NULL, "
                + "PRIMARY KEY (student_id, job_id))");
        db.execSQL("CREATE TABLE notices (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, "
                + "title TEXT, body TEXT, created_at INTEGER, is_read INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE meta (k TEXT PRIMARY KEY, v TEXT)");
        db.execSQL("CREATE INDEX idx_jobs_owner ON jobs(owner_id)");
        db.execSQL("CREATE INDEX idx_apps_job ON applications(job_id)");
        db.execSQL("CREATE INDEX idx_apps_student ON applications(student_id)");
        db.execSQL("CREATE INDEX idx_notices_user ON notices(user_id)");
        Seeder.seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // versão única por enquanto
    }
}
