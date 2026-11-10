package com.estagiopro.app;

public class PerfilInstituicaoActivity extends PerfilOrgBase {
    @Override
    protected int layout() {
        return R.layout.activity_perfil_instituicao;
    }

    @Override
    protected Class<?>[] nav() {
        return Ui.NAV_INSTITUICAO;
    }
}
