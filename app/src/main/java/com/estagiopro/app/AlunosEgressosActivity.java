package com.estagiopro.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.estagiopro.app.data.Repo;
import com.estagiopro.app.data.User;
import com.estagiopro.app.util.Fmt;

import java.util.ArrayList;
import java.util.List;

public class AlunosEgressosActivity extends BaseActivity {

    private static final int PAGE = 20;
    private static final String[] STATUSES = {User.ST_DISP, User.ST_ENC, User.ST_CONT};

    private int tab = 0;
    private int shown = PAGE;
    private boolean onlyGrads = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alunos_egressos);
        Ui.statusBar(this, R.color.bg, true);
        Ui.setupNav(this, Ui.NAV_INSTITUICAO, 2);

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
        }, R.id.tabDisponiveis, R.id.tabEncaminhados, R.id.tabContratados);

        findViewById(R.id.btnFiltro).setOnClickListener(v ->
                Ui.choose(this, "Filtrar alunos",
                        new String[]{"Todos os alunos", "Somente formandos de " + gradYear()}, which -> {
                            onlyGrads = which == 1;
                            shown = PAGE;
                            bind();
                        }));
    }

    private int gradYear() {
        try {
            return Integer.parseInt(Repo.getMeta(this, "ano_formandos", "2025"));
        } catch (NumberFormatException e) {
            return 2025;
        }
    }

    @Override
    protected void bind() {
        ((TextView) findViewById(R.id.tvDisponiveis)).setText(String.valueOf(Repo.countStudents(this, null, null)));
        ((TextView) findViewById(R.id.tvFormandos)).setText(String.valueOf(Repo.graduates(this)));

        int rate = Repo.employabilityRate(this);
        int before = 0;
        int goal = 75;
        try {
            before = Integer.parseInt(Repo.getMeta(this, "taxa_anterior", "0"));
            goal = Integer.parseInt(Repo.getMeta(this, "meta_taxa", "75"));
        } catch (NumberFormatException ignored) {
            // usa os padrões
        }
        int delta = rate - before;
        ((TextView) findViewById(R.id.tvTaxa)).setText(rate + "%");
        ((TextView) findViewById(R.id.tvDelta)).setText((delta >= 0 ? "+" : "") + delta + "% neste semestre");
        ((TextView) findViewById(R.id.tvMeta)).setText("Meta institucional: " + goal + "%");

        String q = Ui.text((EditText) findViewById(R.id.etBusca));
        List<User> all = Repo.students(this, STATUSES[tab], q, 0);
        List<User> students = new ArrayList<>();
        int year = gradYear();
        for (User u : all) {
            if (onlyGrads && u.gradYear != year) continue;
            students.add(u);
        }

        LinearLayout list = findViewById(R.id.llAlunos);
        list.removeAllViews();
        if (students.isEmpty()) {
            Ui.empty(list, "Nenhum aluno nesta lista.");
            return;
        }
        int count = Math.min(shown, students.size());
        for (int i = 0; i < count; i++) {
            final User u = students.get(i);
            View row = Ui.inflate(this, R.layout.item_student, list);
            ((TextView) row.findViewById(R.id.tvAvatar)).setText(Fmt.initialsList(u.name));
            ((TextView) row.findViewById(R.id.tvName)).setText(Fmt.shortName(u.name));
            String sem = u.semester.trim().isEmpty() ? "" : " · " + u.semester.trim() + "º semestre";
            ((TextView) row.findViewById(R.id.tvSub)).setText(Fmt.abbrevCourse(u.course) + sem);
            int kind = User.ST_CONT.equals(u.status) ? Ui.GRAY : (User.ST_ENC.equals(u.status) ? Ui.BLUE : Ui.GREEN);
            Ui.setBadge((TextView) row.findViewById(R.id.tvStatus), u.status, kind);
            row.setOnClickListener(v -> Ui.go(this, CandidatoDetalheActivity.class, Ui.STUDENT_ID, u.id));
            list.addView(row);
        }
        if (count < students.size()) {
            Ui.more(list, () -> {
                shown += PAGE;
                bind();
            });
        }
    }
}
