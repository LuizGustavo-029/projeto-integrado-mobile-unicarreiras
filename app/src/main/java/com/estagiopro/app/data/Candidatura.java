package com.estagiopro.app.data;

/** Candidatura de um aluno a uma vaga. */
public class Candidatura {
    public static final int ENVIADA = 0;
    public static final int ANALISE = 1;
    public static final int ENTREVISTA = 2;
    public static final int CONTRATADO = 3;
    public static final int RECUSADO = 4;

    public long id;
    public long jobId;
    public long studentId;
    public int stage;
    public long interviewAt;
    public boolean favorite;
    public int match;
    public boolean referred;
    public long createdAt;
    public long updatedAt;

    public Job job;
    public User student;

    public boolean active() {
        return stage <= ENTREVISTA;
    }

    public static String stageName(int stage) {
        switch (stage) {
            case ANALISE: return "Em análise";
            case ENTREVISTA: return "Entrevista";
            case CONTRATADO: return "Contratado";
            case RECUSADO: return "Recusada";
            default: return "Enviada";
        }
    }
}
