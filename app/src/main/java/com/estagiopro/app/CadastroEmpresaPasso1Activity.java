package com.estagiopro.app;

public class CadastroEmpresaPasso1Activity extends CadastroOrgPasso1Base {
    @Override
    protected int layout() {
        return R.layout.activity_cadastro_empresa_passo1;
    }

    @Override
    protected boolean isEmpresa() {
        return true;
    }
}
