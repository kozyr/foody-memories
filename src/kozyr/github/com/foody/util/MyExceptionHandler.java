package kozyr.github.com.foody.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class MyExceptionHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler defaultUEH;
    private String localPath;
    
    public MyExceptionHandler() {
    	this(Environment.getExternalStorageDirectory().getAbsolutePath());
    }
    
	public MyExceptionHandler(String localPath) {
		this.localPath = localPath;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	}

    public void uncaughtException(Thread t, Throwable e) {
        long timestamp = new Date().getTime();
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        String filename = timestamp + ".stacktrace";

        if (localPath != null) {
            writeToFile(stacktrace, filename);
        }

        defaultUEH.uncaughtException(t, e);
    }


    private void writeToFile(String stacktrace, String filename) {
    	BufferedWriter bos = null;
    	try {
            bos = new BufferedWriter(new FileWriter(
                    new File(localPath, filename)));
            bos.write(stacktrace);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            Log.i("MyExceptionHanlder", e.getMessage());
        } finally {
        	if (bos != null) {
        		try {
					bos.close();
				} catch (IOException e) {
					// oh well...
					Log.i("MyExceptionHanlder", e.getMessage());
				}
        	}
        }
    }

}
