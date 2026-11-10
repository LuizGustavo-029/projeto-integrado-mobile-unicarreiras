package com.estagiopro.app;

import android.os.Bundle;
import android.widget.EditText;

import com.estagiopro.app.data.Repo;
import com.estagiopro.app.data.User;
import com.estagiopro.app.util.Fmt;

public class EditarPerfilOrgActivity extends BaseActivity {

    private boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil_org);
        Ui.statusBar(this, R.color.bg, true);
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnSalvar).setOnClickListener(v -> save());
        findViewById(R.id.btnAlterarSenha).setOnClickListener(v -> Ui.changePasswordDialog(this, me.id));
        // CNPJ e e-mail identificam a conta: apenas leitura
        findViewById(R.id.etCnpj).setEnabled(false);
        findViewById(R.id.etEmail).setEnabled(false);
    }

    @Override
    protected void bind() {
        if (loaded) return;
        loaded = true;
        ((EditText) findViewById(R.id.etOrg)).setText(me.orgName);
        ((EditText) findViewById(R.id.etSetor)).setText(me.sector);
        ((EditText) findViewById(R.id.etCnpj)).setText(me.cnpj);
        ((EditText) findViewById(R.id.etEmail)).setText(me.email);
        ((EditText) findViewById(R.id.etLocal)).setText(me.location);
        ((EditText) findViewById(R.id.etResponsavel)).setText(me.responsible);
        ((EditText) findViewById(R.id.etCargo)).setText(me.responsibleRole);
        ((EditText) findViewById(R.id.etSobre)).setText(me.about);
    }

    private void save() {
        EditText etOrg = findViewById(R.id.etOrg);
        String org = Ui.text(etOrg);
        if (org.length() < 2) {
            etOrg.setError("Informe o nome da organização");
            etOrg.requestFocus();
            return;
        }
        User u = me;
        boolean renamed = !org.equals(u.orgName);
        u.orgName = org;
        if (renamed) u.initials = Fmt.initialsOrg(org);
        u.sector = Ui.text((EditText) findViewById(R.id.etSetor));
        u.location = Ui.text((EditText) findViewById(R.id.etLocal));
        u.responsible = Ui.text((EditText) findViewById(R.id.etResponsavel));
        u.responsibleRole = Ui.text((EditText) findViewById(R.id.etCargo));
        u.about = Ui.text((EditText) findViewById(R.id.etSobre));
        Repo.updateUser(this, u);
        if (renamed && User.EMPRESA.equals(u.role)) Repo.renameCompany(this, u.id, org);
        Ui.hideKeyboard(this);
        Ui.toast(this, "Perfil atualizado");
        finish();
    }
}
