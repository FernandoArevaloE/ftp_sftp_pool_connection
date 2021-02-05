package pool.client;

import java.io.InputStream;

import pool.config.Configuration;

public interface Client {

	void connect(Configuration config);

	void delete(String path);

	void disconnect();

	InputStream retrieve(String path);

	boolean validate();
}
