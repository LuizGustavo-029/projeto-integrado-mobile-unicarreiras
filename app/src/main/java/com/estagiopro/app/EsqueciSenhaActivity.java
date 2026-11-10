package com.estagiopro.app;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.estagiopro.app.data.Repo;
import com.estagiopro.app.data.User;
import com.estagiopro.app.util.Fmt;

/** Redefinição de senha local (o app funciona offline, sem envio de e-mail). */
public class EsqueciSenhaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esqueci_senha);
        Ui.statusBar(this, R.color.navy_dark, false);

        final EditText etEmail = findViewById(R.id.etEmail);
        final EditText etSenha = findViewById(R.id.etSenha);
        final EditText etConfirmar = findViewById(R.id.etConfirmar);
        Ui.passwordToggle(this, R.id.etSenha, R.id.ivEye);

        String prefill = getIntent().getStringExtra("email");
        if (prefill != null) etEmail.setText(prefill);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.tvEntrar).setOnClickListener(v -> finish());
        findViewById(R.id.btnRedefinir).setOnClickListener(v -> {
            String email = Ui.text(etEmail);
            if (!Fmt.validEmail(email)) {
                etEmail.setError("Informe um e-mail válido");
                etEmail.requestFocus();
                return;
            }
            User u = Repo.findByEmail(this, email);
            if (u == null) {
                etEmail.setError("Nenhuma conta com este e-mail");
                etEmail.requestFocus();
                return;
            }
            String pass = etSenha.getText().toString();
            String err = Auth.passwordError(pass);
            if (err != null) {
                etSenha.setError(err);
                etSenha.requestFocus();
                return;
            }
            if (!pass.equals(etConfirmar.getText().toString())) {
                etConfirmar.setError("As senhas não conferem");
                etConfirmar.requestFocus();
                return;
            }
            Repo.setPassword(this, u.id, pass);
            Ui.toast(this, "Senha redefinida! Entre com a nova senha.");
            finish();
        });
    }
}
