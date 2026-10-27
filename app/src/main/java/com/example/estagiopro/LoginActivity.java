package com.example.estagiopro;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.estagiopro.api.ApiResponse;
import com.example.estagiopro.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etSenha;
    private ImageView ivTogglePassword;
    private boolean senhaVisivel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        TextView btnEntrar = findViewById(R.id.btnEntrar);
        TextView tvCadastrese = findViewById(R.id.tvCadastrese);

        // Mostrar/ocultar senha
        ivTogglePassword.setOnClickListener(v -> {
            senhaVisivel = !senhaVisivel;
            if (senhaVisivel) {
                etSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etSenha.setSelection(etSenha.getText().length());
        });

        // Botão Entrar: valida se os campos não estão vazios
        btnEntrar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Preencha o e-mail institucional");
                etEmail.requestFocus();
                return;
            }
            if (senha.isEmpty()) {
                etSenha.setError("Preencha a senha");
                etSenha.requestFocus();
                return;
            }

            RetrofitClient.getApi().login(email, senha).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    ApiResponse body = response.body();
                    if (body != null && body.sucesso) {
                        Toast.makeText(LoginActivity.this, body.mensagem, Toast.LENGTH_SHORT).show();
                        // TODO: navegar para a tela principal do app
                    } else {
                        String msg = (body != null) ? body.mensagem : "Erro no login";
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Ao clicar em "Cadastre-se", vai para a tela 2 (Crie sua conta)
        tvCadastrese.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupStep1Activity.class);
            startActivity(intent);
        });
    }
}
