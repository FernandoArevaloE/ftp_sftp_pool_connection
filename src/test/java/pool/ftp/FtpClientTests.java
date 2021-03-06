/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package pool.ftp;

import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import pool.ClientFactory;
import pool.client.Client;
import pool.config.Configuration;
import pool.config.Protocol;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;

public class FtpClientTests {

	static final int TOTAL_CONNECTIONS = 100;
	static final String HOST = "localhost";
	static final String USERNAME = "user";
	static final String PASSWORD = "password";
	static final int PORT = 100;
	static final Protocol PROTOCOL = Protocol.FTP;
	static final String DIRECTORY = "/data";

	FakeFtpServer fakeFtpServer;

	GenericObjectPool<Client> pool;

	Configuration configuration;

	@Before
	public void setup() throws IOException {
		fakeFtpServer = new FakeFtpServer();
		fakeFtpServer.addUserAccount(new UserAccount(USERNAME, PASSWORD, DIRECTORY));

		FileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new DirectoryEntry(DIRECTORY));

		fileSystem.add(new FileEntry(String.format("%s/%s", DIRECTORY, "temp.txt"), "test content"));
		fakeFtpServer.setFileSystem(fileSystem);
		fakeFtpServer.setServerControlPort(PORT);

		fakeFtpServer.start();

		configuration = new Configuration(HOST, USERNAME, PASSWORD, PORT, 10, PROTOCOL);

		pool = new GenericObjectPool<Client>(new ClientFactory(configuration));
		pool.setMaxTotal(TOTAL_CONNECTIONS);
		pool.setTestOnBorrow(true);
	}

	@Test
	public void recoveredFileTest() throws Exception {
		Client client = new FtpClient();
		client.connect(configuration);
		InputStream content = client.retrieve(String.format("%s/%s", DIRECTORY, "temp.txt"));
		assertNotNull(content);
		client.disconnect();
	}

	@Test
	public void deletedFileTest() {
		Client client = new FtpClient();
		client.connect(configuration);
		client.delete(String.format("%s/%s", DIRECTORY, "temp.txt"));
		client.disconnect();
		assertThat(true, Matchers.is(fakeFtpServer.getFileSystem().listFiles(DIRECTORY).isEmpty()));
	}

	@Test
	public void concurrentClientsTest() throws Exception {
		final Integer totalClients = 8;
		final Map<Integer, InputStream> map = new Hashtable<>();

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		List<Client> activeClients = new ArrayList<>();
		for (int i = 0; i < totalClients; i++) {
			final int index = i + 1;
			final Client ftpClient = pool.borrowObject();
			activeClients.add(ftpClient);
			executor.execute(() -> {
				InputStream content = ftpClient.retrieve(String.format("%s/%s", DIRECTORY, "temp.txt"));
				map.put(index, content);
			});
		}

		executor.shutdown();
		executor.awaitTermination(totalClients + 2, TimeUnit.SECONDS);

		Integer recoveredFiles = map.size();

		Integer result = pool.getNumActive();

		assertEquals(totalClients, result);
		assertEquals(totalClients, recoveredFiles);

		for (Client c : activeClients) {
			pool.returnObject(c);
		}
		assertEquals(BigInteger.ZERO.intValue(), pool.getNumActive());
	}

	@After
	public void teardown() throws IOException {
		pool.close();
		fakeFtpServer.stop();
	}
}
