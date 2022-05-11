package com.app.uber.model;

import com.app.uber.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Requisicao {
    // Attributes
    private String id;
    private String status;
    private Usuario passageiro;
    private Usuario motorista;
    private Destino destino;

    //usuário chama um Uber
    public static final String STATUS_AGUARDANDO = "aguardando";
    //motorista a caminho do usuario
    public static final String STATUS_A_CAMINHO = "acaminho";
    //em viagem
    public static final String STATUS_VIAGEM = "viagem";
    //viagem finalizada
    public static final String STATUS_FINALIZADA = "finalizada";
    //viagem encerrada
    public static final String STATUS_ENCERRADA = "encerrada";
    //viagem cancelada
    public static final String STATUS_CANCELADA = "cancelada";

    // Constructor
    public Requisicao() {
    }

    // Methods
    public void salvar() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        // definindo nó de requisições
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");
        // definir id requisição
        String idRequisicao = requisicoes.push().getKey();
        setId(idRequisicao);
        // salvando todos os dados da requisição no Firebase
        requisicoes.child(getId()).setValue(this);
    }

    /** Atualizar apenas os dados do motorista e o status **/
    public void atualizar() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        // definindo nó de requisições
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        // única requisição
        DatabaseReference requisicao = requisicoes.child(getId());

        // atualizando apenas o nó "motorista"
        Map objeto = new HashMap();
        objeto.put("motorista", getMotorista());
        objeto.put("status", getStatus());

        requisicao.updateChildren(objeto);
    }

    /** Atualizar o status de cada requisição conforme mudam as condições **/
    public void atualizarStatus(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        DatabaseReference requisicao = requisicoes.child(getId());

        Map objeto = new HashMap();
        objeto.put("status", getStatus());

        requisicao.updateChildren(objeto);
    }

    /** Atualizar a localização do motorista **/
    public void atualizarLocalizacaoMotorista() {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = firebaseRef
                .child("requisicoes");

        DatabaseReference requisicao = requisicoes
                .child(getId())
                .child("motorista");

        Map objeto = new HashMap();
        objeto.put("latitude", getMotorista().getLatitude());
        objeto.put("longitude", getMotorista().getLongitude());

        requisicao.updateChildren(objeto);
    }

    // Getters/Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Usuario getMotorista() {
        return motorista;
    }

    public void setMotorista(Usuario motorista) {
        this.motorista = motorista;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }
}
