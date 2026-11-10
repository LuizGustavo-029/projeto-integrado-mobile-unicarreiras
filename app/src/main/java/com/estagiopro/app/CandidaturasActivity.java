package com.estagiopro.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.estagiopro.app.data.Candidatura;
import com.estagiopro.app.data.Job;
import com.estagiopro.app.data.Repo;
import com.estagiopro.app.util.Fmt;

import java.util.List;

public class CandidaturasActivity extends BaseActivity {

    private static final int ANDAMENTO = 0;
    private static final int FINALIZADAS = 1;
    private static final int TODAS = 2;
    private static final int ENTREVISTAS = 3;

    private int filter = ANDAMENTO;
    private Ui.Group tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidaturas);
        Ui.statusBar(this, R.color.bg, true);
        Ui.setupNav(this, Ui.NAV_ALUNO, 2);

        tabs = Ui.tabs(this, 0, index -> {
            filter = index;
            bind();
        }, R.id.tabAndamento, R.id.tabFinalizadas, R.id.tabTodas);

        // tocar nos cartões de resumo também filtra a lista
        card(R.id.tvAtivas).setOnClickListener(v -> setFilter(ANDAMENTO));
        card(R.id.tvEntrevistas).setOnClickListener(v -> setFilter(ENTREVISTAS));
        card(R.id.tvConcluidas).setOnClickListener(v -> setFilter(FINALIZADAS));
    }

    private View card(int numberId) {
        return (View) findViewById(numberId).getParent();
    }

    private void setFilter(int f) {
        filter = f;
        tabs.select(f == ENTREVISTAS ? -1 : f, false);
        bind();
    }

    @Override
    protected void bind() {
        int[] st = Repo.studentStats(this, me.id);
        TextView tvA = findViewById(R.id.tvAtivas);
        TextView tvE = findViewById(R.id.tvEntrevistas);
        TextView tvC = findViewById(R.id.tvConcluidas);
        tvA.setText(Fmt.pad2(st[0]));
        tvE.setText(Fmt.pad2(st[1]));
        tvC.setText(Fmt.pad2(st[2]));
        Ui.statSelect(tvA, filter == ANDAMENTO);
        Ui.statSelect(tvE, filter == ENTREVISTAS);
        Ui.statSelect(tvC, filter == FINALIZADAS);

        String group = filter == FINALIZADAS ? "finalizadas" : (filter == TODAS ? "todas" : "andamento");
        List<Candidatura> apps = Repo.appsForStudent(this, me.id, group);
        LinearLayout list = findViewById(R.id.llProcessos);
        list.removeAllViews();
        int shown = 0;
        for (Candidatura c : apps) {
            if (c.job == null) continue;
            if (filter == ENTREVISTAS && c.stage != Candidatura.ENTREVISTA) continue;
            addCard(list, c);
            shown++;
        }
        if (shown == 0) {
            Ui.empty(list, filter == FINALIZADAS ? "Nenhum processo finalizado ainda."
                    : "Nenhuma candidatura por aqui. Explore as vagas e candidate-se!");
        }
    }

    private void addCard(LinearLayout list, final Candidatura c) {
        final Job j = c.job;
        View row = Ui.inflate(this, R.layout.item_process, list);
        ((TextView) row.findViewById(R.id.tvAvatar)).setText(Fmt.initialsOrg(j.companyName));
        ((TextView) row.findViewById(R.id.tvCompany)).setText(Fmt.shortCompany(j.companyName));
        ((TextView) row.findViewById(R.id.tvTitle)).setText(j.title);

        int[] segIds = {R.id.seg1, R.id.seg2, R.id.seg3, R.id.seg4};
        int filled = c.stage == Candidatura.RECUSADO ? 0 : c.stage + 1;
        for (int i = 0; i < segIds.length; i++) {
            row.findViewById(segIds[i]).setBackgroundResource(i < filled ? R.drawable.bg_seg_on : R.drawable.bg_seg_off);
        }

        String status;
        String sub;
        switch (c.stage) {
            case Candidatura.ANALISE:
                status = "Currículo em análise";
                sub = "Atualizado " + Fmt.ago(c.updatedAt);
                break;
            case Candidatura.ENTREVISTA:
                status = "Entrevista com o time";
                sub = Fmt.interview(c.interviewAt);
                break;
            case Candidatura.CONTRATADO:
                status = "Você foi selecionado(a)!";
                sub = "Atualizado " + Fmt.ago(c.updatedAt);
                break;
            case Candidatura.RECUSADO:
                status = "Processo encerrado";
                sub = "Atualizado " + Fmt.ago(c.updatedAt);
                break;
            default:
                status = "Candidatura enviada";
                sub = "Enviado em " + Fmt.dayMonth(c.createdAt);
                break;
        }
        ((TextView) row.findViewById(R.id.tvStatus)).setText(status);
        ((TextView) row.findViewById(R.id.tvSub)).setText(sub);
        row.findViewById(R.id.llBadge).setVisibility(c.stage == Candidatura.ENTREVISTA ? View.VISIBLE : View.GONE);
        row.setOnClickListener(v -> Ui.go(this, VagaDetalheActivity.class, Ui.JOB_ID, j.id));
        list.addView(row);
    }
}
