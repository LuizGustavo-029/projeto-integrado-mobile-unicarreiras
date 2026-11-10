package com.estagiopro.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.estagiopro.app.data.Job;
import com.estagiopro.app.data.Repo;
import com.estagiopro.app.util.Fmt;

import java.util.List;

public class PainelInstituicaoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painel_instituicao);
        Ui.statusBar(this, R.color.bg, true);
        Ui.setupNav(this, Ui.NAV_INSTITUICAO, 0);

        findViewById(R.id.btnAvatar).setOnClickListener(v -> Ui.switchTo(this, PerfilInstituicaoActivity.class));
        findViewById(R.id.btnNovaVaga).setOnClickListener(v -> Ui.go(this, PublicarVagaActivity.class));
        findViewById(R.id.tvGerenciar).setOnClickListener(v -> Ui.switchTo(this, VagasInstituicaoActivity.class));
    }

    @Override
    protected void bind() {
        Ui.bell(this, me.id);
        ((TextView) findViewById(R.id.tvNome)).setText(me.orgName);
        ((TextView) findViewById(R.id.tvAvatar)).setText(me.avatar());

        int[] p = Repo.institutionPanel(this, me.id);
        ((TextView) findViewById(R.id.tvStat1)).setText(String.valueOf(p[0]));
        ((TextView) findViewById(R.id.tvStat2)).setText(String.valueOf(p[1]));
        ((TextView) findViewById(R.id.tvStat3)).setText(String.valueOf(p[2]));
        ((TextView) findViewById(R.id.tvStat4)).setText(p[3] + "%");

        LinearLayout list = findViewById(R.id.llVagas);
        list.removeAllViews();
        List<Job> jobs = Repo.jobsByOwner(this, me.id, Job.ATIVA);
        if (jobs.isEmpty()) {
            Ui.empty(list, "Nenhuma oportunidade publicada. Toque no botão + para cadastrar.");
            return;
        }
        for (int i = 0; i < Math.min(2, jobs.size()); i++) {
            final Job j = jobs.get(i);
            View row = Ui.inflate(this, R.layout.item_vaga_row, list);
            ((TextView) row.findViewById(R.id.tvTitle)).setText(j.alias.isEmpty() ? j.title : j.alias);
            ((TextView) row.findViewById(R.id.tvSub)).setText(Fmt.plural(j.applicants, "candidato", "candidatos"));
            Ui.setBadge((TextView) row.findViewById(R.id.tvBadge), j.status, Ui.GREEN);
            row.setOnClickListener(v -> Ui.go(this, VagaGerenciarActivity.class, Ui.JOB_ID, j.id));
            list.addView(row);
        }
    }
}
