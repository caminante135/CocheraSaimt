package ucsd.cse110fa16.group14.ipark;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Dennis on 10/31/2016.
 */
/*
Sources:
  http://stackoverflow.com/questions/2441203/how-to-make-an-android-app-return-to-the-last-open-activity-when-relaunched
  https://www.youtube.com/watch?v=tOn5HsQPhUY&t=6s
 */
public class Emergencia extends AppCompatActivity {

    private Firebase root;
    Firebase newEmergChild, emergHistChild;
    RadioGroup radioGroup;
    RadioButton selected;
    private static FirebaseAuth auth;

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
        setContentView(R.layout.activity_emergencia);

        root = new Firebase("https://ipark-e243b.firebaseio.com");
        final Firebase newEmerg = root.child("NewEmergency");
        final Firebase emergHist = root.child("EmergencyHistory");
        final Bundle bundle = getIntent().getExtras();

        Button sendButt = (Button) findViewById(R.id.button25);
        Button cancelButt = (Button) findViewById(R.id.button23);
        Button helpButt = (Button) findViewById(R.id.button28);

        radioGroup = (RadioGroup) findViewById(R.id.emergencyRadioGroup);
        final EditText parkingNum = (EditText) findViewById(R.id.parkingNumber);
        auth = FirebaseAuth.getInstance();
        final String userName = auth.getCurrentUser().getDisplayName();  // get current user's username

        /* emergency sent */
        sendButt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int selectedButton = radioGroup.getCheckedRadioButtonId();
                selected = (RadioButton) findViewById(selectedButton);
                String selectedbButton = "N/A";
                String parkingNumber = "N/A";

                // get the current date
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                sdf.format(date);

                // creating child fields for twin emergency records simultaneously
                newEmergChild = newEmerg.child(date.toString());
                emergHistChild = emergHist.child(date.toString());
                Firebase newEmEmergencyType = newEmergChild.child("EmergencyType");
                Firebase emHistEmergencyType = emergHistChild.child("EmergencyType");
                Firebase newEmDateChild = newEmergChild.child("Date");
                Firebase emHistDateChild = emergHistChild.child("Date");
                Firebase newEmParkingNumChild = newEmergChild.child("ParkingNumber");
                Firebase emHistParkingNumChild = emergHistChild.child("ParkingNumber");
                Firebase newEmUserChild = newEmergChild.child("User");
                Firebase emHistUserChild = emergHistChild.child("User");


                try {
                    selectedbButton = selected.getText().toString();
                    parkingNumber = parkingNum.getText().toString();

                    // entering emergency records
                    newEmEmergencyType.setValue(selectedbButton);
                    emHistEmergencyType.setValue(selectedbButton);
                    newEmDateChild.setValue(sdf.format(date));
                    emHistDateChild.setValue(sdf.format(date));

                    if (parkingNumber.isEmpty()) {
                        newEmParkingNumChild.setValue("N/A");
                        emHistParkingNumChild.setValue("N/A");
                    } else {
                        newEmParkingNumChild.setValue(parkingNumber);
                        emHistParkingNumChild.setValue(parkingNumber);
                    }
                    newEmUserChild.setValue(userName);  // set username for new emergency
                    emHistUserChild.setValue(userName); // set username for emergency history
                    parkingNum.setText("");

                    // display message after reporting an emergency
                    Toast.makeText(Emergencia.this, "¡Gracias por reportar esta emergencia!",
                            Toast.LENGTH_LONG).show();
                }
                catch (NullPointerException e) {
                    Toast.makeText(Emergencia.this, "Seleccione una opción.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        /* cancel and go to home page */
        cancelButt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Emergencia.this, PaginaUsuario.class);
                startActivity(intent);

            }
        });


        /* information in help button */
        helpButt.setOnClickListener(new View.OnClickListener() {

            // should set the corresponding parking spot to red
            // TO DO


            // then add this log to history
            // TO DO


            // and then pop out a window indicating success report
            @Override
            public void onClick(View v) {
                AlertDialog.Builder respond = new AlertDialog.Builder(Emergencia.this);
                respond.setTitle("Instrucción");
                respond.setMessage("\t\t\t\tEn caso de incendio, salga del estacionamiento\n" +
                        "\t\t\t\tEn caso de lesión, nuestro equipo médico esta en camino\n" +
                        "\t\t\t\tEn caso de ataque cardiaco marque 105\n" +
                        "\t\t\t\tEn caso de alguna pérdida,reportar al guardia " +
                        "que se encuentra en la entrada" +
                        "\t\t\t\tEn caso de falla de energia del vehiculo, " +
                        "el guardia de seguridad lo ayudara");
                respond.setPositiveButton("No me gusta", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                respond.setNegativeButton("Ayuda!", null);

                AlertDialog alertDialog = respond.create();
                alertDialog.show();

            }


        });

    }
}
