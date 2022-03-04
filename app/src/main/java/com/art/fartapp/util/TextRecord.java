package com.art.fartapp.util;

import android.nfc.NdefRecord;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class TextRecord implements ParsedNdefRecord {

    private final String mLanguageCode;

    private final String mText;

    public TextRecord(String languageCode, String text) {
        this.mLanguageCode = languageCode;
        this.mText = text;
    }

    public static TextRecord parse(NdefRecord record) {
        if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
            try {
                byte[] payload = record.getPayload();
                String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                int languageCodeLength = payload[0] & 0077;
                String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
                String text =
                        new String(payload, languageCodeLength + 1,
                                payload.length - languageCodeLength - 1, textEncoding);
                return new TextRecord(languageCode, text);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            return null;
        }
    }

    public static boolean isText(NdefRecord record) {
        try {
            parse(record);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String str() {
        return mText;
    }

    public String getText() {
        return mText;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }
}
