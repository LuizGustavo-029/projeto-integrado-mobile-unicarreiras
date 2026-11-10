package com.estagiopro.app;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.estagiopro.app.data.Repo;
import com.estagiopro.app.data.User;
import com.estagiopro.app.util.Fmt;

import java.util.ArrayList;
import java.util.List;

public class EditarPerfilAlunoActivity extends BaseActivity {

    private final ActivityResultLauncher<String[]> picker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onCvPicked);

    private boolean loaded = false;
    private final List<String> skills = new ArrayList<>();
    private User draft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil_aluno);
        Ui.statusBar(this, R.color.bg, true);
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddSkill).setOnClickListener(v -> addSkill());
        findViewById(R.id.btnAnexarCv).setOnClickListener(v -> picker.launch(CvHelper.MIME));
        findViewById(R.id.btnRemoverCv).setOnClickListener(v -> {
            CvHelper.clear(draft);
            paintCv();
        });
        findViewById(R.id.btnSalvar).setOnClickListener(v -> save());
        findViewById(R.id.btnAlterarSenha).setOnClickListener(v -> Ui.changePasswordDialog(this, me.id));
    }

    private void onCvPicked(Uri uri) {
        if (uri == null || draft == null) return;
        CvHelper.store(this, draft, uri);
        paintCv();
    }

    @Override
    protected void bind() {
        if (loaded) return; // não sobrescreve o que o usuário está digitando
        loaded = true;
        draft = me;
        ((EditText) findViewById(R.id.etNome)).setText(me.name);
        ((EditText) findViewById(R.id.etUniversidade)).setText(me.university);
        ((EditText) findViewById(R.id.etCurso)).setText(me.course);
        ((EditText) findViewById(R.id.etSemestre)).setText(me.semester);
        ((EditText) findViewById(R.id.etTelefone)).setText(me.phone);
        ((EditText) findViewById(R.id.etLinkedin)).setText(me.linkedin);
        ((EditText) findViewById(R.id.etSobre)).setText(me.about);
        skills.clear();
        skills.addAll(me.skillList());
        paintSkills();
        paintCv();
    }

    private void paintSkills() {
        ViewGroup flow = findViewById(R.id.flSkills);
        flow.removeAllViews();
        for (final String s : skills) {
            TextView t = (TextView) Ui.inflate(this, R.layout.item_skill, flow);
            t.setText(s + "  ✕");
            t.setOnClickListener(v -> {
                skills.remove(s);
                paintSkills();
            });
            flow.addView(t);
        }
    }

    private void addSkill() {
        EditText et = findViewById(R.id.etNovaSkill);
        String s = Ui.text(et);
        if (s.isEmpty()) return;
        for (String x : skills) {
            if (x.equalsIgnoreCase(s)) {
                et.setText("");
                return;
            }
        }
        skills.add(s);
        et.setText("");
        paintSkills();
    }

    private void paintCv() {
        TextView name = findViewById(R.id.tvCvNome);
        TextView info = findViewById(R.id.tvCvInfo);
        View remove = findViewById(R.id.btnRemoverCv);
        if (draft.hasCv()) {
            name.setText(draft.cvName);
            String size = Fmt.fileSize(draft.cvSize);
            info.setText(size.isEmpty() ? "Toque para substituir" : size + " · toque para substituir");
            remove.setVisibility(View.VISIBLE);
        } else {
            name.setText("Nenhum arquivo anexado");
            info.setText("Toque para anexar um PDF");
            remove.setVisibility(View.GONE);
        }
    }

    private void save() {
        EditText etNome = findViewById(R.id.etNome);
        String nome = Ui.text(etNome);
        if (nome.length() < 3) {
            etNome.setError("Informe seu nome completo");
            etNome.requestFocus();
            return;
        }
        String sem = Ui.text((EditText) findViewById(R.id.etSemestre));
        if (!sem.isEmpty() && !sem.matches("\\d{1,2}")) {
            ((EditText) findViewById(R.id.etSemestre)).setError("Use apenas números");
            return;
        }
        // campo de nova competência preenchido e não adicionado: inclui
        if (!Ui.text((EditText) findViewById(R.id.etNovaSkill)).isEmpty()) addSkill();

        draft.name = nome;
        draft.university = Ui.text((EditText) findViewById(R.id.etUniversidade));
        draft.course = Ui.text((EditText) findViewById(R.id.etCurso));
        draft.semester = sem;
        draft.phone = Ui.text((EditText) findViewById(R.id.etTelefone));
        draft.linkedin = Ui.text((EditText) findViewById(R.id.etLinkedin));
        draft.about = Ui.text((EditText) findViewById(R.id.etSobre));
        draft.setSkillList(skills);
        draft.initials = Fmt.initialsSelf(nome);
        Repo.updateUser(this, draft);
        Ui.hideKeyboard(this);
        Ui.toast(this, "Perfil atualizado");
        finish();
    }
}
