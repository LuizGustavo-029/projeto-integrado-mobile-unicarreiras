package com.estagiopro.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class LoginEmpresaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_empresa);
        Ui.statusBar(this, R.color.navy_dark, false);

        final EditText etEmail = findViewById(R.id.etEmail);
        final EditText etSenha = findViewById(R.id.etSenha);
        Ui.passwordToggle(this, R.id.etSenha, R.id.ivEye);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnEntrar).setOnClickListener(v -> Auth.login(this, etEmail, etSenha, "empresa"));
        findViewById(R.id.tvEsqueci).setOnClickListener(v -> {
            Intent i = new Intent(this, EsqueciSenhaActivity.class);
            i.putExtra("email", Ui.text(etEmail));
            startActivity(i);
        });
        findViewById(R.id.tvCadastrese).setOnClickListener(v -> Ui.go(this, CadastroEmpresaPasso1Activity.class));
    }
}
