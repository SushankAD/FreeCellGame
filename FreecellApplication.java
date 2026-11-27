import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, R2dbcAutoConfiguration.class})
@Import({FreecellApi.class, FreecellWebConfig.class})
public class FreecellApplication {
	public static void main(String[] args) {
		SpringApplication.run(FreecellApplication.class, args);
	}
}
