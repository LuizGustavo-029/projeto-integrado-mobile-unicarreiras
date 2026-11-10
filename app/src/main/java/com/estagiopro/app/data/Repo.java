package com.estagiopro.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.estagiopro.app.util.Fmt;
import com.estagiopro.app.util.Security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Todas as operações de leitura e escrita no banco. */
public final class Repo {

    private Repo() { }

    private static SQLiteDatabase db(Context c) {
        return Db.get(c).getWritableDatabase();
    }

    // ------------------------------------------------------------------ cursor helpers
    private static String s(Cursor c, String col) {
        int i = c.getColumnIndex(col);
        return (i < 0 || c.isNull(i)) ? "" : c.getString(i);
    }

    private static long l(Cursor c, String col) {
        int i = c.getColumnIndex(col);
        return (i < 0 || c.isNull(i)) ? 0 : c.getLong(i);
    }

    private static int n(Cursor c, String col) {
        return (int) l(c, col);
    }

    private static String[] a(Object... args) {
        String[] out = new String[args.length];
        for (int i = 0; i < args.length; i++) out[i] = String.valueOf(args[i]);
        return out;
    }

    private static long scalar(Context c, String sql, String... args) {
        Cursor cur = db(c).rawQuery(sql, args);
        try {
            return cur.moveToFirst() ? cur.getLong(0) : 0;
        } finally {
            cur.close();
        }
    }

    // ------------------------------------------------------------------ meta
    public static String getMeta(Context c, String key, String def) {
        Cursor cur = db(c).rawQuery("SELECT v FROM meta WHERE k=?", a(key));
        try {
            return cur.moveToFirst() ? cur.getString(0) : def;
        } finally {
            cur.close();
        }
    }

    public static void setMeta(Context c, String key, String value) {
        ContentValues v = new ContentValues();
        v.put("k", key);
        v.put("v", value);
        db(c).insertWithOnConflict("meta", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // ------------------------------------------------------------------ usuários
    static User user(Cursor c) {
        User u = new User();
        u.id = l(c, "id");
        u.role = s(c, "role");
        u.name = s(c, "name");
        u.email = s(c, "email");
        u.university = s(c, "university");
        u.course = s(c, "course");
        u.semester = s(c, "semester");
        u.phone = s(c, "phone");
        u.linkedin = s(c, "linkedin");
        u.about = s(c, "about");
        u.skills = s(c, "skills");
        u.cvUri = s(c, "cv_uri");
        u.cvName = s(c, "cv_name");
        u.cvSize = l(c, "cv_size");
        u.cvDate = l(c, "cv_date");
        u.orgName = s(c, "org_name");
        u.orgType = s(c, "org_type");
        u.initials = s(c, "initials");
        u.cnpj = s(c, "cnpj");
        u.sector = s(c, "sector");
        u.location = s(c, "location");
        u.responsible = s(c, "responsible");
        u.responsibleRole = s(c, "responsible_role");
        u.status = s(c, "status");
        u.gradYear = n(c, "grad_year");
        u.createdAt = l(c, "created_at");
        return u;
    }

    private static ContentValues values(User u) {
        ContentValues v = new ContentValues();
        v.put("role", u.role);
        v.put("name", u.name);
        v.put("email", u.email.trim());
        v.put("university", u.university);
        v.put("course", u.course);
        v.put("semester", u.semester);
        v.put("phone", u.phone);
        v.put("linkedin", u.linkedin);
        v.put("about", u.about);
        v.put("skills", u.skills);
        v.put("cv_uri", u.cvUri);
        v.put("cv_name", u.cvName);
        v.put("cv_size", u.cvSize);
        v.put("cv_date", u.cvDate);
        v.put("org_name", u.orgName);
        v.put("org_type", u.orgType);
        v.put("initials", u.initials);
        v.put("cnpj", u.cnpj);
        v.put("sector", u.sector);
        v.put("location", u.location);
        v.put("responsible", u.responsible);
        v.put("responsible_role", u.responsibleRole);
        v.put("status", u.status);
        v.put("grad_year", u.gradYear);
        return v;
    }

    public static User getUser(Context c, long id) {
        Cursor cur = db(c).rawQuery("SELECT * FROM users WHERE id=?", a(id));
        try {
            return cur.moveToFirst() ? user(cur) : null;
        } finally {
            cur.close();
        }
    }

    public static User findByEmail(Context c, String email) {
        Cursor cur = db(c).rawQuery("SELECT * FROM users WHERE email=? COLLATE NOCASE", a(email.trim()));
        try {
            return cur.moveToFirst() ? user(cur) : null;
        } finally {
            cur.close();
        }
    }

    public static boolean emailTaken(Context c, String email, long exceptId) {
        return scalar(c, "SELECT COUNT(*) FROM users WHERE email=? COLLATE NOCASE AND id<>?", email.trim(), String.valueOf(exceptId)) > 0;
    }

    /** Retorna o usuário se e-mail e senha conferem, senão null. */
    public static User login(Context c, String email, String pass) {
        Cursor cur = db(c).rawQuery("SELECT * FROM users WHERE email=? COLLATE NOCASE", a(email.trim()));
        try {
            if (!cur.moveToFirst()) return null;
            String salt = s(cur, "salt");
            String stored = s(cur, "pass");
            if (stored.isEmpty() || !Security.hash(salt, pass).equals(stored)) return null;
            return user(cur);
        } finally {
            cur.close();
        }
    }

    /** Cria a conta. Retorna o id ou -1 se o e-mail já existe. */
    public static long register(Context c, User u, String pass) {
        if (emailTaken(c, u.email, 0)) return -1;
        ContentValues v = values(u);
        String salt = Security.newSalt();
        v.put("salt", salt);
        v.put("pass", pass.isEmpty() ? "" : Security.hash(salt, pass));
        v.put("created_at", System.currentTimeMillis());
        if (u.isOrg() && u.initials.isEmpty()) v.put("initials", Fmt.initialsOrg(u.orgName));
        long id = db(c).insert("users", null, v);
        if (id > 0) {
            u.id = id;
            notice(c, id, "Bem-vindo ao EstágioPro", u.isOrg()
                    ? "Complete o perfil da organização e publique sua primeira vaga."
                    : "Complete seu perfil e candidate-se às primeiras vagas.");
        }
        return id;
    }

    public static void updateUser(Context c, User u) {
        db(c).update("users", values(u), "id=?", a(u.id));
    }

    public static boolean changePassword(Context c, long id, String oldPass, String newPass) {
        Cursor cur = db(c).rawQuery("SELECT salt, pass FROM users WHERE id=?", a(id));
        try {
            if (!cur.moveToFirst()) return false;
            String stored = cur.getString(1);
            if (stored != null && !stored.isEmpty() && !Security.hash(cur.getString(0), oldPass).equals(stored)) return false;
        } finally {
            cur.close();
        }
        setPassword(c, id, newPass);
        return true;
    }

    public static void setPassword(Context c, long id, String newPass) {
        String salt = Security.newSalt();
        ContentValues v = new ContentValues();
        v.put("salt", salt);
        v.put("pass", Security.hash(salt, newPass));
        db(c).update("users", v, "id=?", a(id));
    }

    // ------------------------------------------------------------------ alunos (instituição)
    private static String studentWhere(String status, String q, List<String> args) {
        StringBuilder w = new StringBuilder(" WHERE role='aluno'");
        if (status != null) {
            w.append(" AND status=?");
            args.add(status);
        }
        if (q != null && !q.trim().isEmpty()) {
            w.append(" AND (name LIKE ? OR course LIKE ? OR university LIKE ? OR skills LIKE ?)");
            String like = "%" + q.trim() + "%";
            for (int i = 0; i < 4; i++) args.add(like);
        }
        return w.toString();
    }

    public static List<User> students(Context c, String status, String q, int limit) {
        List<String> args = new ArrayList<>();
        String sql = "SELECT * FROM users" + studentWhere(status, q, args) + " ORDER BY id ASC" + (limit > 0 ? " LIMIT " + limit : "");
        Cursor cur = db(c).rawQuery(sql, args.toArray(new String[0]));
        List<User> out = new ArrayList<>();
        try {
            while (cur.moveToNext()) out.add(user(cur));
        } finally {
            cur.close();
        }
        return out;
    }

    public static int countStudents(Context c, String status, String q) {
        List<String> args = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM users" + studentWhere(status, q, args);
        return (int) scalar(c, sql, args.toArray(new String[0]));
    }

    public static void setStudentStatus(Context c, long studentId, String status) {
        ContentValues v = new ContentValues();
        v.put("status", status);
        db(c).update("users", v, "id=?", a(studentId));
    }

    // ------------------------------------------------------------------ vagas
    private static final String JOB_SEL = "SELECT j.*, "
            + "(SELECT COUNT(*) FROM applications x WHERE x.job_id=j.id) AS n_apps, "
            + "(SELECT COUNT(*) FROM saved sv WHERE sv.job_id=j.id) AS n_saved FROM jobs j";

    static Job job(Cursor c) {
        Job j = new Job();
        j.id = l(c, "id");
        j.ownerId = l(c, "owner_id");
        j.companyName = s(c, "company_name");
        j.title = s(c, "title");
        j.alias = s(c, "alias");
        j.area = s(c, "area");
        j.location = s(c, "location");
        j.mode = s(c, "mode");
        j.type = s(c, "type");
        j.salary = s(c, "salary");
        j.hours = s(c, "hours");
        j.description = s(c, "description");
        j.requirements = s(c, "requirements");
        j.status = s(c, "status");
        j.source = s(c, "source");
        j.views = n(c, "views");
        j.createdAt = l(c, "created_at");
        j.applicants = n(c, "n_apps");
        j.saved = n(c, "n_saved");
        return j;
    }

    public static Job getJob(Context c, long id) {
        Cursor cur = db(c).rawQuery(JOB_SEL + " WHERE j.id=?", a(id));
        try {
            return cur.moveToFirst() ? job(cur) : null;
        } finally {
            cur.close();
        }
    }

    private static List<Job> jobs(Context c, String sql, String... args) {
        Cursor cur = db(c).rawQuery(sql, args);
        List<Job> out = new ArrayList<>();
        try {
            while (cur.moveToNext()) out.add(job(cur));
        } finally {
            cur.close();
        }
        return out;
    }

    /** Vagas abertas para o aluno, com busca, filtro de modalidade e favoritos. */
    public static List<Job> searchJobs(Context c, final User student, String q, String mode,
                                       boolean onlySaved, boolean hideApplied, boolean byMatch) {
        List<String> args = new ArrayList<>();
        StringBuilder w = new StringBuilder(" WHERE j.status='Ativa'");
        if (mode != null) {
            w.append(" AND j.mode=?");
            args.add(mode);
        }
        if (q != null && !q.trim().isEmpty()) {
            w.append(" AND (j.title LIKE ? OR j.company_name LIKE ? OR j.area LIKE ? OR j.location LIKE ? OR j.requirements LIKE ?)");
            String like = "%" + q.trim() + "%";
            for (int i = 0; i < 5; i++) args.add(like);
        }
        if (onlySaved) {
            w.append(" AND j.id IN (SELECT job_id FROM saved WHERE student_id=?)");
            args.add(String.valueOf(student.id));
        }
        if (hideApplied) {
            w.append(" AND j.id NOT IN (SELECT job_id FROM applications WHERE student_id=?)");
            args.add(String.valueOf(student.id));
        }
        List<Job> list = jobs(c, JOB_SEL + w + " ORDER BY j.created_at DESC", args.toArray(new String[0]));
        if (byMatch) {
            final Map<Long, Integer> m = new HashMap<>();
            for (Job j : list) m.put(j.id, matchPct(student, j));
            Collections.sort(list, new Comparator<Job>() {
                @Override
                public int compare(Job x, Job y) {
                    int d = m.get(y.id) - m.get(x.id);
                    return d != 0 ? d : Long.compare(y.createdAt, x.createdAt);
                }
            });
        }
        return list;
    }

    /** As melhores vagas para o aluno (tela inicial). */
    public static List<Job> recommended(Context c, User student, int limit) {
        List<Job> all = searchJobs(c, student, null, null, false, false, true);
        List<Job> out = new ArrayList<>();
        for (Job j : all) {
            if (out.size() >= limit) break;
            if (Job.SRC_EMPRESA.equals(j.source)) out.add(j);
        }
        return out;
    }

    public static int openCompanyJobs(Context c) {
        return (int) scalar(c, "SELECT COUNT(*) FROM jobs WHERE status='Ativa' AND source='empresa'");
    }

    private static String in(String[] statuses, List<String> args, String col) {
        StringBuilder sb = new StringBuilder(col + " IN (");
        for (int i = 0; i < statuses.length; i++) {
            if (i > 0) sb.append(',');
            sb.append('?');
            args.add(statuses[i]);
        }
        return sb.append(')').toString();
    }

    public static List<Job> jobsByOwner(Context c, long ownerId, String... statuses) {
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(ownerId));
        String cond = in(statuses, args, "j.status");
        return jobs(c, JOB_SEL + " WHERE j.owner_id=? AND " + cond + " ORDER BY j.created_at DESC, j.id ASC",
                args.toArray(new String[0]));
    }

    public static int countJobs(Context c, long ownerId, String... statuses) {
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(ownerId));
        String cond = in(statuses, args, "status");
        return (int) scalar(c, "SELECT COUNT(*) FROM jobs WHERE owner_id=? AND " + cond, args.toArray(new String[0]));
    }

    public static long saveJob(Context c, Job j) {
        ContentValues v = new ContentValues();
        v.put("owner_id", j.ownerId);
        v.put("company_name", j.companyName);
        v.put("title", j.title);
        v.put("alias", j.alias);
        v.put("area", j.area);
        v.put("location", j.location);
        v.put("mode", j.mode);
        v.put("type", j.type);
        v.put("salary", j.salary);
        v.put("hours", j.hours);
        v.put("description", j.description);
        v.put("requirements", j.requirements);
        v.put("status", j.status);
        v.put("source", j.source);
        v.put("views", j.views);
        if (j.id == 0) {
            v.put("created_at", System.currentTimeMillis());
            j.id = db(c).insert("jobs", null, v);
        } else {
            db(c).update("jobs", v, "id=?", a(j.id));
        }
        return j.id;
    }

    /** Atualiza o nome da empresa nas vagas que ela publicou. */
    public static void renameCompany(Context c, long ownerId, String name) {
        ContentValues v = new ContentValues();
        v.put("company_name", name);
        db(c).update("jobs", v, "owner_id=? AND source=?", a(ownerId, Job.SRC_EMPRESA));
    }

    public static void setJobStatus(Context c, long jobId, String status) {
        ContentValues v = new ContentValues();
        v.put("status", status);
        db(c).update("jobs", v, "id=?", a(jobId));
    }

    public static void deleteJob(Context c, long jobId) {
        SQLiteDatabase d = db(c);
        d.beginTransaction();
        try {
            d.delete("applications", "job_id=?", a(jobId));
            d.delete("saved", "job_id=?", a(jobId));
            d.delete("jobs", "id=?", a(jobId));
            d.setTransactionSuccessful();
        } finally {
            d.endTransaction();
        }
    }

    public static void addView(Context c, long jobId) {
        db(c).execSQL("UPDATE jobs SET views = views + 1 WHERE id=?", a(jobId));
    }

    // ------------------------------------------------------------------ compatibilidade
    public static int matchPct(User student, Job job) {
        List<String> tags = job.tags();
        if (tags.isEmpty()) return 70;
        List<String> skills = student.skillList();
        int hit = 0;
        for (String t : tags) {
            String tl = t.toLowerCase();
            for (String k : skills) {
                String kl = k.toLowerCase();
                if (kl.equals(tl) || kl.contains(tl) || tl.contains(kl)) {
                    hit++;
                    break;
                }
            }
        }
        return Math.min(99, 40 + Math.round(60f * hit / tags.size()));
    }

    // ------------------------------------------------------------------ candidaturas
    static Candidatura app(Cursor c) {
        Candidatura x = new Candidatura();
        x.id = l(c, "id");
        x.jobId = l(c, "job_id");
        x.studentId = l(c, "student_id");
        x.stage = n(c, "stage");
        x.interviewAt = l(c, "interview_at");
        x.favorite = n(c, "favorite") == 1;
        x.match = n(c, "match_pct");
        x.referred = n(c, "referred") == 1;
        x.createdAt = l(c, "created_at");
        x.updatedAt = l(c, "updated_at");
        return x;
    }

    private static void fill(Context c, List<Candidatura> list, boolean withJob, boolean withStudent) {
        Map<Long, Job> jobs = new HashMap<>();
        Map<Long, User> users = new HashMap<>();
        for (Candidatura x : list) {
            if (withJob) {
                if (!jobs.containsKey(x.jobId)) jobs.put(x.jobId, getJob(c, x.jobId));
                x.job = jobs.get(x.jobId);
            }
            if (withStudent) {
                if (!users.containsKey(x.studentId)) users.put(x.studentId, getUser(c, x.studentId));
                x.student = users.get(x.studentId);
            }
        }
    }

    private static List<Candidatura> apps(Context c, String sql, boolean withJob, boolean withStudent, String... args) {
        Cursor cur = db(c).rawQuery(sql, args);
        List<Candidatura> out = new ArrayList<>();
        try {
            while (cur.moveToNext()) out.add(app(cur));
        } finally {
            cur.close();
        }
        fill(c, out, withJob, withStudent);
        return out;
    }

    public static Candidatura getApp(Context c, long id) {
        List<Candidatura> l = apps(c, "SELECT * FROM applications WHERE id=?", true, true, a(id));
        return l.isEmpty() ? null : l.get(0);
    }

    public static Candidatura appFor(Context c, long studentId, long jobId) {
        List<Candidatura> l = apps(c, "SELECT * FROM applications WHERE student_id=? AND job_id=?", true, true, a(studentId, jobId));
        return l.isEmpty() ? null : l.get(0);
    }

    /** group: "andamento", "finalizadas" ou "todas". */
    public static List<Candidatura> appsForStudent(Context c, long studentId, String group) {
        String extra = "";
        if ("andamento".equals(group)) extra = " AND stage<=2";
        else if ("finalizadas".equals(group)) extra = " AND stage>=3";
        return apps(c, "SELECT * FROM applications WHERE student_id=?" + extra + " ORDER BY updated_at DESC",
                true, false, a(studentId));
    }

    /** tab: "novos", "favoritos" ou "entrevistas" (somente vagas ativas do dono). */
    public static List<Candidatura> appsForOwner(Context c, long ownerId, String tab, String q) {
        String extra = "";
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(ownerId));
        if ("favoritos".equals(tab)) {
            extra = " AND a.favorite=1";
        } else if ("entrevistas".equals(tab)) {
            extra = " AND a.stage=2";
        } else {
            extra = " AND a.stage<=2 AND a.created_at>=?";
            args.add(String.valueOf(System.currentTimeMillis() - 14 * Fmt.DAY));
        }
        List<Candidatura> all = apps(c, "SELECT a.* FROM applications a JOIN jobs j ON j.id=a.job_id "
                        + "WHERE j.owner_id=? AND j.status='Ativa'" + extra + " ORDER BY j.id ASC, a.match_pct DESC",
                true, true, args.toArray(new String[0]));
        if (q == null || q.trim().isEmpty()) return all;
        String needle = q.trim().toLowerCase();
        List<Candidatura> out = new ArrayList<>();
        for (Candidatura x : all) {
            User u = x.student;
            if (u == null) continue;
            String hay = (u.name + " " + u.course + " " + u.university + " " + u.skills).toLowerCase();
            if (hay.contains(needle)) out.add(x);
        }
        return out;
    }

    public static List<Candidatura> appsForJob(Context c, long jobId) {
        return apps(c, "SELECT * FROM applications WHERE job_id=? ORDER BY match_pct DESC, created_at DESC",
                true, true, a(jobId));
    }

    /** Cria a candidatura. Retorna o id ou -1 se já existe. */
    public static long apply(Context c, long studentId, long jobId) {
        Job j = getJob(c, jobId);
        User st = getUser(c, studentId);
        if (j == null || st == null || appFor(c, studentId, jobId) != null) return -1;
        long now = System.currentTimeMillis();
        ContentValues v = new ContentValues();
        v.put("job_id", jobId);
        v.put("student_id", studentId);
        v.put("stage", Candidatura.ENVIADA);
        v.put("match_pct", matchPct(st, j));
        v.put("created_at", now);
        v.put("updated_at", now);
        long id = db(c).insert("applications", null, v);
        if (id > 0) {
            notice(c, j.ownerId, "Nova candidatura", Fmt.shortName(st.name) + " se candidatou a " + j.title + ".");
            notice(c, studentId, "Candidatura enviada", "Sua candidatura para " + j.title + " foi enviada com sucesso.");
        }
        return id;
    }

    public static void withdraw(Context c, long appId) {
        db(c).delete("applications", "id=?", a(appId));
    }

    public static boolean toggleFavorite(Context c, long appId) {
        Candidatura x = getApp(c, appId);
        if (x == null) return false;
        ContentValues v = new ContentValues();
        v.put("favorite", x.favorite ? 0 : 1);
        db(c).update("applications", v, "id=?", a(appId));
        return !x.favorite;
    }

    /** Muda a etapa e avisa o aluno. */
    public static void setStage(Context c, long appId, int stage, long interviewAt) {
        Candidatura x = getApp(c, appId);
        if (x == null || x.job == null) return;
        ContentValues v = new ContentValues();
        v.put("stage", stage);
        v.put("updated_at", System.currentTimeMillis());
        if (stage == Candidatura.ENTREVISTA) v.put("interview_at", interviewAt);
        db(c).update("applications", v, "id=?", a(appId));
        if (x.stage == Candidatura.CONTRATADO && stage != Candidatura.CONTRATADO
                && x.student != null && User.ST_CONT.equals(x.student.status)) {
            setStudentStatus(c, x.studentId, User.ST_DISP);
        }
        String org = Fmt.shortCompany(x.job.companyName);
        String title = x.job.title;
        switch (stage) {
            case Candidatura.ANALISE:
                notice(c, x.studentId, "Currículo em análise", org + " está analisando seu currículo para " + title + ".");
                break;
            case Candidatura.ENTREVISTA:
                notice(c, x.studentId, "Entrevista agendada", org + " · " + title + " — " + Fmt.interview(interviewAt).toLowerCase() + ".");
                break;
            case Candidatura.CONTRATADO:
                setStudentStatus(c, x.studentId, User.ST_CONT);
                notice(c, x.studentId, "Parabéns! Você foi selecionado(a)", org + " escolheu você para " + title + ".");
                break;
            case Candidatura.RECUSADO:
                notice(c, x.studentId, "Processo encerrado", org + " encerrou o processo de " + title + ". Continue buscando novas vagas!");
                break;
            default:
                break;
        }
    }

    /** Instituição encaminha um aluno para uma vaga. Retorna o id ou -1 se já existe. */
    public static long refer(Context c, long studentId, long jobId, String byName) {
        Job j = getJob(c, jobId);
        User st = getUser(c, studentId);
        if (j == null || st == null || appFor(c, studentId, jobId) != null) return -1;
        long now = System.currentTimeMillis();
        ContentValues v = new ContentValues();
        v.put("job_id", jobId);
        v.put("student_id", studentId);
        v.put("stage", Candidatura.ENVIADA);
        v.put("match_pct", matchPct(st, j));
        v.put("referred", 1);
        v.put("created_at", now);
        v.put("updated_at", now);
        long id = db(c).insert("applications", null, v);
        if (id > 0) {
            if (User.ST_DISP.equals(st.status)) setStudentStatus(c, studentId, User.ST_ENC);
            notice(c, studentId, "Você foi encaminhado(a)", byName + " encaminhou seu perfil para " + j.title + " (" + Fmt.shortCompany(j.companyName) + ").");
            notice(c, j.ownerId, "Candidato encaminhado", byName + " encaminhou " + Fmt.shortName(st.name) + " para " + j.title + ".");
        }
        return id;
    }

    // ------------------------------------------------------------------ favoritos (vagas salvas)
    public static boolean toggleSaved(Context c, long studentId, long jobId) {
        SQLiteDatabase d = db(c);
        if (isSaved(c, studentId, jobId)) {
            d.delete("saved", "student_id=? AND job_id=?", a(studentId, jobId));
            return false;
        }
        ContentValues v = new ContentValues();
        v.put("student_id", studentId);
        v.put("job_id", jobId);
        d.insertWithOnConflict("saved", null, v, SQLiteDatabase.CONFLICT_IGNORE);
        return true;
    }

    public static boolean isSaved(Context c, long studentId, long jobId) {
        return scalar(c, "SELECT COUNT(*) FROM saved WHERE student_id=? AND job_id=?", String.valueOf(studentId), String.valueOf(jobId)) > 0;
    }

    public static Set<Long> savedIds(Context c, long studentId) {
        Cursor cur = db(c).rawQuery("SELECT job_id FROM saved WHERE student_id=?", a(studentId));
        Set<Long> out = new HashSet<>();
        try {
            while (cur.moveToNext()) out.add(cur.getLong(0));
        } finally {
            cur.close();
        }
        return out;
    }

    // ------------------------------------------------------------------ estatísticas
    /** [ativas, entrevistas, concluídas] */
    public static int[] studentStats(Context c, long studentId) {
        String id = String.valueOf(studentId);
        return new int[]{
                (int) scalar(c, "SELECT COUNT(*) FROM applications WHERE student_id=? AND stage<=2", id),
                (int) scalar(c, "SELECT COUNT(*) FROM applications WHERE student_id=? AND stage=2", id),
                (int) scalar(c, "SELECT COUNT(*) FROM applications WHERE student_id=? AND stage>=3", id)};
    }

    /** [inscritos, em análise, entrevistas] nas vagas ativas do dono. */
    public static int[] ownerStats(Context c, long ownerId) {
        String id = String.valueOf(ownerId);
        String base = "SELECT COUNT(*) FROM applications a JOIN jobs j ON j.id=a.job_id WHERE j.owner_id=? AND j.status='Ativa'";
        return new int[]{
                (int) scalar(c, base, id),
                (int) scalar(c, base + " AND a.stage=1", id),
                (int) scalar(c, base + " AND a.stage=2", id)};
    }

    /** [vagas ativas, candidatos, em análise, entrevistas] */
    public static int[] companyPanel(Context c, long ownerId) {
        int[] s = ownerStats(c, ownerId);
        return new int[]{countJobs(c, ownerId, Job.ATIVA, Job.PAUSADA), s[0], s[1], s[2]};
    }

    public static int graduates(Context c) {
        return (int) scalar(c, "SELECT COUNT(*) FROM users WHERE role='aluno' AND grad_year=?", getMeta(c, "ano_formandos", "2025"));
    }

    public static int employabilityRate(Context c) {
        int total = graduates(c);
        if (total == 0) return 0;
        long hired = scalar(c, "SELECT COUNT(*) FROM users WHERE role='aluno' AND grad_year=? AND status='Contratado'",
                getMeta(c, "ano_formandos", "2025"));
        return Math.round(hired * 100f / total);
    }

    /** [oportunidades, alunos, empresas parceiras, empregabilidade %] */
    public static int[] institutionPanel(Context c, long instId) {
        return new int[]{
                (int) scalar(c, "SELECT COUNT(*) FROM jobs WHERE owner_id=?", String.valueOf(instId)),
                (int) scalar(c, "SELECT COUNT(*) FROM users WHERE role='aluno'"),
                (int) scalar(c, "SELECT COUNT(*) FROM users WHERE role='empresa'"),
                employabilityRate(c)};
    }

    public static int reach(Context c, long instId) {
        return (int) scalar(c, "SELECT COALESCE(SUM(views),0) FROM jobs WHERE owner_id=? AND status='Ativa'", String.valueOf(instId));
    }

    // ------------------------------------------------------------------ notificações
    public static void notice(Context c, long userId, String title, String body) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("title", title);
        v.put("body", body);
        v.put("created_at", System.currentTimeMillis());
        v.put("is_read", 0);
        db(c).insert("notices", null, v);
    }

    public static List<Notice> notices(Context c, long userId) {
        Cursor cur = db(c).rawQuery("SELECT * FROM notices WHERE user_id=? ORDER BY created_at DESC, id DESC LIMIT 100", a(userId));
        List<Notice> out = new ArrayList<>();
        try {
            while (cur.moveToNext()) {
                Notice x = new Notice();
                x.id = l(cur, "id");
                x.userId = l(cur, "user_id");
                x.title = s(cur, "title");
                x.body = s(cur, "body");
                x.createdAt = l(cur, "created_at");
                x.read = n(cur, "is_read") == 1;
                out.add(x);
            }
        } finally {
            cur.close();
        }
        return out;
    }

    public static int unread(Context c, long userId) {
        return (int) scalar(c, "SELECT COUNT(*) FROM notices WHERE user_id=? AND is_read=0", String.valueOf(userId));
    }

    public static void markAllRead(Context c, long userId) {
        ContentValues v = new ContentValues();
        v.put("is_read", 1);
        db(c).update("notices", v, "user_id=?", a(userId));
    }
}
