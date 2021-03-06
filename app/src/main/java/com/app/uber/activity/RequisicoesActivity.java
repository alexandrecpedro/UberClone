package com.app.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.app.uber.R;
import com.app.uber.adapter.RequisicoesAdapter;
import com.app.uber.config.ConfiguracaoFirebase;
import com.app.uber.helper.RecyclerItemClickListener;
import com.app.uber.helper.UsuarioFirebase;
import com.app.uber.model.Requisicao;
import com.app.uber.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequisicoesActivity extends AppCompatActivity {

    //Componentes
    private RecyclerView recyclerRequisicoes;
    private TextView textResultado;

    private FirebaseAuth autenticacao;
    private DatabaseReference firebaseRef;
    private List<Requisicao> listaRequisicoes = new ArrayList<>();
    private RequisicoesAdapter adapter;
    private Usuario motorista;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requisicoes);

        inicializarComponentes();

        //Recuperar localizacao do usu??rio
        recuperarLocalizacaoUsuario();
    }

    @Override
    protected void onStart() {
        super.onStart();
        verificaStatusRequisicao();
    }

    /** Verificando a situa????o (status) da requisi????o **/
    private void verificaStatusRequisicao() {
        // Recuperando dados do motorista logado
        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        // Recuperando os dados das requisicoes, ordenadas por motorista e pelo id do motoristaLogado
        Query requisicoesPesquisa = requisicoes.orderByChild("motorista/id")
                .equalTo(usuarioLogado.getId());

        // Fazendo uma pesquisa espec??fica
        requisicoesPesquisa.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // recuperando os itens
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    /* TODO Para n??o ter v??rias requisi????es, o ideal ?? criar um n??
                    com apenas uma requisi????o ativa, identificada por id.
                    Ao cancelar essa requisi????o, bastaria apenas remov??-la do n?? */
                    Requisicao requisicao = ds.getValue(Requisicao.class);

                    // testando o status dessa requisi????o
                    // visa enviar usu??rio para CorridaActivity => STATUS_A_CAMINHO ou STATUS_VIAGEM
                    if (requisicao.getStatus().equals(Requisicao.STATUS_A_CAMINHO)
                            || requisicao.getStatus().equals(Requisicao.STATUS_VIAGEM)
                            || requisicao.getStatus().equals(Requisicao.STATUS_FINALIZADA)) {
                        /* recuperar os dados do motorista (enviando para a CorridaActivity,
                        motorista j?? configurado) */
                        motorista = requisicao.getMotorista();
                        abrirTelaCorrida(requisicao.getId(), motorista, true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /** Recuperando a localiza????o do usu??rio **/
    private void recuperarLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //recuperar latitude e longitude
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());

                //Atualizar GeoFire
                UsuarioFirebase.atualizarDadosLocalizacao(
                        location.getLatitude(),
                        location.getLongitude()
                );

                if (!latitude.isEmpty() && !longitude.isEmpty()) {
                    motorista.setLatitude(latitude);
                    motorista.setLongitude(longitude);

                    // TODO exibir uma tela carregando. Ap??s dado estar carregado, exibir carregamento
                    /* Apenas adicionando o evento de clique no RecyclerView somente
                    ap??s ter (latitude, longitude) do motorista */
                    adicionaEventoCliqueRecyclerView();
                    locationManager.removeUpdates(locationListener);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // Receber apenas a primeira atualiza????o do usu??rio
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0, // tempo = 0 segundo
                    0, // distancia = 0 metro
                    locationListener
            );
        }
    }

    /** Configurando menu **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // pegando endere??o do menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // deslogar o usu??rio
            case R.id.menuSair :
                autenticacao.signOut();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void abrirTelaCorrida(String idRequisicao, Usuario motorista, boolean requisicaoAtiva) {
        Intent i = new Intent(RequisicoesActivity.this, CorridaActivity.class);
        i.putExtra("idRequisicao", idRequisicao);
        i.putExtra("motorista", motorista);
        i.putExtra("requisicaoAtiva", requisicaoAtiva);
        startActivity(i);
    }

    private void inicializarComponentes() {

        getSupportActionBar().setTitle("Requisi????es");

        //Configura componentes
        recyclerRequisicoes = findViewById(R.id.recyclerRequisicoes);
        textResultado = findViewById(R.id.textResultado);

        //Configura????es iniciais
        motorista = UsuarioFirebase.getDadosUsuarioLogado();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        //Configurar RecyclerView
        adapter = new RequisicoesAdapter(listaRequisicoes, getApplicationContext(), motorista);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerRequisicoes.setLayoutManager(layoutManager);
        recyclerRequisicoes.setHasFixedSize(true);
        recyclerRequisicoes.setAdapter(adapter);

        recuperarRequisicoes();
    }

    private void adicionaEventoCliqueRecyclerView() {
        //Adiciona evento de clique no recycler
        recyclerRequisicoes.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerRequisicoes,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                /* quando usuario clicar, recuperamos
                                 * a lista de requisicoes */
                                Requisicao requisicao = listaRequisicoes.get(position);
                                abrirTelaCorrida(requisicao.getId(), motorista, false);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );
    }

    private void recuperarRequisicoes() {
        DatabaseReference requisicoes = firebaseRef.child("requisicoes");

        Query requisicaoPesquisa = requisicoes.orderByChild("status")
                .equalTo(Requisicao.STATUS_AGUARDANDO);

        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // verificando se h?? requisicoes
                if (dataSnapshot.getChildrenCount() > 0) {
                    textResultado.setVisibility(View.GONE);
                    recyclerRequisicoes.setVisibility(View.VISIBLE);
                } else {
                    textResultado.setVisibility(View.VISIBLE);
                    recyclerRequisicoes.setVisibility(View.GONE);
                }

                /** Lista de requisi????es **/
                // atualizando (limpando Requisicoes.STATUS_AGUARDANDO)
                listaRequisicoes.clear();
                //recuperar as requisicoes
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Requisicao requisicao = ds.getValue(Requisicao.class);
                    listaRequisicoes.add(requisicao);

                    //salvando lista de requisicoes
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}