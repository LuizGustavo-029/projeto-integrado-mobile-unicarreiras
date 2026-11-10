package com.estagiopro.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.estagiopro.app.data.Candidatura;
import com.estagiopro.app.data.Job;
import com.estagiopro.app.data.Repo;
import com.estagiopro.app.data.User;
import com.estagiopro.app.util.Fmt;

import java.util.List;

/** Gerenciar uma vaga publicada (empresa ou instituição). */
public class VagaGerenciarActivity extends BaseActivity {

    private long jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaga_gerenciar);
        Ui.statusBar(this, R.color.bg, true);
        jobId = getIntent().getLongExtra(Ui.JOB_ID, 0);
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnEditar).setOnClickListener(v ->
                Ui.go(this, PublicarVagaActivity.class, Ui.JOB_ID, jobId));
    }

    @Override
    protected void bind() {
        final Job j = Repo.getJob(this, jobId);
        if (j == null || j.ownerId != me.id) {
            finish();
            return;
        }
        final boolean inst = User.INSTITUICAO.equals(me.role);
        ((TextView) findViewById(R.id.tvTitle)).setText(j.title);
        ((TextView) findViewById(R.id.tvSub)).setText(
                inst ? j.companyName + " · " + j.subLine() : j.subLine());
        Ui.setBadge((TextView) findViewById(R.id.tvBadge), j.status, VagasEmpresaActivity.badgeKind(j.status));
        ((TextView) findViewById(R.id.tvCands)).setText(String.valueOf(j.applicants));
        ((TextView) findViewById(R.id.tvViews)).setText(String.valueOf(j.views));
        ((TextView) findViewById(R.id.tvSalvos)).setText(String.valueOf(j.saved));
        ((TextView) findViewById(R.id.tvDescricao)).setText(
                j.description.isEmpty() ? "Sem descrição." : j.description);

        // botão de status
        View btnStatus = findViewById(R.id.btnStatus);
        final String next;
        String label;
        switch (j.status) {
            case Job.ATIVA:
                next = inst ? Job.REVISAO : Job.PAUSADA;
                label = inst ? "Enviar para revisão" : "Pausar vaga";
                break;
            case Job.PAUSADA:
                next = Job.ATIVA;
                label = "Reativar vaga";
                break;
            case Job.REVISAO:
            case Job.RASCUNHO:
                next = Job.ATIVA;
                label = "Publicar vaga";
                break;
            default: // Encerrada / Expirada
                next = Job.ATIVA;
                label = "Reabrir vaga";
                break;
        }
        Ui.setLabel(btnStatus, label);
        btnStatus.setOnClickListener(v -> {
            Repo.setJobStatus(this, jobId, next);
            Ui.toast(this, "Status atualizado: " + next);
            bind();
        });

        View btnEncerrar = findViewById(R.id.btnEncerrar);
        final String closed = inst ? Job.EXPIRADA : Job.ENCERRADA;
        boolean already = Job.ENCERRADA.equals(j.status) || Job.EXPIRADA.equals(j.status);
        btnEncerrar.setVisibility(already ? View.GONE : View.VISIBLE);
        Ui.setLabel(btnEncerrar, inst ? "Expirar" : "Encerrar vaga");
        btnEncerrar.setOnClickListener(v ->
                Ui.confirm(this, inst ? "Expirar oportunidade" : "Encerrar vaga",
                        "A vaga deixará de aparecer para os alunos. Você poderá reabri-la depois.",
                        "Confirmar", () -> {
                            Repo.setJobStatus(this, jobId, closed);
                            bind();
                        }));

        findViewById(R.id.btnExcluir).setOnClickListener(v ->
                Ui.confirm(this, "Excluir vaga",
                        "Isso apaga a vaga e todas as candidaturas dela. Não é possível desfazer.",
                        "Excluir", () -> {
                            Repo.deleteJob(this, jobId);
                            Ui.toast(this, "Vaga excluída");
                            finish();
                        }));

        LinearLayout list = findViewById(R.id.llCandidatos);
        list.removeAllViews();
        List<Candidatura> apps = Repo.appsForJob(this, jobId);
        if (apps.isEmpty()) {
            Ui.empty(list, "Ainda não há candidatos para esta vaga.");
            return;
        }
        for (final Candidatura c : apps) {
            if (c.student == null) continue;
            View row = Ui.inflate(this, R.layout.item_applicant, list);
            ((TextView) row.findViewById(R.id.tvAvatar)).setText(Fmt.initialsList(c.student.name));
            ((TextView) row.findViewById(R.id.tvName)).setText(Fmt.shortName(c.student.name));
            ((TextView) row.findViewById(R.id.tvSub)).setText(c.student.shortLine());
            ((TextView) row.findViewById(R.id.tvMatch)).setText(c.match + "% match");
            ((TextView) row.findViewById(R.id.tvStage)).setText(Candidatura.stageName(c.stage));
            row.setOnClickListener(v -> Ui.go(this, CandidatoDetalheActivity.class, Ui.APP_ID, c.id));
            list.addView(row);
        }
    }
}
