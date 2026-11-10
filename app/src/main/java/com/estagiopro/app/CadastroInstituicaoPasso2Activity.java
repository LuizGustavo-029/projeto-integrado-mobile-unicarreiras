package com.estagiopro.app;

public class CadastroInstituicaoPasso2Activity extends CadastroOrgPasso2Base {
    @Override
    protected int layout() {
        return R.layout.activity_cadastro_instituicao_passo2;
    }

    @Override
    protected boolean isEmpresa() {
        return false;
    }
}
