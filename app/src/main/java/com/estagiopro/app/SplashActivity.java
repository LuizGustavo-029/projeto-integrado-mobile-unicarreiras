package com.estagiopro.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.estagiopro.app.data.Session;
import com.estagiopro.app.data.User;

/** Tela de abertura: se já existe sessão, entra direto na tela inicial do perfil. */
public class SplashActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable next = new Runnable() {
        @Override
        public void run() {
            User u = Session.current(SplashActivity.this);
            if (u != null) {
                Ui.goHome(SplashActivity.this, u);
            } else {
                Ui.goRoot(SplashActivity.this, LoginAlunoActivity.class);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Ui.statusBar(this, R.color.navy_dark, false);
        handler.postDelayed(next, 900);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(next);
        super.onDestroy();
    }
}
