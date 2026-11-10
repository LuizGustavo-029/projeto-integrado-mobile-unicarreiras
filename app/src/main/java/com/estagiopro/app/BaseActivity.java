package com.estagiopro.app;

import androidx.appcompat.app.AppCompatActivity;

import com.estagiopro.app.data.Session;
import com.estagiopro.app.data.User;

/**
 * Base das telas que exigem login: carrega o usuário logado e chama bind() a cada
 * vez que a tela volta ao primeiro plano (para refletir mudanças feitas em outras telas).
 */
public abstract class BaseActivity extends AppCompatActivity {

    /** Usuário logado (atualizado antes de cada bind). */
    protected User me;

    /** Preenche a tela com os dados atuais. */
    protected abstract void bind();

    @Override
    protected void onResume() {
        super.onResume();
        me = Session.current(this);
        if (me == null) {
            Session.logout(this);
            Ui.goRoot(this, LoginAlunoActivity.class);
            return;
        }
        bind();
    }
}
