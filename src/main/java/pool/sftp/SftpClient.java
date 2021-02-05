package pool.sftp;

import java.io.InputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import pool.client.Client;
import pool.config.Configuration;
import pool.exception.ContentException;
import pool.exception.ClientException;

public class SftpClient implements Client {

	Session session;
	ChannelSftp channel;

	@Override
	public void connect(Configuration config) {
		try {
			JSch sch = new JSch();
			session = sch.getSession(config.getUsername(), config.getHost(), config.getPort());
			session.setConfig("StrictHostKeyChecking", "no");
			session.setConfig("PreferredAuthentications", "publicke,keyboard-interactive,password");
			session.setPassword(config.getPassword());
			session.setTimeout(config.getTimeout());
			session.connect();
			Channel ch = session.openChannel("sftp");
			ch.connect();
			this.channel = (ChannelSftp) ch;
		} catch (JSchException e) {
			throw new ClientException("Failed to create", e);
		}

	}

	@Override
	public void delete(String path) {
		try {
			channel.rm(path);
		} catch (SftpException e) {
			throw new ContentException("Failed to delete file", e);
		}
	}

	@Override
	public void disconnect() {
		channel.exit();
		session.disconnect();
	}

	@Override
	public InputStream retrieve(String path) {
		try {
			return channel.get(path);
		} catch (SftpException e) {
			throw new ContentException("Failed to retrieve file", e);
		}
	}

	@Override
	public boolean validate() {
		boolean result;
		try {
			channel.cd("/");
			result = true;
		} catch (SftpException e) {
			result = false;
		}
		return result;
	}

}
