package com.example.estagiopro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignupStep1Activity extends AppCompatActivity {

    private EditText etNome, etEmail, etUniversidade, etCurso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_step1);

        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etUniversidade = findViewById(R.id.etUniversidade);
        etCurso = findViewById(R.id.etCurso);

        LinearLayout btnBack = findViewById(R.id.btnBack);
        TextView btnContinuar = findViewById(R.id.btnContinuar);
        TextView tvEntrar = findViewById(R.id.tvEntrar);

        // Voltar para o Login
        btnBack.setOnClickListener(v -> finish());
        tvEntrar.setOnClickListener(v -> finish());

        // Botão Continuar: valida campos e vai para a tela 3 (Defina sua senha)
        btnContinuar.setOnClickListener(v -> {
            String nome = etNome.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String universidade = etUniversidade.getText().toString().trim();
            String curso = etCurso.getText().toString().trim();

            if (nome.isEmpty()) {
                etNome.setError("Preencha seu nome completo");
                etNome.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                etEmail.setError("Preencha o e-mail institucional");
                etEmail.requestFocus();
                return;
            }
            if (universidade.isEmpty()) {
                etUniversidade.setError("Preencha sua universidade");
                etUniversidade.requestFocus();
                return;
            }
            if (curso.isEmpty()) {
                etCurso.setError("Preencha seu curso");
                etCurso.requestFocus();
                return;
            }

            Intent intent = new Intent(SignupStep1Activity.this, SignupStep2Activity.class);
            intent.putExtra("nome", nome);
            intent.putExtra("email", email);
            intent.putExtra("universidade", universidade);
            intent.putExtra("curso", curso);
            startActivity(intent);
        });
    }
}
