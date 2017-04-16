package ece420.lab6;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    public static int appflag = 0;
    private Button buttonST;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        buttonST = (Button) findViewById(R.id.buttonST);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
        {ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1);}

        buttonST.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                appflag = 1;
                startActivity(new Intent(MainActivity.this, HistEq.class));
            }
        });




    }
}
