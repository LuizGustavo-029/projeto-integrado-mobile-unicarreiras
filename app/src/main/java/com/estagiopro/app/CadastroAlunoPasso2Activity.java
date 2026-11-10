package com.estagiopro.app;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.estagiopro.app.data.Repo;
import com.estagiopro.app.data.Session;
import com.estagiopro.app.data.User;

public class CadastroAlunoPasso2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_aluno_passo2);
        Ui.statusBar(this, R.color.navy_dark, false);

        final String nome = getIntent().getStringExtra("nome");
        final String email = getIntent().getStringExtra("email");
        final String uni = getIntent().getStringExtra("uni");
        final String curso = getIntent().getStringExtra("curso");
        if (nome == null || email == null) {
            finish();
            return;
        }

        final EditText etSenha = findViewById(R.id.etSenha);
        final EditText etConfirmar = findViewById(R.id.etConfirmarSenha);
        final CheckBox cb = findViewById(R.id.cbTermos);
        Ui.passwordToggle(this, R.id.etSenha, R.id.ivEye);
        Ui.highlightTerms(this, (TextView) findViewById(R.id.cbTermosTexto));

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.tvEntrar).setOnClickListener(v -> Ui.goRoot(this, LoginAlunoActivity.class));

        findViewById(R.id.btnCriarConta).setOnClickListener(v -> {
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
            if (!cb.isChecked()) {
                Ui.toast(this, "Aceite os Termos de Uso e a Política de Privacidade para continuar");
                return;
            }
            User u = new User();
            u.role = User.ALUNO;
            u.name = nome;
            u.email = email;
            u.university = uni == null ? "" : uni;
            u.course = curso == null ? "" : curso;
            u.status = User.ST_DISP;
            long id = Repo.register(this, u, pass);
            if (id < 0) {
                Ui.toast(this, "Este e-mail já está cadastrado");
                finish();
                return;
            }
            Session.login(this, u);
            Ui.hideKeyboard(this);
            Ui.toast(this, "Conta criada! Bem-vindo(a) ao EstágioPro");
            Ui.goHome(this, u);
        });
    }
}
