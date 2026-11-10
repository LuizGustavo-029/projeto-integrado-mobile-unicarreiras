package com.estagiopro.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.estagiopro.app.data.Candidatura;
import com.estagiopro.app.data.Job;
import com.estagiopro.app.data.Repo;
import com.estagiopro.app.util.Fmt;

import java.util.List;

/** Detalhe da vaga, visto pelo aluno: candidatar, retirar candidatura e salvar. */
public class VagaDetalheActivity extends BaseActivity {

    private long jobId;
    private boolean viewCounted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaga_detalhe);
        Ui.statusBar(this, R.color.bg, true);
        jobId = getIntent().getLongExtra(Ui.JOB_ID, 0);
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
    }

    @Override
    protected void bind() {
        final Job j = Repo.getJob(this, jobId);
        if (j == null) {
            Ui.toast(this, "Esta vaga não está mais disponível");
            finish();
            return;
        }
        if (!viewCounted) {
            viewCounted = true;
            Repo.addView(this, jobId);
        }
        final Candidatura app = Repo.appFor(this, me.id, jobId);

        ((TextView) findViewById(R.id.tvAvatar)).setText(Fmt.initialsOrg(j.companyName));
        ((TextView) findViewById(R.id.tvCompany)).setText(j.companyName);
        ((TextView) findViewById(R.id.tvTitle)).setText(j.title);
        ((TextView) findViewById(R.id.tvPosted)).setText("Publicada " + Fmt.ago(j.createdAt).replace("há ", "há ")
                + " · " + Fmt.plural(j.applicants, "candidato", "candidatos"));
        Ui.addChips((ViewGroup) findViewById(R.id.flChips), j.chips(), R.layout.item_chip);
        ((TextView) findViewById(R.id.tvLocal)).setText(j.location.isEmpty() ? "Não informado" : j.location);
        ((TextView) findViewById(R.id.tvModo)).setText(j.mode);
        ((TextView) findViewById(R.id.tvBolsa)).setText(j.salary.isEmpty() ? "A combinar" : j.salary);
        ((TextView) findViewById(R.id.tvCarga)).setText(j.hours.isEmpty() ? "A combinar" : j.hours);
        ((TextView) findViewById(R.id.tvDescricao)).setText(
                j.description.isEmpty() ? "A empresa ainda não detalhou as atividades desta vaga." : j.description);
        List<String> req = j.tags();
        if (req.isEmpty()) req = java.util.Collections.singletonList("Sem requisitos específicos");
        Ui.addChips((ViewGroup) findViewById(R.id.flRequisitos), req, R.layout.item_chip);

        // salvar
        final ImageView bookmark = findViewById(R.id.btnSalvar);
        paintBookmark(bookmark, Repo.isSaved(this, me.id, jobId));
        bookmark.setOnClickListener(v -> {
            boolean now = Repo.toggleSaved(this, me.id, jobId);
            paintBookmark(bookmark, now);
            Ui.toast(this, now ? "Vaga salva" : "Vaga removida dos salvos");
        });

        View status = findViewById(R.id.llStatusCand);
        View apply = findViewById(R.id.btnCandidatar);
        View withdraw = findViewById(R.id.btnRetirar);
        boolean open = Job.ATIVA.equals(j.status);
        if (app != null) {
            status.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvStatusCand)).setText(
                    "Candidatura: " + Candidatura.stageName(app.stage));
            apply.setVisibility(View.GONE);
            withdraw.setVisibility(app.active() ? View.VISIBLE : View.GONE);
            withdraw.setOnClickListener(v ->
                    Ui.confirm(this, "Retirar candidatura", "Deseja retirar sua candidatura para “" + j.title + "”?",
                            "Retirar", () -> {
                                Repo.withdraw(this, app.id);
                                Ui.toast(this, "Candidatura retirada");
                                bind();
                            }));
        } else {
            status.setVisibility(View.GONE);
            withdraw.setVisibility(View.GONE);
            apply.setVisibility(open ? View.VISIBLE : View.GONE);
            if (!open) {
                status.setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.tvStatusCand)).setText("Esta vaga não recebe mais candidaturas");
            }
            apply.setOnClickListener(v -> {
                if (!me.hasCv()) {
                    Ui.confirm(this, "Sem currículo anexado",
                            "Você ainda não anexou um currículo. Deseja se candidatar mesmo assim?",
                            "Candidatar", this::doApply);
                } else {
                    doApply();
                }
            });
        }
    }

    private void doApply() {
        long id = Repo.apply(this, me.id, jobId);
        Ui.toast(this, id > 0 ? "Candidatura enviada!" : "Não foi possível se candidatar");
        bind();
    }

    private void paintBookmark(ImageView iv, boolean saved) {
        iv.setColorFilter(ContextCompat.getColor(this, saved ? R.color.blue : R.color.muted));
    }
}
