import com.sun.java.accessibility.util.SwingEventMonitor;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;
import javax.swing.*;

public class WebWorker extends Thread {
    private final String DATE_FORMAT = "HH:mm:ss";
    private String urlString;
    private int row;
    private WebFrame frame;
    private Semaphore workers;

    public WebWorker(WebFrame frame, int row, String url, Semaphore workers){
        this.workers = workers;
        urlString = url;
        this.row = row;
        this.frame = frame;
    }
  //This is the core web/download i/o code...
 	private void download() {
        long startTime = System.currentTimeMillis();
        InputStream input = null;
        StringBuilder contents = null;
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            // Set connect() to throw an IOException
            // if connection does not succeed in this many msecs.
            connection.setConnectTimeout(5000);

            connection.connect();
            input = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            char[] array = new char[1000];
            int len;
            contents = new StringBuilder(1000);
            while ((len = reader.read(array, 0, array.length)) > 0) {
                contents.append(array, 0, len);
                Thread.sleep(100);
            }

            // Successful download if we get here
            StringBuilder builder = new StringBuilder();
            long elapsedTime = System.currentTimeMillis() - startTime;
            SimpleDateFormat dateformat = new SimpleDateFormat(DATE_FORMAT);
            String datetime = dateformat.format(System.currentTimeMillis());
            builder.append(datetime + " " + elapsedTime + "ms " +  contents.length() + " bytes");
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    frame.updateRow(builder.toString(), row);
                }
            });
        }
        // Otherwise control jumps to a catch...
        catch (MalformedURLException ignored) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    frame.updateRow("err", row);
                }
            });

        } catch (InterruptedException exception) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    frame.updateRow("interrupted", row);
                }
            });

            // deal with interruption
        } catch (IOException ignored) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    frame.updateRow("err", row);
                }
            });

        }
        // "finally" clause, to close the input stream
        // in any case
        finally {
            try {
                if (input != null) input.close();
            } catch (IOException ignored) {
            }
        }
    }
    @Override
    public void run(){
        download();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.updateDisplayInformation(row);
            }
        });
        workers.release();
    }

}
