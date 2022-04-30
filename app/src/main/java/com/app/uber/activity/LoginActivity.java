package com.app.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.app.uber.R;
import com.app.uber.config.ConfiguracaoFirebase;
import com.app.uber.helper.UsuarioFirebase;
import com.app.uber.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {
    // Attributes
    private TextInputEditText campoEmail, campoSenha;
    // objeto autenticação
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar componentes
        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);
    }

    public void validarLoginUsuario(View view){
        // Recuperar textos dos campos
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        // Verifica e-mail
        if (!textoEmail.isEmpty()) {
            // Verifica senha
            if (!textoSenha.isEmpty()) {
                // Instanciando objeto usuário
                Usuario usuario = new Usuario();
                usuario.setEmail(textoEmail);
                usuario.setSenha(textoSenha);

                // Autenticar usuario
                logarUsuario(usuario);
            } else {
                Toast.makeText(LoginActivity.this,
                        "Preencha a senha!",
                        Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(LoginActivity.this,
                    "Preencha o email!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void logarUsuario(Usuario usuario) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // Validar se login foi realizado com sucesso
                if (task.isSuccessful()) {
                    try {
                        //Verificar o tipo de usuário logado
                        // "Motorista" / "Passageiro"
                        UsuarioFirebase.redirecionaUsuarioLogado(LoginActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    String excecao = "";
                    try {
                        throw task.getException();
                    } catch ( FirebaseAuthInvalidUserException e) {
                        excecao = "Usuário não está cadastrado.";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        excecao = "E-mail e senha não correspondem a um usuário cadastrado";
                    } catch (Exception e) {
                        excecao = "Erro ao cadastrar usuário: "  + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this,
                            excecao,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}