import filters.InterceptingFilter;
import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class Main {

    public static void main(String[] args) {
        HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
                .withPort(8080)
                .withManInTheMiddle(new SelfSignedMitmManager())
                .withFiltersSource(new InterceptingFilter())
                .start();

        System.out.println("Proxy started on port 8080 with intercepting capabilities.");
    }
}
