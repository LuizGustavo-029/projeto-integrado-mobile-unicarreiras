package com.estagiopro.app;

public class CadastroEmpresaPasso2Activity extends CadastroOrgPasso2Base {
    @Override
    protected int layout() {
        return R.layout.activity_cadastro_empresa_passo2;
    }

    @Override
    protected boolean isEmpresa() {
        return true;
    }
}
