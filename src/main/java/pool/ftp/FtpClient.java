package pool.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import pool.client.Client;
import pool.config.Configuration;
import pool.exception.ContentException;
import pool.exception.ClientException;

public class FtpClient implements Client {

	FTPClient ftpClient;

	@Override
	public void connect(Configuration config) {
		try {
			ftpClient = new FTPClient();
			ftpClient.connect(config.getHost(), config.getPort());
			verify();
			ftpClient.login(config.getUsername(), config.getPassword());
			verify();
		} catch (Exception e) {
			disconnect();
			throw new ClientException("Failed to create", e);
		}
	}

	@Override
	public void delete(String path) {
		try {
			ftpClient.deleteFile(path);
		} catch (IOException e) {
			throw new ContentException("Failed to delete file", e);
		}
	}

	@Override
	public void disconnect() {
		try {
			if (Objects.nonNull(ftpClient) && ftpClient.isConnected()) {
				ftpClient.logout();
				ftpClient.disconnect();
			}
		} catch (IOException e) {
			throw new ClientException("Failed to close", e);
		}
	}

	@Override
	public InputStream retrieve(String path) {
		try {
			InputStream content = ftpClient.retrieveFileStream(path);
			ftpClient.completePendingCommand();
			return content;
		} catch (IOException e) {
			throw new ContentException("Failed to retrieve file", e);
		}
	}

	@Override
	public boolean validate() {
		boolean result;
		try {
			result = ftpClient.sendNoOp();
		} catch (IOException e) {
			result = false;
		}
		return result;
	}

	private void verify() throws IOException {
		int reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			throw new IOException("Failed to verify");
		}
	}

}
