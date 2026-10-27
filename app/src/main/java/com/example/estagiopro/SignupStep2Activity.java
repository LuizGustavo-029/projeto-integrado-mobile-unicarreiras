package com.example.estagiopro;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.estagiopro.api.ApiResponse;
import com.example.estagiopro.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupStep2Activity extends AppCompatActivity {

    private EditText etSenha, etConfirmarSenha;
    private ImageView ivToggleSenha, ivToggleConfirmar, ivCheck;
    private FrameLayout checkboxTerms;
    private boolean senhaVisivel = false;
    private boolean confirmarVisivel = false;
    private boolean termosAceitos = true; // já vem marcado, igual ao design

    // Dados vindos da tela anterior (SignupStep1Activity)
    private String nome, emailCadastro, universidade, curso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_step2);

        // Recupera os dados preenchidos na tela anterior (SignupStep1Activity)
        nome = getIntent().getStringExtra("nome");
        emailCadastro = getIntent().getStringExtra("email");
        universidade = getIntent().getStringExtra("universidade");
        curso = getIntent().getStringExtra("curso");

        etSenha = findViewById(R.id.etSenha);
        etConfirmarSenha = findViewById(R.id.etConfirmarSenha);
        ivToggleSenha = findViewById(R.id.ivToggleSenha);
        ivToggleConfirmar = findViewById(R.id.ivToggleConfirmar);
        checkboxTerms = findViewById(R.id.checkboxTerms);
        ivCheck = findViewById(R.id.ivCheck);

        LinearLayout btnBack = findViewById(R.id.btnBack);
        LinearLayout layoutCheckbox = findViewById(R.id.layoutCheckbox);
        TextView btnCriarConta = findViewById(R.id.btnCriarConta);
        TextView tvEntrar = findViewById(R.id.tvEntrar);

        // Voltar para a tela anterior (Crie sua conta)
        btnBack.setOnClickListener(v -> finish());
        tvEntrar.setOnClickListener(v -> {
            Intent intent = new Intent(SignupStep2Activity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // Mostrar/ocultar senha
        ivToggleSenha.setOnClickListener(v -> {
            senhaVisivel = !senhaVisivel;
            etSenha.setInputType(senhaVisivel
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etSenha.setSelection(etSenha.getText().length());
        });

        ivToggleConfirmar.setOnClickListener(v -> {
            confirmarVisivel = !confirmarVisivel;
            etConfirmarSenha.setInputType(confirmarVisivel
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etConfirmarSenha.setSelection(etConfirmarSenha.getText().length());
        });

        // Alternar caixa de aceite dos termos
        layoutCheckbox.setOnClickListener(v -> {
            termosAceitos = !termosAceitos;
            if (termosAceitos) {
                checkboxTerms.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_checkbox_checked));
                ivCheck.setVisibility(android.view.View.VISIBLE);
            } else {
                checkboxTerms.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_input_field));
                ivCheck.setVisibility(android.view.View.INVISIBLE);
            }
        });

        // Botão Criar conta: valida campos vazios, senhas iguais e termos aceitos
        btnCriarConta.setOnClickListener(v -> {
            String senha = etSenha.getText().toString().trim();
            String confirmar = etConfirmarSenha.getText().toString().trim();

            if (senha.isEmpty()) {
                etSenha.setError("Preencha a senha");
                etSenha.requestFocus();
                return;
            }
            if (senha.length() < 8) {
                etSenha.setError("A senha deve ter no mínimo 8 caracteres");
                etSenha.requestFocus();
                return;
            }
            if (confirmar.isEmpty()) {
                etConfirmarSenha.setError("Confirme sua senha");
                etConfirmarSenha.requestFocus();
                return;
            }
            if (!senha.equals(confirmar)) {
                etConfirmarSenha.setError("As senhas não coincidem");
                etConfirmarSenha.requestFocus();
                return;
            }
            if (!termosAceitos) {
                Toast.makeText(this, "Você precisa aceitar os Termos de Uso e a Política de Privacidade", Toast.LENGTH_SHORT).show();
                return;
            }

            RetrofitClient.getApi().cadastro(nome, emailCadastro, senha, curso, universidade)
                    .enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            ApiResponse body = response.body();
                            if (body != null && body.sucesso) {
                                Toast.makeText(SignupStep2Activity.this, body.mensagem, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignupStep2Activity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            } else {
                                String msg = (body != null) ? body.mensagem : "Erro ao criar conta";
                                Toast.makeText(SignupStep2Activity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            Toast.makeText(SignupStep2Activity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
