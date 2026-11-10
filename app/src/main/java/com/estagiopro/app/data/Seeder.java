package com.estagiopro.app.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.estagiopro.app.util.Fmt;
import com.estagiopro.app.util.Security;

import java.util.Calendar;
import java.util.Random;

/**
 * Dados de demonstração criados na primeira execução.
 * Contas de teste (senha 12345678):
 *   aluno:       ana@universidade.edu.br
 *   empresa:     talentos@lumina.com.br
 *   instituição: carreiras@universidade.edu.br
 */
final class Seeder {

    private Seeder() { }

    private static final long DAY = Fmt.DAY;
    private static final long HOUR = 60L * 60 * 1000;
    private static final String PASS = "12345678";

    private static final String[] FIRST = {"Beatriz", "Gabriel", "Larissa", "Pedro", "Camila", "Matheus", "Fernanda",
            "Lucas", "Amanda", "Thiago", "Bruna", "Felipe", "Carolina", "Gustavo", "Isabela", "Rodrigo", "Letícia",
            "Daniel", "Mariana", "Vinícius", "Juliana", "André", "Natália", "Leonardo", "Patrícia", "Henrique",
            "Aline", "Bruno", "Renata", "Eduardo", "Priscila", "Diego", "Tatiane", "Marcelo", "Vanessa", "Caio",
            "Débora", "Igor", "Sabrina", "Otávio"};
    private static final String[] LAST = {"Almeida", "Souza", "Oliveira", "Pereira", "Lima", "Costa", "Ribeiro",
            "Gomes", "Rocha", "Barbosa", "Araújo", "Cardoso", "Nogueira", "Teixeira", "Moreira", "Azevedo", "Castro",
            "Dias", "Freitas", "Monteiro", "Pinto", "Ramos", "Santana", "Vieira", "Machado", "Correia", "Mendes",
            "Batista", "Campos", "Duarte", "Fonseca", "Guimarães", "Lopes", "Medeiros", "Nunes", "Prado", "Siqueira",
            "Tavares", "Xavier", "Andrade"};
    private static final String[] COURSES = {"Engenharia de Computação", "Ciência da Computação", "Sistemas de Informação",
            "Design", "Administração", "Marketing", "Engenharia de Produção", "Análise e Desenvolvimento de Sistemas",
            "Economia", "Estatística"};
    private static final String[] UNIS = {"Universidade de São Paulo", "Mackenzie", "ESPM", "FIAP", "PUC-Campinas",
            "Unicamp", "Universidade Central", "FGV"};
    private static final String[] SKILLS = {"Figma", "React", "UX Research", "JavaScript", "Python", "Java", "SQL",
            "Excel", "Power BI", "Marketing Digital", "Photoshop", "Scrum", "Node.js", "Git", "Comunicação",
            "Liderança", "Estatística", "Redes", "Android", "Inglês B2"};

    private static Random rnd;
    private static long now;
    private static String salt;
    private static String hash;

    static void seed(SQLiteDatabase db) {
        rnd = new Random(2025);
        now = System.currentTimeMillis();
        salt = "estagiopro-demo-salt";
        hash = Security.hash(salt, PASS);

        meta(db, "ano_formandos", "2025");
        meta(db, "taxa_anterior", "59");
        meta(db, "meta_taxa", "75");
        meta(db, "meta_alcance", "1560");

        // ------------------------------------------------------------ alunos
        long ana = student(db, "Ana Luiza Ferreira", "ana@universidade.edu.br", "Universidade de São Paulo",
                "Engenharia de Computação", "6", "Figma,React,UX Research,JavaScript,Inglês B2",
                "Estudante apaixonada por tecnologia e produtos digitais, buscando experiências em design e desenvolvimento.",
                User.ST_DISP, 0);
        Calendar may = Calendar.getInstance();
        may.set(Calendar.MONTH, Calendar.MAY);
        may.set(Calendar.DAY_OF_MONTH, 10);
        ContentValues cv = new ContentValues();
        cv.put("cv_name", "Curriculo_Ana_Luiza.pdf");
        cv.put("cv_size", 1258291L);
        cv.put("cv_date", may.getTimeInMillis());
        db.update("users", cv, "id=?", new String[]{String.valueOf(ana)});
        long lucas = student(db, "Lucas Carvalho", "lucas.carvalho@universidade.edu.br", "Universidade Central",
                "Administração", "8", "Excel,Power BI,Comunicação", "", User.ST_ENC, 0);
        long marina = student(db, "Marina Borges", "marina.borges@universidade.edu.br", "Universidade Central",
                "Design", "7", "Figma,UI,Photoshop", "", User.ST_DISP, 0);
        long rafael = student(db, "Rafael Martins", "rafael.martins@universidade.edu.br", "ESPM",
                "Design Digital", "5", "UI,Figma,Inglês", "", User.ST_DISP, 0);
        long julia = student(db, "Julia Santos", "julia.santos@universidade.edu.br", "Mackenzie",
                "Sistemas de Informação", "6", "React,UX", "", User.ST_DISP, 0);

        int poolSize = 307;
        long[] pool = new long[poolSize];
        for (int i = 0; i < poolSize; i++) {
            String name = FIRST[(i * 7) % FIRST.length] + " " + LAST[(i * 11 + i / FIRST.length) % LAST.length];
            String course = COURSES[rnd.nextInt(COURSES.length)];
            String uni = UNIS[rnd.nextInt(UNIS.length)];
            StringBuilder sk = new StringBuilder();
            int k = 2 + rnd.nextInt(3);
            for (int j = 0; j < k; j++) {
                String s = SKILLS[rnd.nextInt(SKILLS.length)];
                if (sk.indexOf(s) < 0) sk.append(sk.length() > 0 ? "," : "").append(s);
            }
            String status = User.ST_DISP;
            int grad = 0;
            if (i < 84) {
                grad = 2025;
                status = i < 56 ? User.ST_CONT : (i < 70 ? User.ST_ENC : User.ST_DISP);
            } else if (i % 9 == 0) {
                status = User.ST_ENC;
            }
            pool[i] = student(db, name, "aluno" + (i + 1) + "@universidade.edu.br", uni, course,
                    String.valueOf(2 + rnd.nextInt(8)), sk.toString(),
                    "Estudante de " + course + " em busca de uma oportunidade de estágio.", status, grad);
        }

        // ------------------------------------------------------------ organizações
        long lumina = org(db, User.EMPRESA, "Lumina Tecnologia", "talentos@lumina.com.br", "LU", "Empresa", "Tecnologia",
                "São Paulo, SP", "Criamos produtos digitais que simplificam o dia a dia de milhares de pessoas. Valorizamos aprendizado e colaboração.");
        long nexora = org(db, User.EMPRESA, "Nexora", "rh@nexora.com.br", "NX", "Empresa", "Tecnologia", "Remoto",
                "Soluções em nuvem e desenvolvimento de software para empresas de todos os portes.");
        long orbit = org(db, User.EMPRESA, "Orbit Bank", "rh@orbitbank.com.br", "OB", "Empresa", "Finanças", "Campinas, SP",
                "Banco digital com foco em dados e experiência do cliente.");
        long inst = org(db, User.INSTITUICAO, "Universidade Central", "carreiras@universidade.edu.br", "UC", "Instituição",
                "Ensino superior", "Campinas, SP",
                "Formamos profissionais preparados para transformar o futuro por meio de ensino, pesquisa e conexão com o mercado.");

        String[] others = {"Vertix Software", "Atlas Health", "Kairos Energia", "Solaris Foods", "Prisma Digital",
                "Quanta Labs", "Helix Biotech", "Nimbus Cloud", "Vortex Games", "Aurora Seguros", "Delta Logística",
                "Zenith Consultoria", "Pixel Studio", "Terra Agro", "Mosaico Educação"};
        String[] sectors = {"Tecnologia", "Saúde", "Energia", "Alimentos", "Tecnologia", "Pesquisa", "Biotecnologia",
                "Tecnologia", "Games", "Seguros", "Logística", "Consultoria", "Design", "Agronegócio", "Educação"};
        long[] oth = new long[others.length];
        for (int i = 0; i < others.length; i++) {
            String slug = others[i].toLowerCase().split(" ")[0].replaceAll("[^a-z]", "");
            oth[i] = org(db, User.EMPRESA, others[i], "rh@" + slug + ".com.br", Fmt.initialsOrg(others[i]), "Empresa",
                    sectors[i], i % 2 == 0 ? "São Paulo, SP" : "Campinas, SP", "Empresa parceira do programa de estágios.");
        }
        long vertix = oth[0];
        long atlas = oth[1];

        // ------------------------------------------------------------ vagas das empresas
        long pd = job(db, lumina, "Lumina Tecnologia", "Estágio em Product Design", "", "Design", "São Paulo, SP", "Híbrido",
                "Estágio", "", "6 horas", "Figma, UX, React", Job.ATIVA, Job.SRC_EMPRESA, 146, now - 2 * DAY - HOUR);
        long dev = job(db, lumina, "Lumina Tecnologia", "Estágio em Desenvolvimento", "", "Tecnologia", "Remoto", "Remoto",
                "Estágio", "R$ 2.000", "6 horas", "JavaScript, React, Git", Job.ATIVA, Job.SRC_EMPRESA, 284, now - 4 * DAY);
        long mkt = job(db, lumina, "Lumina Tecnologia", "Estágio em Marketing", "", "Marketing", "São Paulo, SP", "Presencial",
                "Estágio", "R$ 1.800", "6 horas", "Marketing Digital, Excel, Comunicação", Job.PAUSADA, Job.SRC_EMPRESA, 98, now - 5 * DAY);
        job(db, lumina, "Lumina Tecnologia", "Estágio em Dados", "", "Tecnologia", "São Paulo, SP", "Híbrido", "Estágio",
                "R$ 2.100", "6 horas", "Python, SQL, Power BI", Job.ATIVA, Job.SRC_EMPRESA, 61, now - 6 * DAY);
        job(db, lumina, "Lumina Tecnologia", "Estágio em Suporte TI", "", "Tecnologia", "São Paulo, SP", "Presencial", "Estágio",
                "R$ 1.700", "6 horas", "Redes, Excel", Job.ATIVA, Job.SRC_EMPRESA, 44, now - 7 * DAY);
        job(db, lumina, "Lumina Tecnologia", "Estágio em Customer Success", "", "Atendimento", "Remoto", "Remoto", "Estágio",
                "R$ 1.600", "6 horas", "Comunicação, Inglês B2", Job.ATIVA, Job.SRC_EMPRESA, 37, now - 8 * DAY);
        job(db, lumina, "Lumina Tecnologia", "Estágio em QA", "", "Tecnologia", "São Paulo, SP", "Híbrido", "Estágio",
                "", "6 horas", "Java, Git", Job.RASCUNHO, Job.SRC_EMPRESA, 0, now - 9 * DAY);
        job(db, lumina, "Lumina Tecnologia", "Estágio em Recursos Humanos", "", "RH", "São Paulo, SP", "Presencial", "Estágio",
                "", "6 horas", "Comunicação, Excel", Job.RASCUNHO, Job.SRC_EMPRESA, 0, now - 10 * DAY);
        String[] closed = {"Product Owner", "Análise de Negócios", "Design Gráfico", "Financeiro", "Jurídico", "Mobile",
                "Infraestrutura", "Segurança da Informação", "Vendas", "Comunicação", "Produto", "Engenharia de Dados",
                "Administrativo", "Pesquisa de Mercado"};
        for (int i = 0; i < closed.length; i++) {
            job(db, lumina, "Lumina Tecnologia", "Estágio em " + closed[i], "", "Geral", "São Paulo, SP", "Híbrido", "Estágio",
                    "", "6 horas", "Comunicação", Job.ENCERRADA, Job.SRC_EMPRESA, 20 + rnd.nextInt(200), now - (20 + i * 3) * DAY);
        }
        long nexDev = job(db, nexora, "Nexora", "Estágio em Desenvolvimento", "", "Tecnologia", "Remoto", "Remoto", "Estágio",
                "R$ 2.200", "6 horas", "React, JavaScript, Git", Job.ATIVA, Job.SRC_EMPRESA, 120, now - 2 * DAY - 2 * HOUR);
        long orbDados = job(db, orbit, "Orbit Bank", "Estágio em Dados", "", "Dados", "Campinas, SP", "Presencial", "Estágio",
                "R$ 1.900", "6 horas", "Python, SQL, Estatística", Job.ATIVA, Job.SRC_EMPRESA, 90, now - 3 * DAY);
        long atlasMkt = job(db, atlas, "Atlas Health", "Estágio em Marketing", "", "Marketing", "São Paulo, SP", "Presencial",
                "Estágio", "R$ 1.800", "6 horas", "Marketing Digital, Comunicação", Job.ATIVA, Job.SRC_EMPRESA, 70, now - 3 * DAY - 12 * HOUR);
        job(db, vertix, "Vertix Software", "Estágio em Desenvolvimento Mobile", "", "Tecnologia", "Remoto", "Remoto", "Estágio",
                "R$ 2.300", "6 horas", "Android, Java, Git", Job.ATIVA, Job.SRC_EMPRESA, 55, now - 5 * DAY);
        job(db, oth[2], "Kairos Energia", "Estágio em Engenharia de Produção", "", "Engenharia", "Campinas, SP", "Presencial", "Estágio",
                "R$ 1.900", "6 horas", "Excel, Estatística", Job.ATIVA, Job.SRC_EMPRESA, 33, now - 6 * DAY - 2 * HOUR);
        job(db, oth[3], "Solaris Foods", "Estágio em Finanças", "", "Finanças", "São Paulo, SP", "Híbrido", "Estágio",
                "R$ 1.700", "6 horas", "Excel, Power BI", Job.ATIVA, Job.SRC_EMPRESA, 29, now - 7 * DAY - 3 * HOUR);
        job(db, oth[4], "Prisma Digital", "Estágio em UX Design", "", "Design", "São Paulo, SP", "Híbrido", "Estágio",
                "R$ 2.000", "6 horas", "Figma, UX Research, UI", Job.ATIVA, Job.SRC_EMPRESA, 48, now - 8 * DAY - 5 * HOUR);
        long vertixClosed = job(db, vertix, "Vertix Software", "Estágio em Suporte", "", "Tecnologia", "Remoto", "Remoto", "Estágio",
                "R$ 1.500", "6 horas", "Redes", Job.ENCERRADA, Job.SRC_EMPRESA, 80, now - 40 * DAY);

        // ------------------------------------------------------------ candidaturas da Ana
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 14);
        tomorrow.set(Calendar.MINUTE, 30);
        tomorrow.set(Calendar.SECOND, 0);
        app(db, pd, ana, 2, tomorrow.getTimeInMillis(), true, 94, false, now - 2 * DAY, now - 3 * HOUR);
        app(db, nexDev, ana, 1, 0, false, 82, false, now - 4 * DAY, now - 2 * DAY);
        Calendar may12 = Calendar.getInstance();
        may12.set(Calendar.MONTH, Calendar.MAY);
        may12.set(Calendar.DAY_OF_MONTH, 12);
        app(db, orbDados, ana, 0, 0, false, 60, false, may12.getTimeInMillis(), now - 3 * DAY);
        app(db, atlasMkt, ana, 2, now + 3 * DAY, false, 55, false, now - 10 * DAY, now - 4 * DAY);
        app(db, vertixClosed, ana, 4, 0, false, 48, false, now - 30 * DAY, now - 6 * DAY);

        // ------------------------------------------------------------ candidaturas da Lumina (48 em vagas ativas + 12 pausada)
        int idx = 1;
        app(db, pd, rafael, 0, 0, false, 89, false, now - 2 * DAY, now - 2 * DAY);
        app(db, pd, julia, 0, 0, false, 86, false, now - 3 * DAY, now - 3 * DAY);
        idx = 3;
        for (int i = 0; i < 15; i++) {
            idx = pdApp(db, pd, pool[i], idx);
        }
        for (int i = 0; i < 30; i++) {
            idx = pdApp(db, dev, pool[15 + i], idx);
        }
        for (int i = 0; i < 12; i++) {
            app(db, mkt, pool[45 + i], 0, 0, false, 50 + rnd.nextInt(30), false, now - (1 + rnd.nextInt(12)) * DAY, now - DAY);
        }

        // ------------------------------------------------------------ oportunidades da instituição (24)
        String[][] featured = {
                {"Lumina Tecnologia", "Programa de Estágio 2025", "Programa de Estágio Lumina", "Engenharias e Design", "42", "320"},
                {"Nexora", "Trainee em Tecnologia", "Trainee Nexora 2025", "Computação e Sistemas", "68", "410"},
                {"Orbit Bank", "Estágio em Dados", "", "Estatística e Computação", "31", "198"}};
        int start = 60;
        for (int i = 0; i < featured.length; i++) {
            long jid = job(db, inst, featured[i][0], featured[i][1], featured[i][2], featured[i][3], "Campinas, SP", "Híbrido",
                    "Estágio", "", "6 horas", "Comunicação, Excel", Job.ATIVA, Job.SRC_INSTITUICAO, Integer.parseInt(featured[i][5]),
                    now - (10 + i) * DAY);
            interested(db, jid, pool, start, Integer.parseInt(featured[i][4]));
            start += 70;
        }
        String[] oppCompanies = {"Vertix Software", "Atlas Health", "Kairos Energia", "Solaris Foods", "Prisma Digital",
                "Quanta Labs", "Helix Biotech", "Nimbus Cloud", "Vortex Games", "Aurora Seguros", "Delta Logística",
                "Zenith Consultoria", "Pixel Studio", "Terra Agro", "Mosaico Educação", "Lumina Tecnologia", "Nexora",
                "Orbit Bank", "Atlas Health", "Vertix Software", "Prisma Digital", "Quanta Labs"};
        String[] oppAreas = {"Computação", "Saúde", "Engenharias", "Administração", "Design", "Pesquisa", "Biologia",
                "Computação", "Design e Games", "Finanças", "Logística", "Consultoria", "Design", "Agronomia",
                "Educação", "Marketing", "Dados", "Finanças", "Comunicação", "Mobile", "UX", "Dados"};
        for (int i = 0; i < 21; i++) {
            String status = i < 15 ? Job.ATIVA : (i < 19 ? Job.REVISAO : Job.EXPIRADA);
            int views = i < 14 ? 21 : (i == 14 ? 26 : 10 + rnd.nextInt(40));
            long jid = job(db, inst, oppCompanies[i], "Programa de Estágio " + oppAreas[i], "", oppAreas[i], "Campinas, SP", "Híbrido",
                    "Estágio", "", "6 horas", "Comunicação", status, Job.SRC_INSTITUICAO, views, now - (14 + i) * DAY);
            interested(db, jid, pool, 5 + i * 13, 3 + rnd.nextInt(12));
        }

        // ------------------------------------------------------------ notificações
        notice(db, ana, "Entrevista agendada", "Lumina · Estágio em Product Design — amanhã, 14:30.", now - HOUR, false);
        notice(db, ana, "Currículo em análise", "A Nexora está analisando seu currículo para Estágio em Desenvolvimento.", now - 2 * DAY, true);
        notice(db, ana, "Bem-vindo ao EstágioPro", "Complete seu perfil e candidate-se às primeiras vagas.", now - 6 * DAY, true);
        notice(db, lumina, "Nova candidatura", "Rafael Martins se candidatou a Estágio em Product Design.", now - 3 * HOUR, false);
        notice(db, lumina, "Nova candidatura", "Julia Santos se candidatou a Estágio em Product Design.", now - DAY, false);
        notice(db, lumina, "Perfil verificado", "O perfil da Lumina Tecnologia foi verificado.", now - 20 * DAY, true);
        notice(db, inst, "Oportunidade em revisão", "Há oportunidades aguardando revisão na sua curadoria.", now - 5 * HOUR, false);
        notice(db, inst, "Aluno contratado", "Mais um aluno foi contratado através das vagas da curadoria.", now - 4 * DAY, true);
    }

    /** Distribui etapas das candidaturas das vagas da Lumina para chegar a 5 entrevistas e 12 em análise. */
    private static int pdApp(SQLiteDatabase db, long jobId, long studentId, int idx) {
        int stage = 0;
        long interview = 0;
        if (idx >= 3 && idx <= 6) {
            stage = 2;
            interview = now + (1 + rnd.nextInt(7)) * DAY;
        } else if (idx >= 7 && idx <= 18) {
            stage = 1;
        }
        long created = now - (1 + rnd.nextInt(11)) * DAY;
        app(db, jobId, studentId, stage, interview, idx == 3 || idx == 7, 55 + rnd.nextInt(30), false, created, created + HOUR);
        return idx + 1;
    }

    private static void interested(SQLiteDatabase db, long jobId, long[] pool, int start, int count) {
        for (int i = 0; i < count; i++) {
            long sid = pool[(start + i) % pool.length];
            long created = now - (1 + rnd.nextInt(20)) * DAY;
            app(db, jobId, sid, 0, 0, false, 50 + rnd.nextInt(40), false, created, created);
        }
    }

    // ------------------------------------------------------------------ inserts
    private static void meta(SQLiteDatabase db, String k, String v) {
        ContentValues c = new ContentValues();
        c.put("k", k);
        c.put("v", v);
        db.insert("meta", null, c);
    }

    private static long student(SQLiteDatabase db, String name, String email, String uni, String course, String sem,
                                String skills, String about, String status, int grad) {
        ContentValues v = new ContentValues();
        v.put("role", User.ALUNO);
        v.put("name", name);
        v.put("email", email);
        v.put("salt", salt);
        v.put("pass", hash);
        v.put("university", uni);
        v.put("course", course);
        v.put("semester", sem);
        v.put("skills", skills);
        v.put("about", about);
        v.put("status", status);
        v.put("grad_year", grad);
        v.put("created_at", now - 30 * DAY);
        return db.insert("users", null, v);
    }

    private static long org(SQLiteDatabase db, String role, String name, String email, String initials, String type,
                            String sector, String location, String about) {
        ContentValues v = new ContentValues();
        v.put("role", role);
        v.put("email", email);
        v.put("salt", salt);
        v.put("pass", hash);
        v.put("org_name", name);
        v.put("org_type", type);
        v.put("initials", initials);
        v.put("cnpj", "12.345.678/0001-90");
        v.put("sector", sector);
        v.put("location", location);
        v.put("about", about);
        v.put("responsible", "Mariana Costa");
        v.put("responsible_role", "Gestora de talentos · Administradora");
        v.put("created_at", now - 60 * DAY);
        return db.insert("users", null, v);
    }

    private static long job(SQLiteDatabase db, long owner, String company, String title, String alias, String area,
                            String location, String mode, String type, String salary, String hours, String reqs,
                            String status, String source, int views, long created) {
        ContentValues v = new ContentValues();
        v.put("owner_id", owner);
        v.put("company_name", company);
        v.put("title", title);
        v.put("alias", alias);
        v.put("area", area);
        v.put("location", location);
        v.put("mode", mode);
        v.put("type", type);
        v.put("salary", salary);
        v.put("hours", hours);
        v.put("description", "A " + company + " está em busca de estudantes para atuar como " + title.toLowerCase()
                + ". Você vai participar de projetos reais, aprender com profissionais experientes e contar com o "
                + "acompanhamento de um mentor.\n\nBenefícios: bolsa-auxílio, vale-transporte e recesso remunerado.");
        v.put("requirements", reqs);
        v.put("status", status);
        v.put("source", source);
        v.put("views", views);
        v.put("created_at", created);
        return db.insert("jobs", null, v);
    }

    private static void app(SQLiteDatabase db, long jobId, long studentId, int stage, long interview, boolean fav,
                            int match, boolean referred, long created, long updated) {
        ContentValues v = new ContentValues();
        v.put("job_id", jobId);
        v.put("student_id", studentId);
        v.put("stage", stage);
        v.put("interview_at", interview);
        v.put("favorite", fav ? 1 : 0);
        v.put("match_pct", match);
        v.put("referred", referred ? 1 : 0);
        v.put("created_at", created);
        v.put("updated_at", updated);
        db.insertWithOnConflict("applications", null, v, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private static void notice(SQLiteDatabase db, long user, String title, String body, long at, boolean read) {
        ContentValues v = new ContentValues();
        v.put("user_id", user);
        v.put("title", title);
        v.put("body", body);
        v.put("created_at", at);
        v.put("is_read", read ? 1 : 0);
        db.insert("notices", null, v);
    }
}
