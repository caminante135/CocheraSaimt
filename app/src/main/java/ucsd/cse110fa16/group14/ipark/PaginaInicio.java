package ucsd.cse110fa16.group14.ipark;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Mag on 10/19/2016.
 */

public class PaginaInicio extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagina_inicio);
        final Firebase root = new Firebase("https://ipark-e243b.firebaseio.com");

        final ProgressDialog progress = new ProgressDialog(PaginaInicio.this);
        ImageButton map1 = (ImageButton) findViewById(R.id.map1);
        ImageButton settings = (ImageButton) findViewById(R.id.settings);
        ImageButton reviews = (ImageButton) findViewById(R.id.reviews);
        ImageButton compose = (ImageButton) findViewById(R.id.compose);
        ImageButton emergency = (ImageButton) findViewById(R.id.emergency1);
        ImageButton logout = (ImageButton) findViewById(R.id.logout);

        // Update illegal parking bubble on map
        final TextView illegalBubble = (TextView) findViewById(R.id.bubble);
        final TextView notificationAlert = (TextView) findViewById(R.id.AttentionNotification);
        long curTimeInSec = iLink.getCurTimeInSec();
        final int [] parkingLotStatus = iLink.getParkingLotStatus(curTimeInSec, curTimeInSec);
        Firebase parkingLotDB = new Firebase("https://ipark-e243b.firebaseio.com/ParkingLot");
        parkingLotDB.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {

                long curTimeInSec = iLink.getCurTimeInSec();
                final int [] parkingLotStatus = iLink.getParkingLotStatus(curTimeInSec, curTimeInSec);

                int illegalCount = 0;
                for( int i = 0; i < parkingLotStatus.length; i++ )
                  if( parkingLotStatus[i] == iLink.ILLEGAL )
                      illegalCount++;

                if( illegalCount != 0 ) {
                    notificationAlert.setVisibility(View.VISIBLE);
                    illegalBubble.setVisibility(View.VISIBLE);
                    illegalBubble.setText( String.valueOf(illegalCount) );
                }
                else {
                    illegalBubble.setVisibility(View.GONE);
                    notificationAlert.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.v("NO ACCESS ERROR", "Could not connect to Firebase");
            }
        });

        root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("NewEmergency")) {
                    startActivity(new Intent(PaginaInicio.this, Mostrar.class));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        map1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                root.child("NewEmergency").removeValue();

                Intent intent = new Intent(PaginaInicio.this, DirigirMapa.class);
                progress.show();
                progress.setMessage("Cargando....");
                startActivity(intent);
                progress.dismiss();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                root.child("NewEmergency").removeValue();

                Intent intent = new Intent(PaginaInicio.this, Configuracion.class);
                ProgressDialog progress = new ProgressDialog(PaginaInicio.this);
                progress.show();
                iLink.defaultPrice = iLink.getDefaultPrice();
                progress.setMessage("Cargando....");
                startActivity(intent);
                progress.dismiss();

            }
        });

        reviews.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                root.child("NewEmergency").removeValue();

                Intent intent = new Intent(PaginaInicio.this, CuentaRegresiva.class);
                startActivity(intent);
            }
        });

        compose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                root.child("NewEmergency").removeValue();

                Intent intent = new Intent(PaginaInicio.this, MensajesUsuariosActivity.class);
                startActivity(intent);
            }
        });

        emergency.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                root.child("NewEmergency").removeValue();

                Intent intent = new Intent(PaginaInicio.this, DirigirEmergencia.class);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                root.child("NewEmergency").removeValue();

                AlertDialog.Builder Quest = new AlertDialog.Builder(PaginaInicio.this);
                Quest.setTitle("Cerrar Sesión");
                Quest.setMessage(
                        "¿Estas seguro de cerrar sesión?");
                Quest.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                Quest.setNegativeButton("Log out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(PaginaInicio.this, InicioSesion.class);
                        FirebaseAuth.getInstance().signOut();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Toast.makeText(PaginaInicio.this, "Salida exitosa", Toast.LENGTH_LONG).show();
                        startActivity(intent);

                    }
                });
                AlertDialog alertDialog = Quest.create();
                alertDialog.show();
            }
        });
    }
}
