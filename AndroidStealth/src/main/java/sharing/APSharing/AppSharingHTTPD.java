package sharing.APSharing;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.stealth.android.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Alex on 2/26/14.
 */
public class AppSharingHTTPD extends NanoHTTPD {
    public interface OnAppTransferListener {
        public void appTransferStarted();
        public void onBytesRead(long totalBytesRead);
        public void appTransferFinished();
    }

    public static final String ApShareUri = "/AndroidStealth.apk";
    private static final String MimeType = "application/android";

    private String fileNotFoundMsg;
    private String pageNotFoundMsg;

    private OnAppTransferListener mListener;
    private File mApkFile;
    private long mFileSize;

    public long getAppSize(){
        return mFileSize;
    }

    public void setOnAppTransferListener(OnAppTransferListener listener){
        mListener = listener;
    }

    public AppSharingHTTPD(Context context) {
        super(8080);

        mApkFile = new File(context.getPackageResourcePath());
        mFileSize = mApkFile.length();

        Resources resources = context.getResources();
        fileNotFoundMsg = resources.getString(R.string.file_not_found_message);
        pageNotFoundMsg = resources.getString(R.string.page_not_found_message);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Log.d("AppSharingHTTPD", "URI: " + uri);

        if(uri.endsWith(ApShareUri)){
            try {
                return new Response(Response.Status.OK, MimeType, getApkInputStream());
            } catch (FileNotFoundException e) {
                return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, fileNotFoundMsg);
            }
        }
        else {
            return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, pageNotFoundMsg);
        }
    }

    private InputStream getApkInputStream() throws FileNotFoundException {
        return new FileInputStream(mApkFile);
    }

    private class FileInputStreamListener extends FileInputStream {

        private boolean mFirstRead = true;
        private long byteProgress = 0;

        public FileInputStreamListener(File file) throws FileNotFoundException {
            super(file);
        }

        public FileInputStreamListener(FileDescriptor fd) {
            super(fd);
        }

        public FileInputStreamListener(String path) throws FileNotFoundException {
            super(path);
        }

        @Override
        public int read() throws IOException {
            int bytesRead = super.read();

            //notification of initialization (dat alliteration)
            if(mFirstRead){
                mFirstRead = false;
                if(mListener != null)
                    mListener.appTransferStarted();
            }

            if(bytesRead != -1){
                byteProgress += bytesRead;

                if(mListener != null){
                    mListener.onBytesRead(byteProgress);
                }
            }

            return bytesRead;
        }

        @Override
        public void close() throws IOException {
            super.close();

            //notification of finalization (dat alliteration)
            if(mListener != null)
                mListener.appTransferFinished();
        }
    }
}
