package com.estagiopro.app;

import android.os.Bundle;
import android.widget.TextView;

import com.estagiopro.app.data.Session;

/** Perfil de empresa / instituição (as duas telas têm o mesmo conteúdo). */
public abstract class PerfilOrgBase extends BaseActivity {

    protected abstract int layout();

    protected abstract Class<?>[] nav();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout());
        Ui.statusBar(this, R.color.navy_dark, false);
        Ui.setupNav(this, nav(), 3);

        Runnable edit = () -> Ui.go(this, EditarPerfilOrgActivity.class);
        findViewById(R.id.btnEditarPerfil).setOnClickListener(v -> edit.run());
        findViewById(R.id.btnEditarSobre).setOnClickListener(v -> edit.run());
        findViewById(R.id.btnEditarInfo).setOnClickListener(v -> edit.run());
        findViewById(R.id.btnSair).setOnClickListener(v ->
                Ui.confirm(this, "Sair da conta", "Deseja realmente sair?", "Sair", () -> {
                    Session.logout(this);
                    Ui.goRoot(this, LoginAlunoActivity.class);
                }));
    }

    private static String orDash(String s, String fallback) {
        return s == null || s.trim().isEmpty() ? fallback : s.trim();
    }

    @Override
    protected void bind() {
        ((TextView) findViewById(R.id.tvNomeHeader)).setText(me.orgName);
        ((TextView) findViewById(R.id.tvAvatar)).setText(me.avatar());
        ((TextView) findViewById(R.id.tvNome)).setText(me.orgName);
        ((TextView) findViewById(R.id.tvSub)).setText(orDash(me.orgLine(), me.orgType));
        ((TextView) findViewById(R.id.tvSobre)).setText(
                orDash(me.about, "Conte um pouco sobre a organização para atrair candidatos."));
        ((TextView) findViewById(R.id.tvCnpj)).setText(orDash(me.cnpj, "Não informado"));
        ((TextView) findViewById(R.id.tvContato)).setText(me.email);
        ((TextView) findViewById(R.id.tvLocal)).setText(orDash(me.location, "Não informado"));
        ((TextView) findViewById(R.id.tvResponsavel)).setText(orDash(me.responsible, "Não informado"));
        ((TextView) findViewById(R.id.tvCargo)).setText(orDash(me.responsibleRole, ""));
    }
}
