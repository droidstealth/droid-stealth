package sharing.APSharing;

import android.content.Context;
import android.content.res.Resources;

import com.stealth.android.R;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * A HTTP file server
 * Created by Alex on 2/26/14.
 */
public class FileSharingHTTPD extends NanoHTTPD {
    /**
     * Interface for listening to transfer progress of a response from this server
     */
    public interface OnTransferListener {
        public void onTransferStarted(Transferable transferable);
        public void onBytesRead(long bytesRead, Transferable transferable);
        public void onTransferFinished(Transferable transferable);
    }

    private String fileNotFoundMsg;
    private String pageNotFoundMsg;

    private OnTransferListener mListener;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private Calendar cal = Calendar.getInstance();

    Map<String, Transferable> mRegisteredTransferables;

    /**
     * Sets a listener for the transfer of files
     * @param listener The listener to be set
     */
    public void setOnTransferListener(OnTransferListener listener){
        mListener = listener;
    }

    /**
     * Creates a new instance of a {@link sharing.APSharing.NanoHTTPD} server, listening on port 8080.
     * @param context {@link android.content.Context} passed along to retrieve response messages
     * to be used on failure.
     */
    public FileSharingHTTPD(Context context) {
        super(8080);

        mRegisteredTransferables = new HashMap<String, Transferable>();

        Resources resources = context.getResources();
        fileNotFoundMsg = resources.getString(R.string.file_not_found_message);
        pageNotFoundMsg = resources.getString(R.string.page_not_found_message);
    }

    /**
     * Adds the passed {@link sharing.APSharing.Transferable} to the list of accepted requests,
     * replacing any other Transferable if the uri was already in the list.
     * @param uri The uri to which the server will respond
     * @param transferable The object from which the response will be constructed.
     */
    public void addTransferable(String uri, Transferable transferable){
        mRegisteredTransferables.put(uri, transferable);
    }

    /**
     * Removes the passed {@link sharing.APSharing.Transferable} from the list of accepted requests
     * @param transferable The object to be removed
     * @return Whether the transferable was successfully removed
     */
    public boolean removeTransferable(Transferable transferable){
        return mRegisteredTransferables.remove(transferable) != null;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        Transferable transferable = mRegisteredTransferables.get(uri);

        if(transferable != null){
            try {
                Response response = new Response(
                        Response.Status.OK,
                        transferable.getMimeType(),
                        new FileInputStreamListener(transferable));

                //Since we're sending media, range is always 'bytes'
                response.addHeader("Accept-Ranges", "bytes");
                //This is transfer mode, so can close after completion
                response.addHeader("Connection", "close");
                //Send size of content-length as well, since some browsers might whine otherwise
                response.addHeader("Content-Length", String.valueOf(transferable.getContentLength()));
                //Extra information for the client
                response.addHeader("Server", "NanoHTTPD");
                response.addHeader("Date", dateFormat.format(cal.getTime()));


                return response;
            } catch (FileNotFoundException e) {
                //Notify the client we failed to find the file requested.
                return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, fileNotFoundMsg);
            }
        }
        else {
            //Notify the client the requested uri does not exist.
            return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, pageNotFoundMsg);
        }
    }

    /**
     * An extension of FileInputStream which listens for progress in the reading of the file
     */
    private class FileInputStreamListener extends FileInputStream {

        private boolean mFirstRead = true;
        private Transferable mTransferable;

        /**
         * Constructs a new {@link FileSharingHTTPD.FileInputStreamListener}, which
         * notifies a listener in outer-class {@link FileSharingHTTPD} of any progress.
         * @param transferable transferable from which the file will be retrieved and which will be
         *                     passed along to the listener.
         * @throws FileNotFoundException thrown by the {@link java.io.FileInputStream} parent class
         */
        public FileInputStreamListener(Transferable transferable) throws FileNotFoundException {
            super(transferable.getTransferObject());

            mTransferable = transferable;
        }

        /**
         * Unused but needs to be overridden for parent class
         * @param fd
         */
        public FileInputStreamListener(FileDescriptor fd) {
            super(fd);
        }

        /**
         * Unused but needs to be overridden for parent class
         * @param path
         * @throws FileNotFoundException
         */
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
                    mListener.onTransferStarted(mTransferable);
            }

            if(bytesRead != -1){
                if(mListener != null){
                    mListener.onBytesRead(bytesRead, mTransferable);
                }
            }

            return bytesRead;
        }

        @Override
        public void close() throws IOException {
            super.close();

            //notification of finalization (dat alliteration)
            if(mListener != null)
                mListener.onTransferFinished(mTransferable);
        }
    }
}
