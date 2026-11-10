package com.estagiopro.app;

public class CadastroInstituicaoPasso1Activity extends CadastroOrgPasso1Base {
    @Override
    protected int layout() {
        return R.layout.activity_cadastro_instituicao_passo1;
    }

    @Override
    protected boolean isEmpresa() {
        return false;
    }
}
