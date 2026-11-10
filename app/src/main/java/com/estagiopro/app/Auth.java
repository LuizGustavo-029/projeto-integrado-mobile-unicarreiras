package com.estagiopro.app;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.estagiopro.app.data.Repo;
import com.estagiopro.app.data.Session;
import com.estagiopro.app.data.User;
import com.estagiopro.app.util.Fmt;

/** Regras comuns de login e cadastro. */
final class Auth {

    private Auth() { }

    private static boolean accepts(String portal, User u) {
        if (User.ALUNO.equals(portal)) return !u.isOrg();
        return portal.equals(u.role);
    }

    private static String roleName(User u) {
        if (User.EMPRESA.equals(u.role)) return "empresa";
        if (User.INSTITUICAO.equals(u.role)) return "instituição";
        return "aluno";
    }

    /** Valida os campos, confere a senha e abre a tela inicial do perfil. */
    static void login(Activity a, EditText etEmail, EditText etSenha, String portal) {
        String email = Ui.text(etEmail);
        String pass = etSenha.getText().toString();
        if (!Fmt.validEmail(email)) {
            etEmail.setError("Informe um e-mail válido");
            etEmail.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            etSenha.setError("Informe sua senha");
            etSenha.requestFocus();
            return;
        }
        User u = Repo.login(a, email, pass);
        if (u == null) {
            etSenha.setError("E-mail ou senha incorretos");
            etSenha.requestFocus();
            return;
        }
        if (!accepts(portal, u)) {
            Ui.toast(a, "Esta é uma conta de " + roleName(u) + ". Use o acesso correspondente na tela inicial.");
            return;
        }
        Session.login(a, u);
        Ui.hideKeyboard(a);
        Ui.goHome(a, u);
    }

    /** Formata o CNPJ enquanto o usuário digita. */
    static void cnpjMask(final EditText et) {
        et.addTextChangedListener(new TextWatcher() {
            private boolean busy;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (busy) return;
                busy = true;
                String masked = Fmt.maskCnpj(s.toString());
                if (!masked.equals(s.toString())) s.replace(0, s.length(), masked);
                busy = false;
            }
        });
    }

    /** Retorna a mensagem de erro da senha ou null se estiver ok. */
    static String passwordError(String pass) {
        if (pass.length() < 8) return "Mínimo de 8 caracteres";
        return null;
    }
}
