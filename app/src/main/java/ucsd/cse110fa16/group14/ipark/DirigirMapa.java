package ucsd.cse110fa16.group14.ipark;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
/*
Sources:
  http://stackoverflow.com/questions/2441203/how-to-make-an-android-app-return-to-the-last-open-activity-when-relaunched
 */
public class DirigirMapa extends AppCompatActivity {


    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences prefs = getSharedPreferences("X", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lastActivity", getClass().getName());
        editor.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dirigir_mapa);

        // help button and status of the parking lot
        Button helpButt = (Button) findViewById(R.id.help);
        Button statusButt = (Button) findViewById(R.id.status);
        TextView verticalNums = (TextView) findViewById(R.id.verticalIndices);
        //Button priceChanger = (Button) findViewById(R.id.priceChanger);
        String text = "\r\n00\r"+"\t10\r"+"\n20\r"+"\n30\r"+"\n\n40\r"+"\t50\r"+"\n60\r"+"\n70";
        verticalNums.setText(text);


        /* information page */
        helpButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder hlp = new AlertDialog.Builder(DirigirMapa.this);
                hlp.setTitle("Instrucción");
                hlp.setMessage(

                        "Verde  - Disponible\n" +
                                "Amarillo - Ocupado\n" +
                                "Blanco  - Propietario\n" +
                                "Rojo     -  Ilegal\n" +
                                "Mantenga pulsados los puntos para cambiar.\n"
                );
                hlp.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


                AlertDialog alertDialog = hlp.create();
                alertDialog.show();

            }
        });

        statusButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder hlp = new AlertDialog.Builder(DirigirMapa.this);
                hlp.setTitle("Estado de estacionamiento");

                int ownerReserved = 0;
                int available = 0;
                int occupied = 0;
                int illegalParking = 0;
                String availStr = "";
                String occupyStr = "";
                String reserveStr = "";
                String illegalStr = "";

                long curTimeInSec = iLink.getCurTimeInSec();
                int parkingLotStatus[] = iLink.getParkingLotStatus(curTimeInSec, curTimeInSec);

                for (int i = 0; i < iLink.NUM_SPOTS; i++) {
                    if (parkingLotStatus[i] == iLink.AVAILABLE)
                        available++;
                    else if (parkingLotStatus[i] == iLink.OCCUPIED)
                        occupied++;
                    else if (parkingLotStatus[i] == iLink.OWNER_RESERVED)
                        ownerReserved++;
                    else
                        illegalParking++;
                }

                availStr = String.format("%02d", available);
                occupyStr = String.format("%02d", occupied);
                reserveStr = String.format("%02d", ownerReserved);
                illegalStr = String.format("%02d", illegalParking);

                hlp.setMessage("Estacionamiento disponible: " + availStr +
                        "\nOcupado:              " + occupyStr +
                        "\nPropietario Reserva:   " + reserveStr +
                        "\nEstacionamiento Ilegal:      " + illegalStr + "\n");

                hlp.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = hlp.create();
                alertDialog.show();

            }
        });

    }
}
