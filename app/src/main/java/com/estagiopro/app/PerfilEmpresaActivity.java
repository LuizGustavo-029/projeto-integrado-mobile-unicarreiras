package com.estagiopro.app;

public class PerfilEmpresaActivity extends PerfilOrgBase {
    @Override
    protected int layout() {
        return R.layout.activity_perfil_empresa;
    }

    @Override
    protected Class<?>[] nav() {
        return Ui.NAV_EMPRESA;
    }
}
