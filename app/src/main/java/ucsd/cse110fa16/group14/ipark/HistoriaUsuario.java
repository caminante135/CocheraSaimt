package ucsd.cse110fa16.group14.ipark;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HistoriaUsuario extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historia_usuario);
        String[] transactions = {"Estacionamiento ilegal: 24", "Estacionamiento ilegal:25", "Estacionamiento ilegal:40"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, transactions);
        ListView list = (ListView) findViewById(R.id.listview);
        list.setAdapter(adapter);

    }

}
