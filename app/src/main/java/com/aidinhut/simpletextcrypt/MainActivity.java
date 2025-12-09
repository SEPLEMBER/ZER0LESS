/*
 * This file is part of ZER0LESS.
 * Copyright (c) 2015-2020, Aidin Gharibnavaz <aidin@aidinhut.com>
 *
 * ZER0LESS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ZER0LESS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZER0LESS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aidinhut.simpletextcrypt;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.aidinhut.simpletextcrypt.exceptions.EncryptionKeyNotSet;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_THEME = "theme";
    private static final String PREF_LANGUAGE = "language";
    Long lastActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved locale
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString(PREF_LANGUAGE, "ru");
        setLocale(language);

        // Apply theme before setContentView
        String theme = prefs.getString(PREF_THEME, "light");
        if (theme.equals("dark")) {
            setTheme(R.style.AppTheme_Dark);
        } else if (theme.equals("amoled")) {
            setTheme(R.style.AppTheme_Amoled);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);
        this.lastActivity = System.currentTimeMillis() / 1000;
    }

    private void setLocale(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Showing settings activity, when `Settings' menu item clicked.
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        if (id == R.id.action_about) {
            // Showing about message.
            showAbout();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Encrypt button handler — каскад: сначала Crypter, затем Crypter2.
     * Crypter.encrypt(...) должен вернуть строку (обычно base64) — она подвергается обфускации Crypter2.
     */
    public void onEncryptButtonClicked(View view) {
        try {
            // Первый этап: основное шифрование
            String first = Crypter.encrypt(getEncryptionKey(), getText());
            // Второй этап: обфускация/маппинг в кандзи
            String second = Crypter2.encrypt(getEncryptionKey(), first);
            setText(second);
        }
        catch (Exception error) {
            Utilities.showErrorMessage(error.getMessage(), this);
        }
    }

    /**
     * Decrypt button handler — обратный порядок: сначала Crypter2 (деобфускация), затем Crypter (расшифровка).
     */
    public void onDecryptButtonClicked(View view) {
        try {
            // Первый этап при расшифровке: деобфускация (канзи -> base64)
            String stage1 = Crypter2.decrypt(getEncryptionKey(), getText());
            // Второй этап: основное расшифрование
            String stage2 = Crypter.decrypt(getEncryptionKey(), stage1);
            setText(stage2);
        }
        catch (Exception error) {
            Utilities.showErrorMessage(error.getMessage(), this);
        }
    }

    public void onCopyButtonClicked(View view) {
        ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(getText());
    }

    public void onPasteButtonClicked(View view) {
        ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasText()) {
            setText(clipboard.getText().toString());
        }
    }

    public void onClearButtonClicked(View view) {
        setText("");
    }

    @Override
    protected void onResume() {
        int timeout = SettingsManager.getInstance().getLockTimeout(this);
        long currentTime = System.currentTimeMillis() / 1000;
        if (timeout != 0 && currentTime - lastActivity >= timeout * 60) {
            // Empty the text box, to protect privacy.
            setText("");
            // Finishing this activity, to get back to the lock screen.
            finish();
        } else {
            this.lastActivity = System.currentTimeMillis() / 1000;
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        int timeout = SettingsManager.getInstance().getLockTimeout(this);
        long currentTime = System.currentTimeMillis() / 1000;
        if (timeout == 0 || currentTime - lastActivity >= timeout * 60) {
            // Empty the text box, to protect privacy.
            setText("");
            // Finishing this activity, to get back to the lock screen.
            finish();
        }
        super.onPause();
    }

    /*
     * Returns the text inside the Text Box.
     */
    private String getText() {
        EditText textBox = (EditText)findViewById(R.id.editText);
        return textBox.getText().toString();
    }

    /*
     * Sets the specified text in the Text Box.
     */
    private void setText(String input) {
        EditText textBox = (EditText)findViewById(R.id.editText);
        textBox.setText(input);
    }

    /*
     * Returns the encryption key from settings.
     */
    private String getEncryptionKey() throws UnsupportedEncodingException,
            GeneralSecurityException,
            EncryptionKeyNotSet {
        String encKey = SettingsManager.getInstance().getEncryptionKey(this);
        if (encKey.isEmpty()) {
            throw new EncryptionKeyNotSet(this);
        }
        return encKey;
    }

    private void showAbout() {
        // To align the text at the center, and make it scrollable, I created a custom view
        // for the message dialog.
        TextView messageTextView = new TextView(this);
        messageTextView.setLinksClickable(true);
        messageTextView.setAutoLinkMask(Linkify.WEB_URLS);
        messageTextView.setText(String.format("%s\n\n%s\n\n%s\n%s\n\n%s",
                this.getString(R.string.about_copyright),
                this.getString(R.string.about_source),
                this.getString(R.string.about_license_1),
                this.getString(R.string.about_license_2),
                this.getString(R.string.about_license_3)));
        messageTextView.setPadding(10, 10, 10, 10);
        messageTextView.setGravity(Gravity.CENTER);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(messageTextView);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(scrollView);
        dialogBuilder.setPositiveButton("OK", null);
        dialogBuilder.setCancelable(true);

        dialogBuilder.show();
    }
}
