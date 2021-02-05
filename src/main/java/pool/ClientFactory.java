package pool;

import java.util.Objects;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import lombok.RequiredArgsConstructor;
import pool.client.Client;
import pool.config.Configuration;
import pool.config.Protocol;
import pool.ftp.FtpClient;
import pool.sftp.SftpClient;

@RequiredArgsConstructor
public class ClientFactory extends BasePooledObjectFactory<Client> {

	final Configuration configuration;

	@Override
	public Client create() throws Exception {
		Client client = null;
		if (Objects.equals(configuration.getProtocol(), Protocol.SFTP)) {
			client = new SftpClient();
		} else {
			client = new FtpClient();
		}
		client.connect(configuration);
		return client;
	}

	@Override
	public PooledObject<Client> wrap(Client session) {
		return new DefaultPooledObject<>(session);
	}

	@Override
	public void destroyObject(PooledObject<Client> p) throws Exception {
		p.getObject().disconnect();
	}

	@Override
	public boolean validateObject(PooledObject<Client> p) {
		return p.getObject().validate();
	}
}
