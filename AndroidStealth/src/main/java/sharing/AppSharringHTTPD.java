package sharing;

/**
 * Created by Alex on 2/26/14.
 */
public class AppSharringHTTPD extends NanoHTTPD {
    public AppSharringHTTPD(int port) {
        super(port);
    }

    public AppSharringHTTPD(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        return super.serve(session);
    }
}
