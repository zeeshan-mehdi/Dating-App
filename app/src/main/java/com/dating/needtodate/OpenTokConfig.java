package com.dating.needtodate;

import android.webkit.URLUtil;

public class OpenTokConfig {
    // *** Fill the following variables using your own Project info from the OpenTok dashboard  ***
    // ***                      https://dashboard.tokbox.com/projects                           ***

    // Replace with your OpenTok API key
    public static final String API_KEY = "46333442";
    // Replace with a generated Session ID
   // routed public static final String SESSION_ID = "1_MX40NjMzMzQ0Mn5-MTU1ODU0ODEyODI5M35qbTAyOG55dkxoTGlDd1RaUUhLQ3ViZjV-fg";
    public static final String SESSION_ID = "2_MX40NjMzMzQ0Mn5-MTU1ODU3MDA5MTAxOH5hVlU2ZmxFSXJDZkhNbmE2RFVyV0E1WjJ-UH4";
    // Replace with a generated token (from the dashboard or using an OpenTok server SDK)
   //routed  public static final String TOKEN = "T1==cGFydG5lcl9pZD00NjMzMzQ0MiZzaWc9M2ZhYzMyYjYxYzM1MzlhNzVjYWJhOGExNmI5ZmFmYTRlYmZmMDlkNDpzZXNzaW9uX2lkPTFfTVg0ME5qTXpNelEwTW41LU1UVTFPRFUwT0RFeU9ESTVNMzVxYlRBeU9HNTVka3hvVEdsRGQxUmFVVWhMUTNWaVpqVi1mZyZjcmVhdGVfdGltZT0xNTU4NTQ4MjU5Jm5vbmNlPTAuNzg3ODcxMTcyNjM1NzIxNSZyb2xlPW1vZGVyYXRvciZleHBpcmVfdGltZT0xNTYxMTQwMjYwJmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    public static final String TOKEN = "T1==cGFydG5lcl9pZD00NjMzMzQ0MiZzaWc9YTUzMjQ3OWE1MjQ5NDhiNWExNTc4ZDFkYTc2YWVjZGNiNDY1ZjgwNjpzZXNzaW9uX2lkPTJfTVg0ME5qTXpNelEwTW41LU1UVTFPRFUzTURBNU1UQXhPSDVoVmxVMlpteEZTWEpEWmtoTmJtRTJSRlZ5VjBFMVdqSi1VSDQmY3JlYXRlX3RpbWU9MTU1ODU3MDEzNCZub25jZT0wLjI0MzQ1NDYwOTIzMjAxMDQ1JnJvbGU9bW9kZXJhdG9yJmV4cGlyZV90aW1lPTE1NjExNjIxMzUmaW5pdGlhbF9sYXlvdXRfY2xhc3NfbGlzdD0=";

    /*                           ***** OPTIONAL *****
     If you have set up a server to provide session information replace the null value
     in CHAT_SERVER_URL with it.

     For example: "https://yoursubdomain.com"
    */
    public static final String CHAT_SERVER_URL = null;
    public static final String SESSION_INFO_ENDPOINT = CHAT_SERVER_URL + "/session";


    // *** The code below is to validate this configuration file. You do not need to modify it  ***

    public static String webServerConfigErrorMessage;
    public static String hardCodedConfigErrorMessage;

    public static boolean areHardCodedConfigsValid() {
        if (OpenTokConfig.API_KEY != null && !OpenTokConfig.API_KEY.isEmpty()
                && OpenTokConfig.SESSION_ID != null && !OpenTokConfig.SESSION_ID.isEmpty()
                && OpenTokConfig.TOKEN != null && !OpenTokConfig.TOKEN.isEmpty()) {
            return true;
        }
        else {
            hardCodedConfigErrorMessage = "API KEY, SESSION ID and TOKEN in OpenTokConfig.java cannot be null or empty.";
            return false;
        }
    }

    public static boolean isWebServerConfigUrlValid(){
        if (OpenTokConfig.CHAT_SERVER_URL == null || OpenTokConfig.CHAT_SERVER_URL.isEmpty()) {
            webServerConfigErrorMessage = "CHAT_SERVER_URL in OpenTokConfig.java must not be null or empty";
            return false;
        } else if ( !( URLUtil.isHttpsUrl(OpenTokConfig.CHAT_SERVER_URL) || URLUtil.isHttpUrl(OpenTokConfig.CHAT_SERVER_URL)) ) {
            webServerConfigErrorMessage = "CHAT_SERVER_URL in OpenTokConfig.java must be specified as either http or https";
            return false;
        } else if ( !URLUtil.isValidUrl(OpenTokConfig.CHAT_SERVER_URL) ) {
            webServerConfigErrorMessage = "CHAT_SERVER_URL in OpenTokConfig.java is not a valid URL";
            return false;
        } else {
            return true;
        }
    }
}
