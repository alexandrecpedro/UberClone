package com.app.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.app.uber.config.ConfiguracaoFirebase;
import com.app.uber.model.Destino;
import com.app.uber.model.Requisicao;
import com.app.uber.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.app.uber.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class CorridaActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    //Componentes
    private Button buttonAceitarCorrida;
    private FloatingActionButton fabRota;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localMotorista;
    private LatLng localPassageiro;
    private Usuario motorista;
    private Usuario passageiro;
    private String idRequisicao;
    private Requisicao requisicao;
    private DatabaseReference firebaseRef;
    private Marker marcadorMotorista;
    private Marker marcadorPassageiro;
    private Destino destino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida);

        inicializarComponentes();

        //Recupera dados do usuário
        if (getIntent().getExtras().containsKey("idRequisicao")
                && getIntent().getExtras().containsKey("motorista")) {
            Bundle extras = getIntent().getExtras();
            // recuperando o objeto motorista
            motorista = (Usuario) extras.getSerializable("motorista");
            // recuperando idRequisicao
            idRequisicao = extras.getString("idRequisicao");
            //confere status da requisição
            verificaStatusRequisicao();
        }
    }

    private void verificaStatusRequisicao() {
        // pegando uma requisição pelo id
        DatabaseReference requisicoes = firebaseRef.child("requisicoes")
                .child(idRequisicao);
        requisicoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Recupera requisição (destino, motorista, passageiro, etc)
                requisicao = dataSnapshot.getValue(Requisicao.class);
                //Recuperando dados e local do passageiro
                passageiro = requisicao.getPassageiro();
                localPassageiro = new LatLng(
                        Double.parseDouble(passageiro.getLatitude()),
                        Double.parseDouble(passageiro.getLongitude())
                );
                // alteracao de interface mediante status recebido
                switch (requisicao.getStatus()) {
                    case Requisicao.STATUS_AGUARDANDO:
                        requisicaoAguardando();
                        break;
                    case Requisicao.STATUS_A_CAMINHO:
                        requisicaoACaminho();
                        break;

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void requisicaoAguardando() {
        buttonAceitarCorrida.setText("Aceitar corrida");
    }

    /** Motorista consegue visualizar a distância do passageiro **/
    private void requisicaoACaminho() {
        buttonAceitarCorrida.setText("A caminho do passageiro");

        //Exibe marcador do motorista
        adicionaMarcadorMotorista(localMotorista, motorista.getNome());

        //Exibe marcador do passageiro
        adicionaMarcadorPassageiro(localPassageiro, passageiro.getNome());

        //Centralizar dois marcadores
        centralizarDoisMarcadores(marcadorMotorista, marcadorPassageiro);
    }

    private void centralizarDoisMarcadores(Marker marcador1, Marker marcador2) {
        /** Definir quais marcadores serão exibidos na tela **/
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(marcador1.getPosition());
        builder.include(marcador2.getPosition());

        // Bounds = limites entre marcadores
        LatLngBounds bounds = builder.build();

        // Características da tela do dispositivo
        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int) (largura * 0.20);

        // Câmera centraliza conforme os bounds fornecidos
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, largura, altura, espacoInterno)
        );
    }

    private void adicionaMarcadorMotorista(LatLng localizacao, String titulo) {
        /* se marcador do motorista não está null,
        * remove marcador antes de add um novo */
        if (marcadorMotorista != null)
            marcadorMotorista.remove();

        //adicionar novo marcador
        marcadorMotorista = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
        );
    }

    private void adicionaMarcadorPassageiro(LatLng localizacao, String titulo) {
        /* se marcador do passageiro não está null,
         * remove marcador antes de add um novo */
        if (marcadorPassageiro != null)
            marcadorPassageiro.remove();

        //adicionar novo marcador
        marcadorPassageiro = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
        );
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Recuperando a localização do usuário
        recuperarLocalizacaoUsuario();
    }

    /** Recuperando a localização do usuário **/
    private void recuperarLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //recuperar latitude e longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localMotorista = new LatLng(latitude, longitude);

                /** Add a marker in my location (meuLocal) and move the camera **/
                // Limpar mapa e exibir um único a cada movimentação do usuário
                mMap.clear();
                mMap.addMarker(
                        new MarkerOptions()
                                .position(localMotorista)
                                .title("Meu Local")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
                );
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(localMotorista, 20)
                );
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

        // Solicitar atualizações de localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000, // tempo = 10 segundos
                    10, // distancia = 10 metros
                    locationListener
            );
        }
    }

    public void aceitarCorrida(View view) {
        //Configura requisicao
        requisicao = new Requisicao();
        requisicao.setId(idRequisicao); //atualiza id da requisição
        requisicao.setMotorista(motorista); // salva na requisicao os dados do motorista
        requisicao.setStatus(Requisicao.STATUS_A_CAMINHO); // motorista está a caminho
        // atualizar requisicao no BD
        requisicao.atualizar();

    }

    private void inicializarComponentes() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Iniciar corrida");

        buttonAceitarCorrida = findViewById(R.id.buttonAceitarCorrida);

        //Configurações iniciais
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
}