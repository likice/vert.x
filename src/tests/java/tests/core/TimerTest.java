package tests.core;

import org.nodex.core.Nodex;
import org.nodex.core.buffer.Buffer;
import org.nodex.core.buffer.DataHandler;
import org.nodex.core.net.NetClient;
import org.nodex.core.net.NetConnectHandler;
import org.nodex.core.net.NetServer;
import org.nodex.core.net.NetSocket;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import tests.Utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: timfox
 * Date: 21/07/2011
 * Time: 13:58
 */
public class TimerTest extends TestBase {
  @BeforeClass
  public void setUp() {
  }

  @AfterClass
  public void tearDown() {
  }

  @Test
  /*
  Test that can't set a timer without a context
   */
  public void testNoContext() throws Exception {
    final AtomicBoolean fired = new AtomicBoolean(false);
    try {
      Nodex.instance.setTimeout(1, new Runnable() {
        public void run() {
          fired.set(true);
        }
      });
      assert false : "Should throw Exception";
    } catch (IllegalStateException e) {
      //OK
    }
    Thread.sleep(100);
    assert !fired.get();
  }

  @Test
  /*
  Test that timers can be set from connect handler
   */
  public void testOneOffInConnect() throws Exception {
    final CountDownLatch endLatch = new CountDownLatch(1);
    NetServer server = NetServer.createServer(new NetConnectHandler() {
      public void onConnect(final NetSocket sock) {
        final Thread th = Thread.currentThread();
        final String contextID = Nodex.instance.getContextID();
        Nodex.instance.setTimeout(1, new Runnable() {
          public void run() {
            assert th == Thread.currentThread();
            assert contextID == Nodex.instance.getContextID();
            endLatch.countDown();
          }
        });
      }
    }).listen(8181);

    NetClient client = NetClient.createClient();
    client.connect(8181, new NetConnectHandler() {
      public void onConnect(NetSocket sock) {
      }
    });

    assert endLatch.await(5, TimeUnit.SECONDS);

    awaitClose(server);
  }

  @Test
  /*
  Test that timers can be set from data handler
   */
  public void testOneOffInData() throws Exception {
    final CountDownLatch endLatch = new CountDownLatch(1);
    NetServer server = NetServer.createServer(new NetConnectHandler() {
      public void onConnect(final NetSocket sock) {
        final Thread th = Thread.currentThread();
        final String contextID = Nodex.instance.getContextID();
        sock.data(new DataHandler() {
          public void onData(Buffer data) {
            Nodex.instance.setTimeout(1, new Runnable() {
              public void run() {
                assert th == Thread.currentThread();
                assert contextID == Nodex.instance.getContextID();
                endLatch.countDown();
              }
            });
          }
        });

      }
    }).listen(8181);

    NetClient client = NetClient.createClient();
    client.connect(8181, new NetConnectHandler() {
      public void onConnect(NetSocket sock) {
        sock.write(Utils.generateRandomBuffer(100));
      }
    });

    assert endLatch.await(5, TimeUnit.SECONDS);

    awaitClose(server);
  }

  @Test
  /*
  Test the timers fire with approximately the correct delay
   */
  public void testTimings() throws Exception {
    final CountDownLatch endLatch = new CountDownLatch(1);
    NetServer server = NetServer.createServer(new NetConnectHandler() {
      public void onConnect(final NetSocket sock) {
        final Thread th = Thread.currentThread();
        final String contextID = Nodex.instance.getContextID();
        final long start = System.nanoTime();
        final long delay = 100;
        Nodex.instance.setTimeout(delay, new Runnable() {
          public void run() {
            long dur = (System.nanoTime() - start) / 1000000;
            assert dur >= delay;
            assert dur < delay * 1.25; // 25% margin of error
            assert th == Thread.currentThread();
            assert contextID == Nodex.instance.getContextID();
            endLatch.countDown();
          }
        });
      }
    }).listen(8181);

    NetClient client = NetClient.createClient();
    client.connect(8181, new NetConnectHandler() {
      public void onConnect(NetSocket sock) {
      }
    });

    assert endLatch.await(5, TimeUnit.SECONDS);

    awaitClose(server);
  }

}
