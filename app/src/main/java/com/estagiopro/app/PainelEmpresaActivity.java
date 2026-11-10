package com.estagiopro.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.estagiopro.app.data.Job;
import com.estagiopro.app.data.Repo;
import com.estagiopro.app.util.Fmt;

import java.util.List;

public class PainelEmpresaActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painel_empresa);
        Ui.statusBar(this, R.color.bg, true);
        Ui.setupNav(this, Ui.NAV_EMPRESA, 0);

        findViewById(R.id.btnAvatar).setOnClickListener(v -> Ui.switchTo(this, PerfilEmpresaActivity.class));
        findViewById(R.id.btnNovaVaga).setOnClickListener(v -> Ui.go(this, PublicarVagaActivity.class));
        findViewById(R.id.tvGerenciar).setOnClickListener(v -> Ui.switchTo(this, VagasEmpresaActivity.class));
    }

    @Override
    protected void bind() {
        Ui.bell(this, me.id);
        ((TextView) findViewById(R.id.tvNome)).setText(me.orgName);
        ((TextView) findViewById(R.id.tvAvatar)).setText(me.avatar());

        int[] p = Repo.companyPanel(this, me.id);
        ((TextView) findViewById(R.id.tvStat1)).setText(Fmt.pad2(p[0]));
        ((TextView) findViewById(R.id.tvStat2)).setText(Fmt.pad2(p[1]));
        ((TextView) findViewById(R.id.tvStat3)).setText(Fmt.pad2(p[2]));
        ((TextView) findViewById(R.id.tvStat4)).setText(Fmt.pad2(p[3]));

        LinearLayout list = findViewById(R.id.llVagas);
        list.removeAllViews();
        List<Job> jobs = Repo.jobsByOwner(this, me.id, Job.ATIVA, Job.PAUSADA);
        if (jobs.isEmpty()) {
            Ui.empty(list, "Você ainda não publicou vagas. Toque no botão + para começar.");
            return;
        }
        for (int i = 0; i < Math.min(3, jobs.size()); i++) {
            final Job j = jobs.get(i);
            View row = Ui.inflate(this, R.layout.item_vaga_row, list);
            ((TextView) row.findViewById(R.id.tvTitle)).setText(j.title);
            ((TextView) row.findViewById(R.id.tvSub)).setText(Fmt.plural(j.applicants, "candidato", "candidatos"));
            Ui.setBadge((TextView) row.findViewById(R.id.tvBadge), j.status,
                    Job.ATIVA.equals(j.status) ? Ui.GREEN : Ui.ORANGE);
            row.setOnClickListener(v -> Ui.go(this, VagaGerenciarActivity.class, Ui.JOB_ID, j.id));
            list.addView(row);
        }
    }
}
