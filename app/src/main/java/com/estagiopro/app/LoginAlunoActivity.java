package com.estagiopro.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class LoginAlunoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_aluno);
        Ui.statusBar(this, R.color.navy_dark, false);

        final EditText etEmail = findViewById(R.id.etEmail);
        final EditText etSenha = findViewById(R.id.etSenha);
        Ui.passwordToggle(this, R.id.etSenha, R.id.ivEye);

        findViewById(R.id.btnEntrar).setOnClickListener(v -> Auth.login(this, etEmail, etSenha, "aluno"));
        findViewById(R.id.tvEsqueci).setOnClickListener(v -> {
            Intent i = new Intent(this, EsqueciSenhaActivity.class);
            i.putExtra("email", Ui.text(etEmail));
            startActivity(i);
        });
        findViewById(R.id.btnGoogle).setOnClickListener(v -> social("Google"));
        findViewById(R.id.btnMicrosoft).setOnClickListener(v -> social("Microsoft"));
        findViewById(R.id.btnSouEmpresa).setOnClickListener(v -> Ui.go(this, LoginEmpresaActivity.class));
        findViewById(R.id.btnSouInstituicao).setOnClickListener(v -> Ui.go(this, LoginInstituicaoActivity.class));
        findViewById(R.id.tvCadastrese).setOnClickListener(v -> Ui.go(this, CadastroAlunoPasso1Activity.class));
    }

    private void social(String provider) {
        Ui.toast(this, "Login com " + provider + " ainda não está disponível. Entre com e-mail e senha.");
    }
}
