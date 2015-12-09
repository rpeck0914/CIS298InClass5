package edu.kvcc.cis298.inclass3.inclass3;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by dbarnes on 12/7/2015.
 */

/*
How JavaScript works...

var myNewThing = {};
myNewThing.somethingElse = 1;

myNewThing : {
    somethingElse: 1;
}
*/

public class CrimeFetcher {

    //String constant for logging.
    private static final String TAG = "CrimeFragment";

    //Method to get the raw bytes from the web source. Conversion from bytes
    //to something more meaningful will happen in a different method.
    //The method has one parameter which is the url that we want to connect
    //to.
    private byte[] getUrlBytes(String urlSpec) throws IOException {
        //Create a new URL object from the url string that was passed in.
        URL url = new URL(urlSpec);
        //Create a new HTTP connection to the specified url.
        //If we were to load data from a secure site, it would need
        //to use HttpsURLConnection.
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            //Create a output stream to hold that data that is read from
            //the url source.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            //create a input stream from the http connection.
            InputStream in = connection.getInputStream();

            //Check to see that the response code from the http request is
            //200, which is the same as http_ok. Every web request will return
            //some sort of response code. You can google them. Typically
            //200's = good, 300's = cache, 400's = error, 500's = server error.
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                ": with " +
                urlSpec);
            }

            //Create an int to hold how many bytes were read in.
            int bytesRead = 0;
            //Create a byte array to act as a buffer that will read
            //in up to 1024 bytes at a time.
            byte[] buffer = new byte[1024];

            //While we can read bytes from the input stream
            while ((bytesRead = in.read(buffer)) > 0) {
                //write the bytes out to the output stream
                out.write(buffer, 0, bytesRead);
            }
            //Once everything has been read and written, close the
            //output stream
            out.close();

            //Convert the output stream to a byte array.
            return out.toByteArray();
        } finally {
            //Make sure the connection to the web is closed.
            connection.disconnect();
        }
    }

    //Method to get the string result from the http web address
    //The url bytes representing the data get returned from the
    //getUrlBytes method, and are then transformed into a string.
    private String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    //Method to fetch the crimes from the web url and parse them into crimes
    public List<Crime> fetchCrimes() {

        //Make a new list of crimes that can be returned.
        List<Crime> crimes = new ArrayList<>();

        try {
            //This method will take the original URL and allow us to add
            //any parameters that might be required to it.
            //For the URL's on my server there are no additional parameters
            //needed. However many API's require extra parameters. The API
            //that the book uses requires extra parameters and this is where
            //they add them.
            String url = Uri.parse("http://barnesbrothers.homeserver.com/crimeapi")
                    .buildUpon()
                            //Add extra parameters here with the method
                            //.appendQueryParameter("param", "Value")
                    .build().toString();

            //This calls the above methods to use the URL to get the JSON from
            //the web service. After this call we will actually have the JSON
            //that we need to parse.
            String jsonString = getUrlString(url);

            //This will take the jsonString that we got back and put it into
            //a jsonArray object. We have to use a jsonArray because our
            //jsonString starts out with an Array. If it started with an Object
            //"{}" we would need to use JSONObject instead of JSONArray. The
            //book uses JSONObject for their parse.
            JSONArray jsonArray = new JSONArray(jsonString);

            //Call the parseCrimes method that is defined below. Send in the
            //crimes list that was created at the top of this method. Also
            //send in the jsonArray that was created in the previous statement.
            //Since the List that was sent in was declared in this method, and is
            //and object, it will be automatically passed by reference. Once the
            //parseCrimes method completes the crimes list variable that was passed
            //in will have the data created in the parseCrimes method.
            parseCrimes(crimes, jsonArray);

            Log.i(TAG, "Received JSON: "+ jsonString);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }

        //return the list of crimes
        return crimes;
    }

    private void parseCrimes(List<Crime> crimes, JSONArray jsonArray)
        throws IOException, JSONException {

        //Loop through all of the elements in the JSONArray that was sent
        //into this method.
        for (int i=0; i<jsonArray.length(); i++) {

            //Fetch a single JSONObject out from the JSONArray based on
            //the current index that we are on.
            JSONObject crimeJsonObject = jsonArray.getJSONObject(i);

            //Pull the Value from the JSONObject for the Key of "uuid"
            String uuidString = crimeJsonObject.getString("uuid");
            //Use the Value to create a new UUID from that string
            UUID uuidForNewCrime = UUID.fromString(uuidString);
            //Create a new Crime passing in the newly created UUID.
            Crime crime = new Crime(uuidForNewCrime);

            //Set the title on the crime by retrieving it from the
            //JSONObject.
            crime.setTitle(crimeJsonObject.getString("title"));

            //This try block is to do the work of parsing the date string.
            try {
                //Declare a date formatter that can be used to parse the date from
                //a string into an actual date object.
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                //User the format to parse the string that we get from the JSONObject
                Date date = format.parse(crimeJsonObject.getString("incident_date"));
                //Set the date on the crime to the parsed date.
                crime.setDate(date);
            } catch (Exception e) {
                //If there is an exception, just set the date to today.
                crime.setDate(new Date());
            }

            //Evaluate the is_solved value from the JSONObject to see if it is
            //equal to "1". If the expression is true, then the is_solved property
            //of the crime will be true. Else false.
            crime.setSolved(crimeJsonObject.getString("is_solved").equals("1"));

            //Add the finished crime to the list of crimes that was passed in
            //to this method.
            crimes.add(crime);
        }
    }
}












