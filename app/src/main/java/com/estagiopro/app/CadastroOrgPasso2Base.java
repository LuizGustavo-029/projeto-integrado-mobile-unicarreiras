package com.estagiopro.app;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.estagiopro.app.data.Repo;
import com.estagiopro.app.data.Session;
import com.estagiopro.app.data.User;

/** Passo 2 do cadastro de empresa / instituição: responsável, senha e termos. */
public abstract class CadastroOrgPasso2Base extends AppCompatActivity {

    protected abstract int layout();

    protected abstract boolean isEmpresa();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout());
        Ui.statusBar(this, R.color.navy_dark, false);

        final String org = getIntent().getStringExtra("org");
        final String cnpj = getIntent().getStringExtra("cnpj");
        final String email = getIntent().getStringExtra("email");
        if (org == null || email == null) {
            finish();
            return;
        }

        final EditText etResp = findViewById(R.id.etResponsavel);
        final EditText etSenha = findViewById(R.id.etSenha);
        final CheckBox cb = findViewById(R.id.cbTermos);
        Ui.passwordToggle(this, R.id.etSenha, R.id.ivEye);
        Ui.highlightTerms(this, (TextView) findViewById(R.id.cbTermosTexto));

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnCriarPerfil).setOnClickListener(v -> {
            String resp = Ui.text(etResp);
            String pass = etSenha.getText().toString();
            if (resp.length() < 3) {
                etResp.setError("Informe o nome do responsável");
                etResp.requestFocus();
                return;
            }
            String err = Auth.passwordError(pass);
            if (err != null) {
                etSenha.setError(err);
                etSenha.requestFocus();
                return;
            }
            if (!cb.isChecked()) {
                Ui.toast(this, "Confirme que representa a organização e aceite os termos");
                return;
            }
            User u = new User();
            u.role = isEmpresa() ? User.EMPRESA : User.INSTITUICAO;
            u.name = resp;
            u.email = email;
            u.orgName = org;
            u.orgType = isEmpresa() ? "Empresa" : "Instituição";
            u.sector = u.orgType;
            u.cnpj = cnpj == null ? "" : cnpj;
            u.responsible = resp;
            u.responsibleRole = "Responsável pela conta";
            long id = Repo.register(this, u, pass);
            if (id < 0) {
                Ui.toast(this, "Este e-mail já está cadastrado");
                finish();
                return;
            }
            Session.login(this, u);
            Ui.hideKeyboard(this);
            Ui.toast(this, "Perfil criado! Complete os dados da organização.");
            Ui.goHome(this, u);
        });
    }
}
