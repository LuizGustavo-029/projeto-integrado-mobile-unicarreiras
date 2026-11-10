package com.estagiopro.app.data;

import com.estagiopro.app.util.Fmt;

import java.util.ArrayList;
import java.util.List;

/** Conta do app: aluno, empresa ou instituição. */
public class User {
    public static final String ALUNO = "aluno";
    public static final String EMPRESA = "empresa";
    public static final String INSTITUICAO = "instituicao";

    public static final String ST_DISP = "Disponível";
    public static final String ST_ENC = "Encaminhado";
    public static final String ST_CONT = "Contratado";

    public long id;
    public String role = "";
    public String name = "";
    public String email = "";
    public String university = "";
    public String course = "";
    public String semester = "";
    public String phone = "";
    public String linkedin = "";
    public String about = "";
    public String skills = "";
    public String cvUri = "";
    public String cvName = "";
    public long cvSize;
    public long cvDate;
    public String orgName = "";
    public String orgType = "";
    public String initials = "";
    public String cnpj = "";
    public String sector = "";
    public String location = "";
    public String responsible = "";
    public String responsibleRole = "";
    public String status = ST_DISP;
    public int gradYear;
    public long createdAt;

    public boolean isOrg() {
        return !ALUNO.equals(role);
    }

    public List<String> skillList() {
        List<String> out = new ArrayList<>();
        if (skills == null) return out;
        for (String s : skills.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    public void setSkillList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            if (sb.length() > 0) sb.append(',');
            sb.append(s);
        }
        skills = sb.toString();
    }

    /** Nome exibido: nome da pessoa (aluno) ou da organização. */
    public String display() {
        return isOrg() ? orgName : name;
    }

    /** Iniciais para o avatar do próprio usuário. */
    public String avatar() {
        if (isOrg()) return initials.isEmpty() ? Fmt.initialsOrg(orgName) : initials;
        return Fmt.initialsSelf(name);
    }

    public boolean hasCv() {
        return cvName != null && !cvName.isEmpty();
    }

    /** Porcentagem do perfil do aluno (8 itens opcionais). */
    public int profilePercent() {
        int ok = 0;
        if (!university.trim().isEmpty()) ok++;
        if (!course.trim().isEmpty()) ok++;
        if (!semester.trim().isEmpty()) ok++;
        if (!about.trim().isEmpty()) ok++;
        if (!skillList().isEmpty()) ok++;
        if (hasCv()) ok++;
        if (!phone.trim().isEmpty()) ok++;
        if (!linkedin.trim().isEmpty()) ok++;
        return Math.round(ok * 100f / 8f);
    }

    /** "Engenharia de Computação · 6º semestre" */
    public String courseLine() {
        String s = course;
        if (!semester.trim().isEmpty()) s += (s.isEmpty() ? "" : " · ") + semester.trim() + "º semestre";
        return s;
    }

    /** "Eng. de Computação · USP" */
    public String shortLine() {
        String c = Fmt.abbrevCourse(course);
        String u = Fmt.abbrevUni(university);
        if (c.isEmpty()) return u;
        if (u.isEmpty()) return c;
        return c + " · " + u;
    }

    /** "Tecnologia · São Paulo, SP" */
    public String orgLine() {
        String s = sector;
        if (!location.trim().isEmpty()) s += (s.isEmpty() ? "" : " · ") + location.trim();
        return s;
    }
}
