package com.estagiopro.app.data;

import com.estagiopro.app.util.Fmt;

import java.util.ArrayList;
import java.util.List;

/** Vaga de estágio (publicada por empresa) ou oportunidade (curadoria da instituição). */
public class Job {
    public static final String ATIVA = "Ativa";
    public static final String PAUSADA = "Pausada";
    public static final String RASCUNHO = "Rascunho";
    public static final String ENCERRADA = "Encerrada";
    public static final String REVISAO = "Em revisão";
    public static final String EXPIRADA = "Expirada";

    public static final String SRC_EMPRESA = "empresa";
    public static final String SRC_INSTITUICAO = "instituicao";

    public long id;
    public long ownerId;
    public String companyName = "";
    public String title = "";
    public String alias = "";
    public String area = "";
    public String location = "";
    public String mode = "Híbrido";
    public String type = "Estágio";
    public String salary = "";
    public String hours = "";
    public String description = "";
    public String requirements = "";
    public String status = ATIVA;
    public String source = SRC_EMPRESA;
    public int views;
    public long createdAt;
    /** Preenchidos pelas consultas. */
    public int applicants;
    public int saved;

    public List<String> tags() {
        List<String> out = new ArrayList<>();
        for (String s : requirements.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    public boolean isNew() {
        return System.currentTimeMillis() - createdAt <= 2 * Fmt.DAY + 2 * 60 * 60 * 1000L;
    }

    public boolean isRemote() {
        return "Remoto".equals(mode);
    }

    /** Chips do cartão: [cidade, modalidade, tipo] ou [Remoto, bolsa, tipo]. */
    public List<String> chips() {
        List<String> out = new ArrayList<>();
        if (isRemote()) {
            out.add("Remoto");
            if (!salary.trim().isEmpty()) out.add(salary.trim());
        } else {
            String city = Fmt.cityOf(location);
            if (!city.isEmpty()) out.add(city);
            out.add(mode);
        }
        out.add(type);
        return out;
    }

    /** "São Paulo · Híbrido" ou "Remoto · 6 horas". */
    public String subLine() {
        if (isRemote()) {
            return hours.trim().isEmpty() ? "Remoto" : "Remoto · " + hours.trim();
        }
        String city = Fmt.cityOf(location);
        return city.isEmpty() ? mode : city + " · " + mode;
    }

    public int interested() {
        return applicants + saved;
    }
}
