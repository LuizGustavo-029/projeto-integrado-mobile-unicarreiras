package com.estagiopro.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.estagiopro.app.data.Repo;
import com.estagiopro.app.util.Fmt;

/** Passo 1 do cadastro de empresa / instituição (as duas telas são idênticas, mudando só a opção marcada). */
public abstract class CadastroOrgPasso1Base extends AppCompatActivity {

    protected abstract int layout();

    protected abstract boolean isEmpresa();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout());
        Ui.statusBar(this, R.color.navy_dark, false);

        final EditText etOrg = findViewById(R.id.etOrganizacao);
        final EditText etCnpj = findViewById(R.id.etCnpj);
        final EditText etEmail = findViewById(R.id.etEmail);
        Auth.cnpjMask(etCnpj);

        Intent in = getIntent();
        if (in.getStringExtra("org") != null) etOrg.setText(in.getStringExtra("org"));
        if (in.getStringExtra("cnpj") != null) etCnpj.setText(in.getStringExtra("cnpj"));
        if (in.getStringExtra("email") != null) etEmail.setText(in.getStringExtra("email"));

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        // Alternar entre Empresa e Instituição mantém o que já foi digitado
        final Class<?> other = isEmpresa() ? CadastroInstituicaoPasso1Activity.class : CadastroEmpresaPasso1Activity.class;
        findViewById(isEmpresa() ? R.id.optInstituicao : R.id.optEmpresa).setOnClickListener(v -> {
            Intent i = new Intent(this, other);
            i.putExtra("org", Ui.text(etOrg));
            i.putExtra("cnpj", Ui.text(etCnpj));
            i.putExtra("email", Ui.text(etEmail));
            startActivity(i);
            finish();
        });

        findViewById(R.id.btnContinuar).setOnClickListener(v -> {
            String org = Ui.text(etOrg);
            String cnpj = Ui.text(etCnpj);
            String email = Ui.text(etEmail);
            if (org.length() < 3) {
                etOrg.setError("Informe o nome da organização");
                etOrg.requestFocus();
                return;
            }
            if (!Fmt.validCnpj(cnpj)) {
                etCnpj.setError("CNPJ inválido");
                etCnpj.requestFocus();
                return;
            }
            if (!Fmt.validEmail(email)) {
                etEmail.setError("Informe um e-mail corporativo válido");
                etEmail.requestFocus();
                return;
            }
            if (Repo.emailTaken(this, email, 0)) {
                etEmail.setError("Este e-mail já está cadastrado");
                etEmail.requestFocus();
                return;
            }
            Intent i = new Intent(this, isEmpresa() ? CadastroEmpresaPasso2Activity.class : CadastroInstituicaoPasso2Activity.class);
            i.putExtra("org", org);
            i.putExtra("cnpj", cnpj);
            i.putExtra("email", email);
            startActivity(i);
        });
    }
}
