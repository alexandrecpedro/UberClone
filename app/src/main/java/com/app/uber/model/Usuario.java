package com.app.uber.model;

import com.app.uber.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Usuario implements Serializable {
    // Attributes
    private String id;
    private String nome;
    private String email;
    private String senha;
    private String tipo;

    private String latitude;
    private String longitude;

    // Constructor
    public Usuario() {}

    // Methods
    public void salvar(){
        // Referência do BD
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        // Criando nó "usuarios" e nó "idUsuario"
        DatabaseReference usuarios = firebaseRef.child("usuarios").child(getId());

        // Configurando usuário no BD (salvando todos os atributos, menos a senha = this)
        usuarios.setValue(this);
    }

    // Getters/Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
