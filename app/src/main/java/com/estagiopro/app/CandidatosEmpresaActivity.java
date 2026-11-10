package com.estagiopro.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.estagiopro.app.data.Candidatura;
import com.estagiopro.app.data.Job;
import com.estagiopro.app.data.Repo;
import com.estagiopro.app.data.User;
import com.estagiopro.app.util.Fmt;

import java.util.ArrayList;
import java.util.List;

public class CandidatosEmpresaActivity extends BaseActivity {

    private static final int PAGE = 30;
    private static final String[] TABS = {"novos", "favoritos", "entrevistas"};

    private int tab = 0;
    private int shown = PAGE;
    private long jobFilter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidatos_empresa);
        Ui.statusBar(this, R.color.bg, true);
        Ui.setupNav(this, Ui.NAV_EMPRESA, 2);

        ((EditText) findViewById(R.id.etBusca)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) { }

            @Override
            public void afterTextChanged(Editable s) {
                shown = PAGE;
                if (me != null) bind();
            }
        });
        Ui.tabs(this, 0, index -> {
            tab = index;
            shown = PAGE;
            bind();
        }, R.id.tabNovos, R.id.tabFavoritos, R.id.tabEntrevistas);
        findViewById(R.id.btnFiltro).setOnClickListener(v -> chooseJob());
    }

    private void chooseJob() {
        final List<Job> jobs = Repo.jobsByOwner(this, me.id, Job.ATIVA);
        String[] names = new String[jobs.size() + 1];
        names[0] = "Todas as vagas";
        for (int i = 0; i < jobs.size(); i++) names[i + 1] = jobs.get(i).title;
        Ui.choose(this, "Filtrar por vaga", names, which -> {
            jobFilter = which == 0 ? 0 : jobs.get(which - 1).id;
            shown = PAGE;
            bind();
            Ui.toast(this, which == 0 ? "Mostrando todas as vagas" : "Vaga: " + jobs.get(which - 1).title);
        });
    }

    @Override
    protected void bind() {
        int[] st = Repo.ownerStats(this, me.id);
        ((TextView) findViewById(R.id.tvInscritos)).setText(Fmt.pad2(st[0]));
        ((TextView) findViewById(R.id.tvEmAnalise)).setText(Fmt.pad2(st[1]));
        ((TextView) findViewById(R.id.tvEntrevistas)).setText(Fmt.pad2(st[2]));

        String q = Ui.text((EditText) findViewById(R.id.etBusca));
        List<Candidatura> all = Repo.appsForOwner(this, me.id, TABS[tab], q);
        List<Candidatura> apps = new ArrayList<>();
        for (Candidatura c : all) {
            if (c.student == null || c.job == null) continue;
            if (jobFilter != 0 && c.jobId != jobFilter) continue;
            apps.add(c);
        }

        LinearLayout list = findViewById(R.id.llCandidatos);
        list.removeAllViews();
        if (apps.isEmpty()) {
            Ui.empty(list, tab == 1 ? "Você ainda não favoritou candidatos."
                    : (tab == 2 ? "Nenhuma entrevista agendada." : "Nenhum candidato encontrado."));
            return;
        }
        long lastJob = -1;
        int count = Math.min(shown, apps.size());
        for (int i = 0; i < count; i++) {
            final Candidatura c = apps.get(i);
            if (c.jobId != lastJob) {
                Ui.groupLabel(list, c.job.title);
                lastJob = c.jobId;
            }
            User s = c.student;
            View row = Ui.inflate(this, R.layout.item_candidate, list);
            ((TextView) row.findViewById(R.id.tvAvatar)).setText(Fmt.initialsList(s.name));
            ((TextView) row.findViewById(R.id.tvName)).setText(Fmt.shortName(s.name));
            ((TextView) row.findViewById(R.id.tvSub)).setText(s.shortLine());
            ((TextView) row.findViewById(R.id.tvMatch)).setText(c.match + "% match");
            List<String> skills = s.skillList();
            Ui.addChips((ViewGroup) row.findViewById(R.id.flChips),
                    skills.subList(0, Math.min(3, skills.size())), R.layout.item_chip_dark);
            row.setOnClickListener(v -> Ui.go(this, CandidatoDetalheActivity.class, Ui.APP_ID, c.id));
            list.addView(row);
        }
        if (count < apps.size()) {
            Ui.more(list, () -> {
                shown += PAGE;
                bind();
            });
        }
    }
}
