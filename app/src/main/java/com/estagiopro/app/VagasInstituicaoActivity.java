package com.estagiopro.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.estagiopro.app.data.Job;
import com.estagiopro.app.data.Repo;
import com.estagiopro.app.util.Fmt;

import java.util.List;

public class VagasInstituicaoActivity extends BaseActivity {

    private static final int PAGE = 10;
    private static final String[] STATUSES = {Job.ATIVA, Job.REVISAO, Job.EXPIRADA};

    private int tab = 0;
    private int shown = PAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vagas_instituicao);
        Ui.statusBar(this, R.color.bg, true);
        Ui.setupNav(this, Ui.NAV_INSTITUICAO, 1);

        findViewById(R.id.btnAdicionarOportunidade).setOnClickListener(v -> Ui.go(this, PublicarVagaActivity.class));
        Ui.tabs(this, 0, index -> {
            tab = index;
            shown = PAGE;
            bind();
        }, R.id.tabPublicadas, R.id.tabRevisao, R.id.tabExpiradas);
    }

    @Override
    protected void bind() {
        int reach = Repo.reach(this, me.id);
        int goal = 1560;
        try {
            goal = Integer.parseInt(Repo.getMeta(this, "meta_alcance", "1560"));
        } catch (NumberFormatException ignored) {
            // mantém o padrão
        }
        ((TextView) findViewById(R.id.tvAlcance)).setText(Fmt.thousands(reach) + " alunos");
        ((ProgressBar) findViewById(R.id.pbAlcance)).setProgress(Math.min(100, goal <= 0 ? 0 : reach * 100 / goal));

        LinearLayout list = findViewById(R.id.llOportunidades);
        list.removeAllViews();
        List<Job> jobs = Repo.jobsByOwner(this, me.id, STATUSES[tab]);
        if (jobs.isEmpty()) {
            Ui.empty(list, "Nenhuma oportunidade nesta aba.");
            return;
        }
        int count = Math.min(shown, jobs.size());
        for (int i = 0; i < count; i++) {
            final Job j = jobs.get(i);
            View row = Ui.inflate(this, R.layout.item_opp, list);
            ((TextView) row.findViewById(R.id.tvAvatar)).setText(Fmt.initialsOrg(j.companyName));
            ((TextView) row.findViewById(R.id.tvCompany)).setText(j.companyName);
            ((TextView) row.findViewById(R.id.tvTitle)).setText(j.title);
            ((TextView) row.findViewById(R.id.tvSub)).setText(j.area);
            ((TextView) row.findViewById(R.id.tvInteresse)).setText(Fmt.plural(j.interested(), "interessado", "interessados"));
            row.setOnClickListener(v -> Ui.go(this, VagaGerenciarActivity.class, Ui.JOB_ID, j.id));
            list.addView(row);
        }
        if (count < jobs.size()) {
            Ui.more(list, () -> {
                shown += PAGE;
                bind();
            });
        }
    }
}
