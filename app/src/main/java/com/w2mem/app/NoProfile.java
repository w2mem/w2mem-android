package com.w2mem.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NoProfile extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v3_no_profile);

        ((Button) findViewById(R.id.btnGoToSite))
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://w2mem.com"));
                    startActivity(intent);
                }
            });
    }
}
