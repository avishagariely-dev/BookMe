package com.example.bookme;

/*
צד הלקוח:
מסך ראשוני: פרטים אודות. לחצן קביעת תור.
התחברות והרשמה? אופציה לביטול תור כשמתחברים
מסך קביעת תור: שירותים לבחירה עם מחירים (צבע, החלקה, תספורת...)
לאחר קביעת שירות, מסך של החודש כאשר הימין הפנויים מסומנים.
בחירת יום, הצגת השעות הפנויות באותו יום
מסך אישור ותשלום: מספר אשראי, שם הלקוח, מספר טלפון...
הודעת התור נקבע בהצלחה
 שליחת הודעה לפלאפון עם פרטי התור וחשבונית

 צד העסק:
 מסך ראשי: התורים של היום יחד עם פרטי הלקוח עם אופציה לביטול תור. לחצן לוח שנה
 מסך לוח שנה: סימון יום- צפייה בתורים/חסימת יום.
 חסימת יום- כל היום/ שעות ספציפיות.
 */

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}