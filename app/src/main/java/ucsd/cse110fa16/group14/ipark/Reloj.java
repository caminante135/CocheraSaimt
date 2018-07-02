package ucsd.cse110fa16.group14.ipark;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
/*
Sources:
  http://stackoverflow.com/questions/2441203/how-to-make-an-android-app-return-to-the-last-open-activity-when-relaunched
 */
public class Reloj extends AppCompatActivity {
    private boolean hasReservation;
    private int min, hour, curMin, curHour, ampm;

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences prefs = getSharedPreferences("X", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lastActivity", getClass().getName());
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        // if back button is pressed, send user to User Homepage
        Intent intent = new Intent(Reloj.this, UserHomepage.class);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reloj);
        final Bundle bundle = getIntent().getExtras();
        hasReservation = false;

        // Redirect user back to home page or status page if they HAVE reserved a spot
        if (bundle.getInt("departHour") != 0 || bundle.getInt("departMin") != 0) {
            hasReservation = true;
            AlertDialog.Builder alertNotReserved = new AlertDialog.Builder(Reloj.this);
            alertNotReserved.setTitle("Ya hice un pedido");
            alertNotReserved.setMessage("¿Le gustaría ver el estado de su reserva?");
            alertNotReserved.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Reloj.this, conteo.class);
                    intent.putExtra("arriveHour", bundle.getInt("arriveHour"));
                    intent.putExtra("arriveMin", bundle.getInt("arriveMin"));
                    intent.putExtra("departHour", bundle.getInt("departHour"));
                    intent.putExtra("departMin", bundle.getInt("departMin"));
                    intent.putExtra("spotAssign", bundle.getString("spotAssign"));
                    startActivity(intent);
                    dialog.cancel();
                }

            });
            alertNotReserved.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Reloj.this, UserHomepage.class);
                    startActivity(intent);
                    dialog.cancel();
                }

            });
            //Handle when back is pressed
            alertNotReserved.setOnKeyListener(new AlertDialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode,
                                     KeyEvent event) {

                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        finish();
                    }
                    return true;
                }
            });
            AlertDialog alertDialog = alertNotReserved.create();
            alertDialog.show();
        }


        ImageButton homeButt = (ImageButton) findViewById(R.id.clockinHomeButton);
        Button help = (Button) findViewById(R.id.button4);
        Button nextButt = (Button) findViewById(R.id.next);

        NumberPicker hourNumPick = (NumberPicker) findViewById(R.id.hour);
        NumberPicker minNumPick = (NumberPicker) findViewById(R.id.min);
        NumberPicker amPmPick = (NumberPicker) findViewById(R.id.amPM);
        final String[] s = {"AM", "PM"};

        //Populate values from minimum and maximum value range
        hourNumPick.setMinValue(1);
        minNumPick.setMinValue(0);
        amPmPick.setMinValue(1);

        //Specify the maximum value/number
        hourNumPick.setMaxValue(12);
        minNumPick.setMaxValue(59);
        amPmPick.setMaxValue(2);

        // Display text
        amPmPick.setDisplayedValues(s);

        //Gets whether the selector wheel wraps when reaching the min/max value.
        hourNumPick.setWrapSelectorWheel(true);
        minNumPick.setWrapSelectorWheel(true);

        // Initialize start time
        hour = 1;
        min = 0;
        ampm = 1;

        //Set a value change listener for hourNumPick
        hourNumPick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                hour = newVal;
            }
        });

        //Set a value change listener for minNumPick
        minNumPick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                min = newVal;
            }
        });

        //Set a value change listener for minNumPick
        amPmPick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                ampm = newVal;                       // 1 = AM, 2 = PM
            }
        });

        // Check to see if time entered is valid before continuing (after current time)
        nextButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int hourEntered, minEntered;
                hourEntered = hour;
                minEntered = min;

               /* if (hour == 12 && ampm == 1) {
                    hourEntered = 0;
                }*/

                if ((ampm == 2) && hourEntered != 12)
                    hourEntered += 12;

                if ((ampm == 1) && hourEntered == 12)
                    hourEntered = 0;

                Date date = new Date();                               // given date
                Calendar calendar = GregorianCalendar.getInstance();  // creates a new calendar instance
                calendar.setTime(date);                               // assigns calendar to given date
                curHour = calendar.get(Calendar.HOUR_OF_DAY);         // gets hour in 24h format
                curMin = calendar.get(Calendar.MINUTE);              // get cur minute

                System.out.println("Tiempo Actual: " + curHour + ":" + curMin);
                System.out.println("Tiempo Ingresado: " + hourEntered + ":" + minEntered);

                if (curHour == 24) curHour -= 24;
                if (hourEntered < curHour) {
                    AlertDialog.Builder invalidTimeAlert = new AlertDialog.Builder(Reloj.this);
                    invalidTimeAlert.setTitle("Tiempo inválido");
                    invalidTimeAlert.setMessage(
                            "La hora de llegada no puede ser anterior a la hora actual.");
                    invalidTimeAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alertDialog = invalidTimeAlert.create();
                    alertDialog.show();
                } else if ((hourEntered == curHour) && (minEntered < curMin)) {
                    AlertDialog.Builder invalidTimeAlert = new AlertDialog.Builder(Reloj.this);
                    invalidTimeAlert.setTitle("Tiempo inválido");
                    invalidTimeAlert.setMessage(
                            "La hora de llegada no puede ser anterior a la hora actual.");
                    invalidTimeAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alertDialog = invalidTimeAlert.create();
                    alertDialog.show();
                } else {
                    Intent intent = new Intent(Reloj.this, ElegirHoraSalidaActivity.class);
                    intent.putExtra("arriveHour", hourEntered);
                    intent.putExtra("arriveMin", minEntered);
                    startActivity(intent);
                }
            }
            //@Override
            //public void onClick(View v) {
            //Intent intent = new Intent(Reloj.this, Payment.class);
            //startActivity(intent);


            //}
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder hlp = new AlertDialog.Builder(Reloj.this);
                hlp.setTitle("Información de Ayuda");
                hlp.setMessage(
                        "Por favor,elija la hora que espera llegar. " +
                                "La hora de llegada no puede ser anterior a la hora actual." +
                                "Presione siguiente una vez que haya terminado.");
                hlp.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = hlp.create();
                alertDialog.show();
            }
        });


        homeButt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Reloj.this, UserHomepage.class);
                startActivity(intent);
            }
        });
    }
}
