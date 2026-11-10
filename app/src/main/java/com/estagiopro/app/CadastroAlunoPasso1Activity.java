package com.estagiopro.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.estagiopro.app.data.Repo;
import com.estagiopro.app.util.Fmt;

public class CadastroAlunoPasso1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_aluno_passo1);
        Ui.statusBar(this, R.color.navy_dark, false);

        final EditText etNome = findViewById(R.id.etNome);
        final EditText etEmail = findViewById(R.id.etEmail);
        final EditText etUni = findViewById(R.id.etUniversidade);
        final EditText etCurso = findViewById(R.id.etCurso);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnSouRecrutador).setOnClickListener(v -> Ui.go(this, CadastroEmpresaPasso1Activity.class));
        findViewById(R.id.tvEntrar).setOnClickListener(v -> Ui.goRoot(this, LoginAlunoActivity.class));

        findViewById(R.id.btnContinuar).setOnClickListener(v -> {
            String nome = Ui.text(etNome);
            String email = Ui.text(etEmail);
            String uni = Ui.text(etUni);
            String curso = Ui.text(etCurso);
            if (nome.split("\\s+").length < 2 || nome.length() < 5) {
                etNome.setError("Informe seu nome completo");
                etNome.requestFocus();
                return;
            }
            if (!Fmt.validEmail(email)) {
                etEmail.setError("Informe um e-mail válido");
                etEmail.requestFocus();
                return;
            }
            if (Repo.emailTaken(this, email, 0)) {
                etEmail.setError("Este e-mail já está cadastrado");
                etEmail.requestFocus();
                return;
            }
            if (uni.isEmpty()) {
                etUni.setError("Informe sua universidade");
                etUni.requestFocus();
                return;
            }
            if (curso.isEmpty()) {
                etCurso.setError("Informe seu curso");
                etCurso.requestFocus();
                return;
            }
            Intent i = new Intent(this, CadastroAlunoPasso2Activity.class);
            i.putExtra("nome", nome);
            i.putExtra("email", email);
            i.putExtra("uni", uni);
            i.putExtra("curso", curso);
            startActivity(i);
        });
    }
}
