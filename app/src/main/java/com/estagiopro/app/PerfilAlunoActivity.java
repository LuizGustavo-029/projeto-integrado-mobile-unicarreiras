package com.estagiopro.app;

import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.estagiopro.app.data.Repo;
import com.estagiopro.app.util.Fmt;

import java.util.List;

public class PerfilAlunoActivity extends BaseActivity {

    private final ActivityResultLauncher<String[]> picker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onCvPicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_aluno);
        Ui.statusBar(this, R.color.navy_dark, false);
        Ui.setupNav(this, Ui.NAV_ALUNO, 3);

        Runnable edit = () -> Ui.go(this, EditarPerfilAlunoActivity.class);
        findViewById(R.id.btnEditarPerfil).setOnClickListener(v -> edit.run());
        findViewById(R.id.btnEditarSobre).setOnClickListener(v -> edit.run());
        findViewById(R.id.btnEditarFormacao).setOnClickListener(v -> edit.run());
        findViewById(R.id.btnAdicionarCompetencia).setOnClickListener(v ->
                Ui.input(this, "Nova competência", "Ex.: Figma, Python, Inglês B2", "", value -> {
                    if (value.isEmpty()) return;
                    List<String> skills = me.skillList();
                    for (String s : skills) {
                        if (s.equalsIgnoreCase(value)) {
                            Ui.toast(this, "Você já tem essa competência");
                            return;
                        }
                    }
                    skills.add(value);
                    me.setSkillList(skills);
                    Repo.updateUser(this, me);
                    bind();
                }));
        findViewById(R.id.btnCurriculo).setOnClickListener(v -> cvMenu());
        findViewById(R.id.btnSair).setOnClickListener(v ->
                Ui.confirm(this, "Sair da conta", "Deseja realmente sair?", "Sair", () -> {
                    com.estagiopro.app.data.Session.logout(this);
                    Ui.goRoot(this, LoginAlunoActivity.class);
                }));
    }

    private void cvMenu() {
        if (!me.hasCv()) {
            picker.launch(CvHelper.MIME);
            return;
        }
        Ui.choose(this, me.cvName, new String[]{"Abrir currículo", "Substituir arquivo", "Remover currículo"}, which -> {
            if (which == 0) {
                CvHelper.open(this, me.cvUri, me.cvName);
            } else if (which == 1) {
                picker.launch(CvHelper.MIME);
            } else {
                CvHelper.clear(me);
                Repo.updateUser(this, me);
                bind();
            }
        });
    }

    private void onCvPicked(Uri uri) {
        if (uri == null || me == null) return;
        CvHelper.store(this, me, uri);
        Repo.updateUser(this, me);
        Ui.toast(this, "Currículo anexado");
        bind();
    }

    @Override
    protected void bind() {
        ((TextView) findViewById(R.id.tvNomeHeader)).setText(me.name);
        ((TextView) findViewById(R.id.tvAvatar)).setText(me.avatar());
        ((TextView) findViewById(R.id.tvNome)).setText(me.name);
        String line = me.courseLine();
        ((TextView) findViewById(R.id.tvSub)).setText(line.isEmpty() ? "Complete sua formação" : line);
        ((TextView) findViewById(R.id.tvBadge)).setText("Perfil " + me.profilePercent() + "% completo");
        ((TextView) findViewById(R.id.tvSobre)).setText(
                me.about.trim().isEmpty() ? "Conte um pouco sobre você, seus objetivos e interesses." : me.about);
        ((TextView) findViewById(R.id.tvUniversidade)).setText(
                me.university.trim().isEmpty() ? "Universidade não informada" : me.university);
        ((TextView) findViewById(R.id.tvCursoPeriodo)).setText(
                me.courseLine().isEmpty() ? "Curso não informado" : me.courseLine());

        ViewGroup flow = findViewById(R.id.flSkills);
        List<String> skills = me.skillList();
        if (skills.isEmpty()) skills = java.util.Collections.singletonList("Nenhuma competência adicionada");
        Ui.addChips(flow, skills, R.layout.item_skill);

        if (me.hasCv()) {
            ((TextView) findViewById(R.id.tvCvNome)).setText(me.cvName);
            String size = Fmt.fileSize(me.cvSize);
            ((TextView) findViewById(R.id.tvCvInfo)).setText(
                    "Atualizado em " + Fmt.dayMonth(me.cvDate) + (size.isEmpty() ? "" : " · " + size));
        } else {
            ((TextView) findViewById(R.id.tvCvNome)).setText("Nenhum currículo enviado");
            ((TextView) findViewById(R.id.tvCvInfo)).setText("Toque para anexar um PDF ou DOC");
        }
    }
}
