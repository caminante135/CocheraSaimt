package ucsd.cse110fa16.group14.ipark;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/*
Sources:
  http://www.codebind.com/android-tutorials-and-examples/android-studio-android-alert-dialog-example/
  http://stackoverflow.com/questions/2441203/how-to-make-an-android-app-return-to-the-last-open-activity-when-relaunched
 */
public class MapDirectional extends AppCompatActivity {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_directional);

        final Bundle bundle = getIntent().getExtras();

        auth = FirebaseAuth.getInstance();
        final String userName = auth.getCurrentUser().getDisplayName();
        root = new Firebase("https://ipark-e243b.firebaseio.com/Users/" + userName);

        Button emergencyButt = (Button) findViewById(R.id.button9);
        Button homeButt = (Button) findViewById(R.id.button14);
        Button reportButt = (Button) findViewById(R.id.send);
        final EditText reportSpotTextEdit = (EditText) findViewById(R.id.editText);

        /* Cannot make it work
        View userMap = findViewById(R.id.myMap);

        userMap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideKeyboard(v);

            }
        });

        */

        reportSpotTextEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        /* click emergency */
        emergencyButt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapDirectional.this, Emergencia.class);
                startActivity(intent);

            }
        });

        /* click home button and go to home page */
        homeButt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapDirectional.this, UserHomepage.class);
                startActivity(intent);

            }
        });


        /* report illegal parking of other spots */
        reportButt.setOnClickListener(new View.OnClickListener() {

            // should set the corresponding parking spot to red
            // TO DO


            // then add this log to history
            // TO DO


            // and then pop out a window indicating success report
            @Override
            public void onClick(View v) {


                String report_num_text = reportSpotTextEdit.getText().toString().trim();
                boolean reportSuccess;

                if (report_num_text.isEmpty()) {

                    AlertDialog.Builder respond = new AlertDialog.Builder(MapDirectional.this);
                    respond.setTitle("Informe fallido");
                    respond.setMessage("No especifico que lugar de estacionamiento reportar.");
                    respond.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = respond.create();
                    alertDialog.show();
                } else {
                    int report_num = Integer.parseInt(report_num_text);
                    ProgressDialog progress = new ProgressDialog(MapDirectional.this);
                    progress.show();
                    progress.setMessage("Informar....");
                    reportSuccess = iLink.reportOther(report_num);
                    progress.dismiss();

                    if (reportSuccess) {
                        Date tempDate = new Date();                               // given date
                        Calendar calendar = GregorianCalendar.getInstance();  // creates a new calendar instance
                        calendar.setTime(tempDate);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                        tempDate = null;
                        try {
                            tempDate = sdf.parse(sdf.format(new Date()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Date date = tempDate;
                        int tempHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int tempMin = calendar.get(Calendar.MINUTE);
                        final Firebase tempHist = root.child("History").child("Report: " + date + " "
                                + generateTimeText(tempHour, tempMin));
                        InputMethodManager inputManager = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ?
                                null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                        AlertDialog.Builder respond = new AlertDialog.Builder(MapDirectional.this);
                        respond.setTitle("Informe exitoso");
                        respond.setMessage("Su informe ha sido guardado con éxito.\n" +
                                "Pronto recibira una recompensa en su cuenta.\n" +
                                "Puede ver esta actividad en el historial de su cuenta.\n");
                        respond.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        reportSpotTextEdit.setText("");
                        AlertDialog alertDialog = respond.create();
                        alertDialog.show();

                        tempHist.child("Date").setValue(sdf.format(date));
                        tempHist.child("Time").setValue(generateTimeText(tempHour, tempMin));
                        tempHist.child("Spot").setValue("" + report_num);
                        tempHist.child("User").setValue(userName);


                    } else {
                        AlertDialog.Builder respond = new AlertDialog.Builder(MapDirectional.this);
                        respond.setTitle("Informe fallido");
                        respond.setMessage("Número de lugar de estacionamiento inválido.");
                        respond.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        reportSpotTextEdit.setText("");
                        AlertDialog alertDialog = respond.create();
                        alertDialog.show();
                    }
                }


            }


        });


        //Button illegalButt = (Button) findViewById(R.id.illegalButton);
        //Button checkoutButt = (Button) findViewById(R.id.checkoutButton);

        /*illegalButt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapDirectional.this, ReportIllegal.class);
                startActivity(intent);
            }
        });

        checkoutButt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapDirectional.this, activity_revision.class);
                startActivity(intent);
            }
        });*/
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

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
