package com.olologin.shellscriptlauncher.shellscriptlauncher;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.*;


public class DummyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executeScript();
        Toast.makeText(this,"test", Toast.LENGTH_LONG).show();
        super.finish();
    }

    private void executeScript(){
        String command ="su";
        Process proc;
        try {
            proc = new ProcessBuilder()
                    .command(command)
                    .redirectErrorStream(true)
                    .start();
        }
        catch (IOException ex)
        {
            Log.e("executeScript", "IOException while trying to start new process with command " + command);
            return;
        }

        final BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        final OutputStreamWriter out = new OutputStreamWriter(proc.getOutputStream());

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        Log.i("sh:", line);
                    }
                }
                catch (IOException e) {
                    Log.e("shell output", "Error in asynctask while trying to output sh output");
                }
                Log.i("shell output", "output thread exitting");
                return null;
            }
        }.execute();

        try {
            String[] scriptNames = this.getFilesDir().getAbsoluteFile().list(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.endsWith(".sh");
                }
            });
            if (scriptNames.length == 0) {
                Log.e("executeScript", "No *.sh file in directory: " + this.getFilesDir().getAbsolutePath());
                return;
            }

            for (String scriptName : scriptNames) {
                Log.i("executeScript", "trying to launch: " + scriptName);
                command = "sh " + this.getFilesDir().getAbsolutePath() + "/" + scriptName + " \n";
                out.write(command);
                out.flush();
            }
            command = "exit\n";
            out.write(command);
            out.flush();

            proc.waitFor();
            if (proc.exitValue() != 0)
            {
                Log.e("executeScript", "Command returned error: " + command + "\n  Exit code: " + proc.exitValue());
                return;
            }
        }
        catch (IOException e) {
            Log.e("executeScript", "IOException while executing command " + command);
            return;
        }
        catch (InterruptedException e){
            Log.e("executeScript", "Interrupted exception while waiting for process " + command);
            return;
        }
        finally {
            proc.destroy();
        }
    }

}
