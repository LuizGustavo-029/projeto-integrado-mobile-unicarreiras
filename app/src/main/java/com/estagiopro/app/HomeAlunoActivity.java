package com.estagiopro.app;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.estagiopro.app.data.Job;
import com.estagiopro.app.data.Repo;
import com.estagiopro.app.util.Fmt;

import java.util.ArrayList;
import java.util.List;

public class HomeAlunoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_aluno);
        Ui.statusBar(this, R.color.bg, true);
        Ui.setupNav(this, Ui.NAV_ALUNO, 0);

        findViewById(R.id.btnAvatar).setOnClickListener(v -> Ui.switchTo(this, PerfilAlunoActivity.class));
        findViewById(R.id.btnCompletarPerfil).setOnClickListener(v -> Ui.go(this, EditarPerfilAlunoActivity.class));
        findViewById(R.id.tvVerTodas).setOnClickListener(v -> Ui.switchTo(this, BuscarVagasActivity.class));
    }

    @Override
    protected void bind() {
        Ui.bell(this, me.id);
        ((TextView) findViewById(R.id.tvSaudacao)).setText(Fmt.greeting());
        ((TextView) findViewById(R.id.tvNome)).setText(Fmt.greetingName(me.name));
        ((TextView) findViewById(R.id.tvAvatar)).setText(me.avatar());

        int pct = me.profilePercent();
        ((TextView) findViewById(R.id.tvPercent)).setText(pct + "%");
        ((ProgressBar) findViewById(R.id.pbPerfil)).setProgress(pct);

        List<Job> all = Repo.searchJobs(this, me, null, null, false, false, true);
        int matchs = 0;
        for (Job j : all) if (Repo.matchPct(me, j) >= 70) matchs++;
        int[] st = Repo.studentStats(this, me.id);
        ((TextView) findViewById(R.id.tvMatchs)).setText(Fmt.pad2(matchs));
        ((TextView) findViewById(R.id.tvCandidaturas)).setText(Fmt.pad2(st[0]));
        ((TextView) findViewById(R.id.tvEntrevistas)).setText(Fmt.pad2(st[1]));

        LinearLayout list = findViewById(R.id.llOportunidades);
        list.removeAllViews();
        List<Job> rec = Repo.recommended(this, me, 2);
        if (rec.isEmpty()) {
            Ui.empty(list, "Nenhuma vaga disponível no momento. Volte mais tarde!");
            return;
        }
        for (final Job j : rec) {
            android.view.View row = Ui.inflate(this, R.layout.item_job_home, list);
            ((TextView) row.findViewById(R.id.tvAvatar)).setText(Fmt.initialsOrg(j.companyName));
            ((TextView) row.findViewById(R.id.tvCompany)).setText(Fmt.shortCompany(j.companyName));
            ((TextView) row.findViewById(R.id.tvTitle)).setText(j.title);
            List<String> chips = new ArrayList<>(j.chips());
            if (chips.size() > 1) chips.remove(chips.size() - 1); // o tipo ("Estágio") não aparece aqui
            Ui.addChips((android.view.ViewGroup) row.findViewById(R.id.flChips), chips, R.layout.item_chip);
            row.setOnClickListener(v -> Ui.go(this, VagaDetalheActivity.class, Ui.JOB_ID, j.id));
            list.addView(row);
        }
    }
}
