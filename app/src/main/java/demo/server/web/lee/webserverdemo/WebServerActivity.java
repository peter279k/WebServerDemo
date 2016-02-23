package demo.server.web.lee.webserverdemo;


import android.content.res.AssetManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;


public class WebServerActivity extends ActionBarActivity {

    Button btn;
    Button dbBtn;
    Process serverProcess = null;
    TextView logText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_server);

        btn = (Button) findViewById(R.id.button);
        dbBtn = (Button) findViewById(R.id.button2);
        logText = (TextView) findViewById(R.id.textView);

        btn.setOnClickListener(listener);
        dbBtn.setOnClickListener(listener);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.button:
                    startServerProcess(serverProcess, logText);
                    break;
                case R.id.button2:
                    startDBServerProcess(serverProcess, logText);
                default:

            }
        }
    };

    private void startDBServerProcess(Process serverProcess, TextView logText) {
        String appFileDirectory = getFilesDir().getPath();

        copyAssetsExecute("_install_mysql.zip", appFileDirectory);

        if (serverProcess == null) {
            //start db server or initial db server
            File mysqlZip = new File(appFileDirectory + "_install_mysql.zip");

            //doDecompress(mysqlZip, desDir);
        }
        else {
            logText.setText("mysql db server has started.");
        }

    }

    private void startServerProcess(Process serverProcess, TextView logText) {
        String appFileDirectory = getFilesDir().getPath();

        copyAssetsExecute("_install_php.zip", appFileDirectory);
        copyAssetsExecute("_install_lighttpd.zip", appFileDirectory);


        if (serverProcess == null) {
            //start lighttpd server
            //copy executable file
            //decompress zip file
            File phpZip = new File(appFileDirectory + "/_install_php.zip");
            File lighttpdZip = new File(appFileDirectory + "/_install_lighttpd.zip");
            File desDir = new File(appFileDirectory);

            try {
                doDecompress(phpZip, desDir);
                doDecompress(lighttpdZip, desDir);

                //String executableFilePath = appFileDirectory + "/_install_php/bin/php-cgi";
                String executableFilePath = appFileDirectory + "/_install_lighttpd/sbin/lighttpd";
                File execFile = new File(executableFilePath);
                execFile.setExecutable(true);

                BufferedReader reader;
                try {
                    //Process process = Runtime.getRuntime().exec(executableFilePath + " -m");
                    Process process = Runtime.getRuntime().exec(executableFilePath + " -V");
                    reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));
                    int read;
                    char[] buffer = new char[4096];
                    StringBuffer output = new StringBuffer();
                    while ((read = reader.read(buffer)) > 0) {
                        output.append(buffer, 0, read);
                    }

                    reader.close();
                    process.waitFor();

                    if (output.toString().length() != 0)
                        Log.e("output-version", "output: " + output.toString());
                    else {
                        reader = new BufferedReader(
                                new InputStreamReader(process.getErrorStream()));

                        buffer = new char[4096];
                        output = new StringBuffer();

                        while ((read = reader.read(buffer)) > 0) {
                            output.append(buffer, 0, read);
                        }

                        reader.close();

                        Log.e("output-error-msg", output.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            logText.append("web server has started.\n");
        }
    }

    private void copyAssetsExecute(String fileName, String appFileDirectory) {

        AssetManager assetManager = getAssets();
        try {
            String []res = assetManager.list("");
            for(int index=0;index<res.length;index++) {
                Log.e("file-list", res[index]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream in;
        OutputStream out = null;
        Log.e("copy-status", "Attempting to copy this file: " + fileName); // + " to: " +       assetCopyDestination);

        try {
            in = assetManager.open(fileName);

            Log.e("copy-status", "outDir: " + appFileDirectory);
            File outFile = new File(appFileDirectory, fileName);
            out = new FileOutputStream(outFile);

            //Apache common io libs jar https://commons.apache.org/proper/commons-io/
            IOUtils.copy(in, out);
            in.close();
            out.flush();
            out.close();

            Log.d("copy-status", "Copy success: " + fileName);

        } catch(IOException e) {
            Log.e("copy-status", "Failed to copy asset file: " + fileName, e);
        } finally {
           IOUtils.closeQuietly(out);
        }
    }

    protected void doDecompress(File srcFile, File destDir) throws IOException {
        ZipArchiveInputStream is = null;
        try {
            is = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile), 4096));
            ZipArchiveEntry entry = null;

            while ((entry = is.getNextZipEntry()) != null) {
                if (entry.isDirectory()) {
                    Log.e("dir-name", entry.getName().toString());
                    File directory = new File(destDir, entry.getName());
                    directory.mkdirs();
                }
                else {
                    OutputStream os = null;
                    try {
                        os = new BufferedOutputStream(
                                new FileOutputStream(new File(destDir, entry.getName())), 4096);
                        IOUtils.copy(is, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web_server, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //kill all process
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
