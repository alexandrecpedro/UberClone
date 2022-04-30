package com.app.uber.helper;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.app.uber.activity.PassageiroActivity;
import com.app.uber.activity.RequisicoesActivity;
import com.app.uber.config.ConfiguracaoFirebase;
import com.app.uber.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class UsuarioFirebase {

    /** Métodos **/
    // Recuperar usuário
    public static FirebaseUser getUsuarioAtual(){
        FirebaseAuth usuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return usuario.getCurrentUser();
    }

    public static Usuario getDadosUsuarioLogado() {
        FirebaseUser firebaseUser = getUsuarioAtual();
        Usuario usuario = new Usuario();
        usuario.setId(firebaseUser.getUid());
        usuario.setEmail(firebaseUser.getEmail());
        usuario.setNome(firebaseUser.getDisplayName());

        return usuario;
    }

    // Atualizar nome do usuário
    public static boolean atualizarNomeUsuario(String nome){
        try {
            // Recuperar Firebase user
            FirebaseUser user = getUsuarioAtual();
            // Atualizar
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build();
            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        Log.d("Perfil", "Erro ao atualizar nome de perfil.");
                    }
                }
            });
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    // Redirecionar usuário logado
    public static void redirecionaUsuarioLogado(final Activity activity) {
        // Recuperando usuário atual
        FirebaseUser user = getUsuarioAtual();
        // Se há um usuário autenticado/logado, redireciona. Senão, não redireciona
        if (user != null) {
            DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                    .child("usuarios")
                    .child(getIdentificadorUsuario());
            // Pesquisa
            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    // Acessar tipo usuário
                    String tipoUsuario = usuario.getTipo();
                    // Verificar tipo
                    if (tipoUsuario.equals("M")) {
                        Intent i = new Intent(activity, RequisicoesActivity.class);
                        activity.startActivity(i);
                    } else {
                        Intent i = new Intent(activity, PassageiroActivity.class);
                        activity.startActivity(i);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    // Retornar id de um usuário presente no BD
    public static String getIdentificadorUsuario() {
        return getUsuarioAtual().getUid();
    }
}
