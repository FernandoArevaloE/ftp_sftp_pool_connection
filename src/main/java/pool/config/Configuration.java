package pool.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Configuration {

	String host;
	String username;
	String password;
	int port;
	int timeout;
	Protocol protocol;
}
