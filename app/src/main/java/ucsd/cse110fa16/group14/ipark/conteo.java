package ucsd.cse110fa16.group14.ipark;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Stack;
/*
Sources:
  http://stackoverflow.com/questions/2441203/how-to-make-an-android-app-return-to-the-last-open-activity-when-relaunched
 */
public class conteo extends AppCompatActivity {

    static Stack<String> parkingspots = new Stack<>();
    private ProgressBar myBar;
    private int barStatus;
    private Handler myHandler;

    private CountDownTimer myTimer = null;
    Firebase root;
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
    public void onBackPressed() {
        final Bundle bundle = getIntent().getExtras();
        Intent intent = new Intent(conteo.this, UserHomepage.class);
        intent.putExtra("arriveHour", bundle.getInt("arriveHour"));
        intent.putExtra("arriveMin", bundle.getInt("arriveMin"));
        intent.putExtra("departHour", bundle.getInt("departHour"));
        intent.putExtra("departMin", bundle.getInt("departMin"));
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta_regresiva);

        final Button checkoutButt = (Button) findViewById(R.id.countdownCheckoutButton);
        final Button reportButt = (Button) findViewById(R.id.countdownReportButton);
        Button emergencyButt = (Button) findViewById(R.id.countdownEmergencyButton);
        Button mapButt = (Button) findViewById(R.id.countdownMapButton);
        Button help = (Button) findViewById(R.id.countdownHelpButton);
        Button homeButt = (Button) findViewById(R.id.countdownHomeButton);
        int progressStatus = 0;

        // Get values passed on from previous activity
        final Bundle bundle = getIntent().getExtras();

        TextView startTimeText = (TextView) findViewById(R.id.StartTime);
        TextView endTimeText = (TextView) findViewById(R.id.EndTime);
        final TextView title = (TextView) findViewById(R.id.TimeRemainingTitle);
        final TextView timerText = (TextView) findViewById(R.id.Timer);
        final TextView pspot = (TextView) findViewById(R.id.textView26);

        auth = FirebaseAuth.getInstance();

        final String userName = auth.getCurrentUser().getDisplayName(); // get the current user's username
        root = new Firebase("https://ipark-e243b.firebaseio.com/Users/" + userName);

        // format the clockin and clockout times
        startTimeText.setText(generateTimeText(bundle.getInt("arriveHour"), bundle.getInt("arriveMin")));
        endTimeText.setText(generateTimeText(bundle.getInt("departHour"), bundle.getInt("departMin")));

        getParkingLotData();
        pspot.setText(bundle.getString("spotAssign"));

        // Express current, start, and end time in seconds
        final long curTimeInSec = getCurrentTimeInSec();
        final long startTimeInSec = ((bundle.getInt("arriveHour") * 60) + bundle.getInt("arriveMin")) * 60;
        final long endTimeInSec = ((bundle.getInt("departHour") * 60) + bundle.getInt("departMin")) * 60;

        // Set initial condition of progress bar
        myBar = (ProgressBar) findViewById(R.id.ProgressBar);
        myBar.setMax((int) (endTimeInSec - startTimeInSec));

        // Updates the timer every 1 second from current time
        if (myTimer != null)
        {
            myTimer.cancel();
        }
        myTimer = new CountDownTimer((endTimeInSec - curTimeInSec) * 1000, 1000) {

            public void onTick(long millisUntilFinished)
            {
                // Display a countdown until start
                boolean beforeStartTime = false;

                // current time is before start
                if ((millisUntilFinished / 1000) > (endTimeInSec - startTimeInSec))
                {
                    millisUntilFinished -= (endTimeInSec - startTimeInSec) * 1000;
                    title.setText("Tiempo hasta el comienzo");
                    timerText.setTextColor(Color.RED);
                    checkoutButt.setText("Cancelar");
                    reportButt.setEnabled(false);
                    myBar.setProgress(0);
                }
                // current time is in the reservation state
                else
                {
                    title.setText("Tiempo Restante");
                    timerText.setTextColor(Color.BLUE);
                    reportButt.setEnabled(true);
                    checkoutButt.setText("Pago");

                }

                // Display a countdown until time's up (after time start)

                long t_sec, t_min, t_hour;
                long timeLeft = millisUntilFinished / 1000;       // Gives Seconds

                t_sec = timeLeft % 60;
                timeLeft /= 60;                                 // Gives minutes

                t_min = timeLeft % 60;
                t_hour = timeLeft / 60;                           // Gives hours


                timerText.setText(String.format("%02d:%02d:%02d", t_hour, t_min, t_sec));
            }

            public void onFinish() {

                // Redirect user back to home page or clockin if they have not reserved a spot
                if ((bundle.getInt("departHour") == 0) && (bundle.getInt("departMin") == 0)) {
                    AlertDialog.Builder alertNotReserved = new AlertDialog.Builder(conteo.this);
                    alertNotReserved.setTitle("No hay pedido");
                    alertNotReserved.setMessage(" ¿Le gustaria hacer un pedido?");
                    alertNotReserved.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(conteo.this, Reloj.class);
                            intent.putExtra("arriveHour", 0);
                            intent.putExtra("arriveMin", 0);
                            intent.putExtra("departHour", 0);
                            intent.putExtra("departMin", 0);
                            startActivity(intent);

                            dialog.cancel();
                        }

                    });
                    alertNotReserved.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(conteo.this, UserHomepage.class);
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
                } else {

                    timerText.setText("00:00:00");
                    timerText.setTextColor(Color.RED);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    Date date = new Date();                               // given date
                    Calendar calendar = GregorianCalendar.getInstance();  // creates a new calendar instance
                    calendar.setTime(date);                               // assigns calendar to given date

                    // send user to the activity_revision page
                    Intent intent = new Intent(conteo.this, activity_revision.class);
                    intent.putExtra("arriveHour", bundle.getInt("arriveHour"));
                    intent.putExtra("arriveMin", bundle.getInt("arriveMin"));

                    int tempDephHour, tempDepMin;
                    int totHours, totMins;
                    double totPay, rate;
                    rate = iLink.userPrice;
                    date = null;

                    // set the correct date
                    try {
                        date = sdf.parse(sdf.format(new Date()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    tempDephHour = calendar.get(Calendar.HOUR_OF_DAY);  // get current hour
                    tempDepMin = calendar.get(Calendar.MINUTE);         // get current minute

                    // populate Firebase with Reloj, Clockout, User and Date data
                    root.child("History").child(date + " " + generateTimeText(bundle.getInt("arriveHour"), bundle.getInt("arriveMin"))).child("Reloj").setValue(
                            generateTimeText(bundle.getInt("arriveHour"), bundle.getInt("arriveMin")));
                    root.child("History").child(date + " " + generateTimeText(bundle.getInt("arriveHour"), bundle.getInt("arriveMin"))).child("User").setValue(userName);
                    root.child("History").child(date + " " + generateTimeText(bundle.getInt("arriveHour"), bundle.getInt("arriveMin"))).child("Date").setValue(sdf.format(date));
                    root.child("History").child(date + " " + generateTimeText(bundle.getInt("arriveHour"), bundle.getInt("arriveMin"))).child("Clockout").setValue(
                            generateTimeText(tempDephHour, tempDepMin));

                    // carry hour and minute to the next layout
                    intent.putExtra("departHour", calendar.get(Calendar.HOUR_OF_DAY));
                    intent.putExtra("departMin", calendar.get(Calendar.MINUTE));

                    // Calculate total time parked and total to pay
                    totHours = tempDephHour - bundle.getInt("arriveHour");
                    totMins = tempDepMin - bundle.getInt("arriveMin");
                    totPay = (totHours + (double) totMins / 60.0) * rate;
                    root.child("History").child(date + " " + generateTimeText(bundle.getInt("arriveHour"), bundle.getInt("arriveMin"))).child("Rate").setValue(String.format("$%.2f", totPay));
                    intent.putExtra("rate", String.format("S/%.2f", totPay));
                    String spot = pspot.getText().toString(); // get a parking spot

                    //?
                    root.child("Reserva").child(date + " " + generateTimeText(bundle.getInt("arriveHour"), bundle.getInt("arriveMin"))).removeValue();
                    startActivity(intent);
                }
            }
        }.start();

        // CODE NOT WORKING
        // Update status bar once start time has begun
        barStatus = 0;
        myHandler = new Handler();
        new Thread(new Runnable() {
            public void run() {
                while ( barStatus < 100) {

                    // If start timer hasn't start, progress bar at 100%. Else calculate percentage
                    if( getCurrentTimeInSec() < startTimeInSec )
                        barStatus = 0;
                    else
                        barStatus = (int) ( getCurrentTimeInSec() - startTimeInSec );


                    // Update the progress bar
                    myHandler.post(new Runnable() {
                        public void run() {
                            myBar.setProgress(barStatus);
                        }
                    });
                }
            }
        }).start();


        /* information page */
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder hlp = new AlertDialog.Builder(conteo.this);
                hlp.setTitle("Información de Ayuda");
                hlp.setMessage("\t\t\t\tEl temporizador comienza a la hora de llegada ingresada antes.\n" +
                        "\t\t\t\tClick en PAGAR para cerrar sesión y finalizar su reserva.\n" +
                        "\t\t\t\tClick en INFORMAR si hay un automovil en su lugar, " +
                        "y recibiras un nuevo espacio de estacionamiento.\n"
                        + "\t\t\t\tClick en MAPA para ver el mapa de espacio de estacionamiento.\n" +
                        "\t\t\t\tClick en EMERGENGIA en caso de cualquier emergencia.");
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


        /* check out and go to review */
        checkoutButt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (bundle.getInt("departHour") == 0 && bundle.getInt("departMin") == 0) {
                    AlertDialog.Builder respond = new AlertDialog.Builder(conteo.this);
                    respond.setTitle("No ha hecho un pedido");
                    respond.setMessage("El recibo no se puede generar antes de hacer un pedido");
                    respond.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }

                    });
                    AlertDialog alertDialog = respond.create();
                    alertDialog.show();
                } else {
                    Date tempDate = new Date();                               // given date
                    Calendar calendar = GregorianCalendar.getInstance();      // creates a new calendar instance
                    calendar.setTime(tempDate);                               // assigns calendar to given date

                    // get the current date
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    tempDate = null;
                    try {
                        tempDate = sdf.parse(sdf.format(new Date()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    final Date date = tempDate;

                    // generate Firebase path for current reservation to be changed to History record
                    final Firebase tempRes = root.child("Reservation").child(date + " " + generateTimeText(bundle.getInt("arriveHour"), bundle.getInt("arriveMin")));
                    final Firebase tempHist = root.child("History").child(date + " " + generateTimeText(bundle.getInt("arriveHour"), bundle.getInt("arriveMin")));

                    // send user to the activity_revision page
                    final Intent intent = new Intent(conteo.this, activity_revision.class);
                    intent.putExtra("arriveHour", bundle.getInt("arriveHour")); // carry hour to next layout
                    intent.putExtra("arriveMin", bundle.getInt("arriveMin"));   // carry minute to next layout

                    int tempDephHour, tempDepMin;
                    int totHours, totMins;
                    double totPay, rate;
                    rate = 2;
                    final String spot = pspot.getText().toString();

                    // cancel order
                    if (startTimeInSec > getCurrentTimeInSec()) {
                        AlertDialog.Builder Quest = new AlertDialog.Builder(conteo.this);
                        Quest.setTitle("¿Cancelar orden?");
                        Quest.setMessage(
                                "¿Seguro que quieres cancelar tu reserva?");
                        Quest.setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        Quest.setNegativeButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (myTimer != null)
                                {
                                    System.out.println("Cancelar tiempo");
                                    myTimer.cancel();
                                }
                                iLink.resetUserReservation();
                                iLink.checkout( spot ,startTimeInSec);
                                Intent intent = new Intent(conteo.this, UserHomepage.class);

                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                tempRes.removeValue();
                                Toast.makeText(conteo.this, "Orden Cancelada", Toast.LENGTH_LONG).show();
                                startActivity(intent);
                                finish();
                            }
                        });
                        AlertDialog alertDialog = Quest.create();
                        alertDialog.show();
                    } else {

                        if (getCurrentTimeInSec() >= endTimeInSec) {
                            // carry current info to next layout
                            intent.putExtra("departHour", bundle.getInt("departHour"));
                            intent.putExtra("departMin", bundle.getInt("departMin"));
                            intent.putExtra("rate", bundle.getInt("rate"));
                        } else {
                            tempDephHour = calendar.get(Calendar.HOUR_OF_DAY);  // get hour
                            tempDepMin = calendar.get(Calendar.MINUTE);         // get minute

                            // converting reservation record to history record
                            tempHist.child("Reloj").setValue(generateTimeText(bundle.getInt("arriveHour"), bundle.getInt("arriveMin")));
                            tempHist.child("Clockout").setValue(generateTimeText(tempDephHour, tempDepMin));
                            tempHist.child("Date").setValue(sdf.format(date));
                            tempHist.child("User").setValue(userName);

                            // carry hour and minute info to next layout
                            intent.putExtra("departHour", calendar.get(Calendar.HOUR_OF_DAY));
                            intent.putExtra("departMin", calendar.get(Calendar.MINUTE));

                            // Calculate total time parked and total to pay
                            totHours = tempDephHour - bundle.getInt("arriveHour");
                            totMins = tempDepMin - bundle.getInt("arriveMin");
                            totPay = (totHours + (double) totMins / 60.0) * rate;
                            tempHist.child("Rate").setValue(String.format("S/%.2f", totPay));

                            tempRes.removeValue();

                            intent.putExtra("rate", String.format("S/%.2f", totPay));
                        }

                        // early checkout
                        AlertDialog.Builder Quest = new AlertDialog.Builder(conteo.this);
                        Quest.setTitle("Confirmación de pago");
                        Quest.setMessage(
                                "Estas seguro que quieres pagar?");
                        Quest.setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        Quest.setNegativeButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // checkout and remove order in firebase
                                if (myTimer != null) {
                                    System.out.println("Cancelar Temporizador");
                                    myTimer.cancel();
                                }

                                iLink.checkout(spot, startTimeInSec);

                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                         });
                        AlertDialog alertDialog = Quest.create();
                        alertDialog.show();

                    }
                }
            }
        });


        /* report your own spot */
        reportButt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                iLink.changeLegalStatus(bundle.getString("spotAssign"), true);

                final String newSpotAssign = iLink.getSpot((int) startTimeInSec / 60, (int) endTimeInSec / 60);

                if (newSpotAssign == null) {
                    AlertDialog.Builder respond = new AlertDialog.Builder(conteo.this);
                    respond.setTitle("Informe exitoso | No hay puntos disponibles");
                    respond.setMessage("Su informe ha sido guardado con éxito.\n" +
                            "Sin embargo, no pudimos conseguir un lugar nuevo, ya que nuestro estacionamiento esta lleno." +
                            "Nos disculpamos por la invonveniencia.\n\n" +
                            "Pronto recibirá una recompensa en su cuenta.\n" +
                            "Puede ver esta actividad en el historial de la cuenta.\n");
                    respond.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(conteo.this, activity_revision.class);
                            intent.putExtra("arriveHour", bundle.getInt("arriveHour"));
                            intent.putExtra("arriveMin", bundle.getInt("arriveMin"));
                            intent.putExtra("departHour", bundle.getInt("arriveHour"));
                            intent.putExtra("departMin", bundle.getInt("arriveMin"));
                            intent.putExtra("rate", 0);
                            startActivity(intent);

                            dialog.cancel();
                        }

                    });


                    AlertDialog alertDialog = respond.create();
                    alertDialog.show();
                } else {

                    AlertDialog.Builder respond = new AlertDialog.Builder(conteo.this);
                    respond.setTitle("Informe exitoso");
                    respond.setMessage("Su informe ha sido grabado con éxito.\n" +
                            "Se te asigno un nuevo lugar de estacionamiento. " +
                            "Nos disculpamos por la inconveniencia.\n" +
                            "Pronto recibirá una recompensa en su cuenta.\n" +
                            "Puede ver esta actividad en su historial de cuenta.\n");
                    respond.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            pspot.setText(newSpotAssign);
                            iLink.setOrder(newSpotAssign, iLink.getCurTimeInSec(), endTimeInSec);

                            dialog.cancel();
                        }

                    });

                    AlertDialog alertDialog = respond.create();
                    alertDialog.show();
                }
            }
        });


        /* go to map */
        mapButt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(conteo.this, MapDirectional.class);
                startActivity(intent);
            }
        });

        /* emergency call */
        emergencyButt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(conteo.this, Emergencia.class);
                startActivity(intent);
            }
        });

        /* go back to home page */
        homeButt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(conteo.this, UserHomepage.class);
                intent.putExtra("arriveHour", bundle.getInt("arriveHour"));
                intent.putExtra("arriveMin", bundle.getInt("arriveMin"));
                intent.putExtra("departHour", bundle.getInt("departHour"));
                intent.putExtra("departMin", bundle.getInt("departMin"));
                intent.putExtra("rate", bundle.getInt("rate"));
                intent.putExtra("spotAssign", bundle.getString("spotAssign"));
                startActivity(intent);
            }
        });

    }

    private String generateTimeText(int hour, int min) {
        String timeText;
        String am_pm_Text = (hour < 12) ? "AM" : "PM";

        // Format hour
        if (hour <= 12) {
            if (hour == 0)
                hour += 12;
            timeText = String.format("%02d", hour);
        } else {
            timeText = String.format("%02d", (hour - 12));
        }

        // Add colon, min, and AM/PM sign
        timeText = (timeText + ":" + String.format("%02d", min) + " " + am_pm_Text);

        return timeText;
    }

    private long getCurrentTimeInSec() {
        Date date = new Date();                               // given date
        Calendar calendar = GregorianCalendar.getInstance();  // creates a new calendar instance
        calendar.setTime(date);                               // assigns calendar to given date
        long curHour = calendar.get(Calendar.HOUR_OF_DAY);    // gets hour in 24h format
        long curMin = calendar.get(Calendar.MINUTE);         // get cur minute
        long curSec = calendar.get(Calendar.SECOND);         // get cur second

        return ((curHour * 60) + curMin) * 60 + curSec;
    }

    protected void getParkingLotData() {

        //Getting all the usernames
        Firebase userReference = new Firebase("https://ipark-e243b.firebaseio.com/ParkingLot");

        userReference.addValueEventListener(new com.firebase.client.ValueEventListener() {
            @Override
            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                Iterable<com.firebase.client.DataSnapshot> usernames = dataSnapshot.getChildren();
                Iterator<com.firebase.client.DataSnapshot> iterator = usernames.iterator();

                //Getting usernames
                while (iterator.hasNext()) {
                    com.firebase.client.DataSnapshot node = iterator.next();
                    String uname = node.getKey();

                    Iterable<com.firebase.client.DataSnapshot> userInfo = node.getChildren();
                    Iterator<com.firebase.client.DataSnapshot> iterator1 = userInfo.iterator();

                    //Getting emails
                    while (iterator1.hasNext()) {
                        com.firebase.client.DataSnapshot innerNode = iterator1.next();
                        String innerKey = innerNode.getKey();
                        if (innerKey.equals("Reserved")) {
                            boolean spot = innerNode.getValue(Boolean.class);
                            //parkingspots.push(uname);
                            if (spot == false) {
                                parkingspots.push(uname);
                            }
                            // uMapEmail.put(uname, mail);
                            //emails.add(mail);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(conteo.this, "No se pudo conectar a la base de datos", Toast.LENGTH_LONG).show();
            }
        });
    }
}