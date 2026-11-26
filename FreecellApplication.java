import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, R2dbcAutoConfiguration.class})
public class FreecellApplication {
	public static void main(String[] args) {
		SpringApplication.run(FreecellApplication.class, args);
	}
}
