package cnj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@SpringBootApplication
public class BrokerApplication {

 @Autowired
 public void configure (Environment e){
  System.out.println(Arrays.toString(e.getActiveProfiles()));
 }

 public static void main(String[] args) {
  SpringApplication.run(BrokerApplication.class, args);
 }
}
