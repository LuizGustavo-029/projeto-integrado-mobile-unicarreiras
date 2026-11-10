package com.estagiopro.app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.estagiopro.app.data.Job;
import com.estagiopro.app.data.Repo;
import com.estagiopro.app.util.Fmt;

import java.util.List;

public class VagasEmpresaActivity extends BaseActivity {

    private static final int PAGE = 10;
    private static final String[][] STATUSES = {
            {Job.ATIVA, Job.PAUSADA}, {Job.RASCUNHO}, {Job.ENCERRADA}};

    private Ui.Group tabs;
    private int tab = 0;
    private int shown = PAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vagas_empresa);
        Ui.statusBar(this, R.color.bg, true);
        Ui.setupNav(this, Ui.NAV_EMPRESA, 1);

        findViewById(R.id.btnPublicarVaga).setOnClickListener(v -> Ui.go(this, PublicarVagaActivity.class));
        tabs = Ui.tabs(this, 0, index -> {
            tab = index;
            shown = PAGE;
            bind();
        }, R.id.tabAtivas, R.id.tabRascunhos, R.id.tabEncerradas);

        int[] nums = {R.id.tvAtivas, R.id.tvRascunhos, R.id.tvEncerradas};
        for (int i = 0; i < nums.length; i++) {
            final int index = i;
            ((View) findViewById(nums[i]).getParent()).setOnClickListener(v -> tabs.select(index, true));
        }
    }

    @Override
    protected void bind() {
        TextView[] nums = {findViewById(R.id.tvAtivas), findViewById(R.id.tvRascunhos), findViewById(R.id.tvEncerradas)};
        for (int i = 0; i < nums.length; i++) {
            nums[i].setText(Fmt.pad2(Repo.countJobs(this, me.id, STATUSES[i])));
            Ui.statSelect(nums[i], i == tab);
        }

        LinearLayout list = findViewById(R.id.llVagas);
        list.removeAllViews();
        List<Job> jobs = Repo.jobsByOwner(this, me.id, STATUSES[tab]);
        if (jobs.isEmpty()) {
            Ui.empty(list, tab == 0 ? "Nenhuma vaga ativa. Toque em “Publicar nova vaga”."
                    : (tab == 1 ? "Você não tem rascunhos." : "Nenhuma vaga encerrada."));
            return;
        }
        int count = Math.min(shown, jobs.size());
        for (int i = 0; i < count; i++) {
            final Job j = jobs.get(i);
            View row = Ui.inflate(this, R.layout.item_vaga_manage, list);
            ((TextView) row.findViewById(R.id.tvTitle)).setText(j.title);
            ((TextView) row.findViewById(R.id.tvSub)).setText(j.subLine());
            Ui.setBadge((TextView) row.findViewById(R.id.tvBadge), j.status, badgeKind(j.status));
            ((TextView) row.findViewById(R.id.tvCands)).setText(String.valueOf(j.applicants));
            ((TextView) row.findViewById(R.id.tvViews)).setText(String.valueOf(j.views));
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

    static int badgeKind(String status) {
        if (Job.ATIVA.equals(status)) return Ui.GREEN;
        if (Job.PAUSADA.equals(status) || Job.REVISAO.equals(status)) return Ui.ORANGE;
        return Ui.GRAY;
    }
}
