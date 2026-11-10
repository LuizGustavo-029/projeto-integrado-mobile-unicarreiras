package com.estagiopro.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.estagiopro.app.data.Job;
import com.estagiopro.app.data.Repo;
import com.estagiopro.app.util.Fmt;

import java.util.List;
import java.util.Set;

public class BuscarVagasActivity extends BaseActivity {

    private static final int PAGE = 15;
    private static final String[] MODES = {null, "Remoto", "Híbrido", "Presencial"};

    private String mode;
    private boolean onlySaved;
    private boolean hideApplied;
    private boolean byMatch;
    private int shown = PAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_vagas);
        Ui.statusBar(this, R.color.bg, true);
        Ui.setupNav(this, Ui.NAV_ALUNO, 1);

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

        Ui.chips(this, 0, index -> {
            mode = MODES[index];
            shown = PAGE;
            bind();
        }, R.id.chipTodas, R.id.chipRemoto, R.id.chipHibrido, R.id.chipPresencial);

        findViewById(R.id.btnFiltro).setOnClickListener(v -> showFilters());
        final TextView sort = findViewById(R.id.tvMaisRecentes);
        sort.setOnClickListener(v -> {
            byMatch = !byMatch;
            sort.setText(byMatch ? "Melhor match" : "Mais recentes");
            shown = PAGE;
            bind();
        });
    }

    private void showFilters() {
        final boolean[] checked = {onlySaved, hideApplied};
        new AlertDialog.Builder(this)
                .setTitle("Filtrar vagas")
                .setMultiChoiceItems(new String[]{"Somente vagas salvas", "Ocultar vagas em que já me candidatei"},
                        checked, (d, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("Aplicar", (d, w) -> {
                    onlySaved = checked[0];
                    hideApplied = checked[1];
                    shown = PAGE;
                    bind();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void bind() {
        String q = Ui.text((EditText) findViewById(R.id.etBusca));
        List<Job> jobs = Repo.searchJobs(this, me, q, mode, onlySaved, hideApplied, byMatch);
        ((TextView) findViewById(R.id.tvSubtitulo)).setText(
                Fmt.plural(jobs.size(), "oportunidade", "oportunidades") + " para o seu perfil");

        LinearLayout list = findViewById(R.id.llVagas);
        list.removeAllViews();
        if (jobs.isEmpty()) {
            Ui.empty(list, onlySaved ? "Você ainda não salvou nenhuma vaga."
                    : "Nenhuma vaga encontrada. Tente outra busca ou filtro.");
            return;
        }
        final Set<Long> saved = Repo.savedIds(this, me.id);
        int count = Math.min(shown, jobs.size());
        for (int i = 0; i < count; i++) addCard(list, jobs.get(i), saved);
        if (count < jobs.size()) {
            Ui.more(list, () -> {
                shown += PAGE;
                bind();
            });
        }
    }

    private void addCard(LinearLayout list, final Job j, Set<Long> saved) {
        View row = Ui.inflate(this, R.layout.item_job_search, list);
        ((TextView) row.findViewById(R.id.tvAvatar)).setText(Fmt.initialsOrg(j.companyName));
        ((TextView) row.findViewById(R.id.tvCompany)).setText(Fmt.shortCompany(j.companyName));
        ((TextView) row.findViewById(R.id.tvTitle)).setText(j.title);
        row.findViewById(R.id.tvNova).setVisibility(j.isNew() ? View.VISIBLE : View.GONE);
        Ui.addChips((ViewGroup) row.findViewById(R.id.flChips), j.chips(), R.layout.item_chip);
        ((TextView) row.findViewById(R.id.tvPosted)).setText("Publicada " + Fmt.ago(j.createdAt));

        final ImageView bookmark = row.findViewById(R.id.btnSalvar);
        paintBookmark(bookmark, saved.contains(j.id));
        bookmark.setOnClickListener(v -> {
            boolean now = Repo.toggleSaved(this, me.id, j.id);
            paintBookmark(bookmark, now);
            Ui.toast(this, now ? "Vaga salva" : "Vaga removida dos salvos");
            if (onlySaved && !now) bind();
        });

        View.OnClickListener open = v -> Ui.go(this, VagaDetalheActivity.class, Ui.JOB_ID, j.id);
        row.findViewById(R.id.btnVerVaga).setOnClickListener(open);
        row.setOnClickListener(open);
        list.addView(row);
    }

    private void paintBookmark(ImageView iv, boolean saved) {
        iv.setColorFilter(ContextCompat.getColor(this, saved ? R.color.blue : R.color.muted));
    }
}
